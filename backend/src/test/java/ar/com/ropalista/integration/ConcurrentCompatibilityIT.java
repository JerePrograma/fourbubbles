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
class ConcurrentCompatibilityIT extends PostgresIntegrationTestSupport {
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private UserAccountRepository users;
    @Autowired
    private PasswordEncoder passwordEncoder;

    @Test
    void reversedConcurrentRequestsReuseTheSameEvaluation() throws Exception {
        String token = login();
        String firstOrder = createClassifiedOrder(token, "concurrent-a");
        String secondOrder = createClassifiedOrder(token, "concurrent-b");
        saveProfile(token, firstOrder);
        saveProfile(token, secondOrder);

        var start = new CountDownLatch(1);
        var executor = Executors.newFixedThreadPool(2);
        try {
            var first = executor.submit(() -> evaluateAfterSignal(start, token, firstOrder, secondOrder));
            var second = executor.submit(() -> evaluateAfterSignal(start, token, secondOrder, firstOrder));
            start.countDown();

            String firstId = first.get(30, TimeUnit.SECONDS);
            String secondId = second.get(30, TimeUnit.SECONDS);
            assertThat(firstId).isNotBlank().isEqualTo(secondId);
        } finally {
            executor.shutdownNow();
        }
    }

    private String evaluateAfterSignal(CountDownLatch start, String token, String firstOrder, String secondOrder) {
        try {
            start.await(10, TimeUnit.SECONDS);
            var result = mockMvc.perform(post("/compatibility/evaluate")
                            .header("Authorization", bearer(token))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(Map.of(
                                    "orderAId", firstOrder,
                                    "orderBId", secondOrder))))
                    .andExpect(status().isOk())
                    .andReturn();
            return objectMapper.readTree(result.getResponse().getContentAsString())
                    .path("data").path("id").asText();
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    private void saveProfile(String token, String orderId) throws Exception {
        mockMvc.perform(put("/orders/{id}/compatibility-profile", orderId)
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
                                Map.entry("exclusiveCycle", false))))
                .andExpect(status().isOk());
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
                .header("Idempotency-Key", "compat-concurrent-reception-" + UUID.randomUUID())
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
                          "firstName":"Compatibilidad",
                          "lastName":"%s",
                          "phone":"1133%s",
                          "whatsapp":"1177%s",
                          "addresses":[{
                            "zoneCode":"MARCOS_PAZ",
                            "street":"Sarmiento",
                            "number":"321",
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
        String username = "admin-compat-concurrent-" + UUID.randomUUID();
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
}
