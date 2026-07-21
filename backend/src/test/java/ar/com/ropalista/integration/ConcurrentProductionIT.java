package ar.com.ropalista.integration;

import ar.com.ropalista.auth.domain.Role;
import ar.com.ropalista.auth.domain.UserAccount;
import ar.com.ropalista.auth.persistence.UserAccountRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ConcurrentProductionIT extends PostgresIntegrationTestSupport {
    private static final String WASH_PROGRAM_ID = "95000000-0000-0000-0000-000000000002";

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;
    @Autowired UserAccountRepository users;
    @Autowired PasswordEncoder passwordEncoder;

    @Test
    void concurrentReplayWithSameKeyReturnsSameCycle() throws Exception {
        String token = login();
        String machineId = createMachine(token, "IDEMP");
        String orderId = createClassifiedOrder(token, "idempotent");
        saveProfile(token, orderId);
        String key = "production-idempotency-" + UUID.randomUUID();

        var start = new CountDownLatch(1);
        var executor = Executors.newFixedThreadPool(2);
        try {
            var first = executor.submit(() -> createAfterSignal(start, token, key, machineId, orderId));
            var second = executor.submit(() -> createAfterSignal(start, token, key, machineId, orderId));
            start.countDown();
            Result firstResult = first.get(30, TimeUnit.SECONDS);
            Result secondResult = second.get(30, TimeUnit.SECONDS);
            assertThat(firstResult.status()).isEqualTo(200);
            assertThat(secondResult.status()).isEqualTo(200);
            assertThat(firstResult.id()).isNotBlank().isEqualTo(secondResult.id());
        } finally {
            executor.shutdownNow();
        }
    }

    @Test
    void differentKeysCompetingForOneMachineAllowOnlyOneCycle() throws Exception {
        String token = login();
        String machineId = createMachine(token, "BUSY");
        String firstOrder = createClassifiedOrder(token, "machine-first");
        String secondOrder = createClassifiedOrder(token, "machine-second");
        saveProfile(token, firstOrder);
        saveProfile(token, secondOrder);

        var start = new CountDownLatch(1);
        var executor = Executors.newFixedThreadPool(2);
        try {
            var first = executor.submit(() -> createAfterSignal(start, token,
                    "production-machine-a-" + UUID.randomUUID(), machineId, firstOrder));
            var second = executor.submit(() -> createAfterSignal(start, token,
                    "production-machine-b-" + UUID.randomUUID(), machineId, secondOrder));
            start.countDown();
            Result firstResult = first.get(30, TimeUnit.SECONDS);
            Result secondResult = second.get(30, TimeUnit.SECONDS);
            assertThat(List.of(firstResult.status(), secondResult.status()))
                    .containsExactlyInAnyOrder(200, 409);
            Result conflict = firstResult.status() == 409 ? firstResult : secondResult;
            assertThat(conflict.code()).isEqualTo("PRODUCTION_MACHINE_BUSY");
        } finally {
            executor.shutdownNow();
        }
    }

    private Result createAfterSignal(CountDownLatch start, String token, String key,
                                     String machineId, String orderId) {
        try {
            start.await(10, TimeUnit.SECONDS);
            var response = mockMvc.perform(post("/production/cycles")
                            .header("Authorization", bearer(token))
                            .header("Idempotency-Key", key)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(Map.of(
                                    "machineId", machineId,
                                    "programId", WASH_PROGRAM_ID,
                                    "orderIds", List.of(orderId)))))
                    .andReturn().getResponse();
            JsonNode body = objectMapper.readTree(response.getContentAsString());
            return new Result(response.getStatus(), body.path("data").path("id").asText(),
                    body.path("code").asText());
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    private String createMachine(String token, String suffix) throws Exception {
        String shortId = UUID.randomUUID().toString().substring(0, 8);
        JsonNode response = performJson(post("/production/machines")
                .header("Authorization", bearer(token))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of(
                        "code", "WC_" + suffix + "_" + shortId,
                        "name", "Lavadora concurrente " + suffix,
                        "machineType", "WASHER",
                        "capacityGrams", 10000,
                        "status", "ACTIVE",
                        "active", true))));
        return response.path("data").path("id").asText();
    }

    private void saveProfile(String token, String orderId) throws Exception {
        performJson(put("/orders/{id}/compatibility-profile", orderId)
                .header("Authorization", bearer(token))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.ofEntries(
                        Map.entry("colorGroup", "LIGHT"),
                        Map.entry("materialGroup", "COTTON"),
                        Map.entry("maxTemperatureC", 40),
                        Map.entry("dryerAllowed", true),
                        Map.entry("fragrancePolicy", "STANDARD"),
                        Map.entry("softenerAllowed", true),
                        Map.entry("hypoallergenic", false),
                        Map.entry("babyClothes", false),
                        Map.entry("petContact", false),
                        Map.entry("heavySoil", false),
                        Map.entry("exclusiveCycle", false)))));
    }

    private String createClassifiedOrder(String token, String label) throws Exception {
        JsonNode client = createClient(token, label);
        String clientId = client.path("data").path("id").asText();
        String addressId = client.path("data").path("addresses").get(0).path("id").asText();
        JsonNode order = performJson(post("/orders")
                .header("Authorization", bearer(token))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of(
                        "clientId", clientId,
                        "addressId", addressId,
                        "serviceCode", "ROPA_LISTA_12",
                        "declaredWeightGrams", 2500,
                        "exclusiveCycle", false,
                        "items", List.of(Map.of("equivalenceCode", "TSHIRT", "physicalPieces", 2))))));
        String orderId = order.path("data").path("id").asText();
        performJson(post("/orders/{id}/confirm-price", orderId).header("Authorization", bearer(token)));
        changeStatus(token, orderId, "RESERVED");
        changeStatus(token, orderId, "PICKUP_SCHEDULED");
        changeStatus(token, orderId, "PICKED_UP");
        performJson(post("/orders/{id}/reception", orderId)
                .header("Authorization", bearer(token))
                .header("Idempotency-Key", "production-concurrent-reception-" + UUID.randomUUID())
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                          "actualWeightGrams":2500,
                          "items":[{
                            "equivalenceCode":"TSHIRT",
                            "actualPhysicalPieces":2,
                            "damageDetected":false,
                            "stainDetected":false
                          }]
                        }
                        """));
        return orderId;
    }

    private JsonNode createClient(String token, String label) throws Exception {
        String suffix = UUID.randomUUID().toString().replace("-", "").substring(0, 10);
        return performJson(post("/clients")
                .header("Authorization", bearer(token))
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                          "firstName":"Producción concurrente",
                          "lastName":"%s",
                          "phone":"1155%s",
                          "whatsapp":"1199%s",
                          "addresses":[{
                            "zoneCode":"MARCOS_PAZ",
                            "street":"Rivadavia",
                            "number":"111",
                            "locality":"Marcos Paz",
                            "primaryAddress":true
                          }]
                        }
                        """.formatted(label, suffix, suffix)));
    }

    private void changeStatus(String token, String orderId, String statusName) throws Exception {
        performJson(patch("/orders/{id}/status", orderId)
                .header("Authorization", bearer(token))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of("newStatus", statusName))));
    }

    private JsonNode performJson(org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder request)
            throws Exception {
        var result = mockMvc.perform(request).andExpect(status().isOk()).andReturn();
        return objectMapper.readTree(result.getResponse().getContentAsString());
    }

    private String login() throws Exception {
        String username = "admin-production-concurrent-" + UUID.randomUUID();
        String password = "Test-password-123!";
        users.save(new UserAccount(username, passwordEncoder.encode(password), Set.of(Role.ADMIN)));
        JsonNode response = performJson(post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of("username", username, "password", password))));
        return response.path("data").path("accessToken").asText();
    }

    private String bearer(String token) {
        return "Bearer " + token;
    }

    private record Result(int status, String id, String code) {}
}
