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

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ProductionFlowIT extends PostgresIntegrationTestSupport {
    private static final String WASHER_ID = "94000000-0000-0000-0000-000000000001";
    private static final String DRYER_ID = "94000000-0000-0000-0000-000000000002";
    private static final String WASH_PROGRAM_ID = "95000000-0000-0000-0000-000000000002";
    private static final String DRY_PROGRAM_ID = "95000000-0000-0000-0000-000000000005";

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;
    @Autowired UserAccountRepository users;
    @Autowired PasswordEncoder passwordEncoder;

    @Test
    void compatibleOrdersCompleteWashDryAndQualityFlow() throws Exception {
        String token = login(Role.ADMIN);
        String first = createClassifiedOrder(token, "production-first", 2600, false);
        String second = createClassifiedOrder(token, "production-second", 2600, false);
        saveProfile(token, first, true, false);
        saveProfile(token, second, true, false);
        evaluate(token, first, second);

        String washKey = "production-wash-" + UUID.randomUUID();
        JsonNode wash = createCycle(token, washKey, WASHER_ID, WASH_PROGRAM_ID, List.of(first, second));
        JsonNode washReplay = createCycle(token, washKey, WASHER_ID, WASH_PROGRAM_ID, List.of(second, first));
        assertThat(wash.path("data").path("id").asText())
                .isEqualTo(washReplay.path("data").path("id").asText());
        assertThat(wash.path("data").path("plannedWeightGrams").asInt()).isEqualTo(5200);
        assertThat(wash.path("data").path("orders")).hasSize(2);
        assertThat(wash.path("data").path("orders").get(0).path("orderStatus").asText())
                .isEqualTo("WAITING_WASH");

        String washCycleId = wash.path("data").path("id").asText();
        JsonNode runningWash = action(token, washCycleId, "start", Map.of("observation", "Inicio lavado"));
        assertThat(runningWash.path("data").path("status").asText()).isEqualTo("RUNNING");
        assertThat(runningWash.path("data").path("orders").get(0).path("orderStatus").asText())
                .isEqualTo("WASHING");

        JsonNode completedWash = action(token, washCycleId, "complete",
                Map.of("actualWeightGrams", 5200, "observation", "Lavado completo"));
        assertThat(completedWash.path("data").path("status").asText()).isEqualTo("COMPLETED");
        assertThat(completedWash.path("data").path("orders").get(0).path("orderStatus").asText())
                .isEqualTo("WAITING_DRY");

        JsonNode dry = createCycle(token, "production-dry-" + UUID.randomUUID(),
                DRYER_ID, DRY_PROGRAM_ID, List.of(first, second));
        String dryCycleId = dry.path("data").path("id").asText();
        action(token, dryCycleId, "start", Map.of("observation", "Inicio secado"));
        JsonNode completedDry = action(token, dryCycleId, "complete",
                Map.of("actualWeightGrams", 5100, "observation", "Secado completo"));
        assertThat(completedDry.path("data").path("orders").get(0).path("orderStatus").asText())
                .isEqualTo("QUALITY_CONTROL");

        quality(token, first, "PASS", "Control conforme");
        quality(token, second, "REWASH", "Persisten manchas");
        assertOrderStatus(token, first, "FOLDING");
        assertOrderStatus(token, second, "REWASH_REQUIRED");
    }

    @Test
    void machineCapacityCannotBeExceeded() throws Exception {
        String token = login(Role.ADMIN);
        String order = createClassifiedOrder(token, "production-heavy", 11000, true);
        saveProfile(token, order, true, false);

        mockMvc.perform(post("/production/cycles")
                        .header("Authorization", bearer(token))
                        .header("Idempotency-Key", "production-heavy-" + UUID.randomUUID())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "machineId", WASHER_ID,
                                "programId", WASH_PROGRAM_ID,
                                "orderIds", List.of(order)))))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.code").value("PRODUCTION_MACHINE_CAPACITY_EXCEEDED"));
    }

    private JsonNode createCycle(String token, String key, String machineId,
                                 String programId, List<String> orderIds) throws Exception {
        return performJson(post("/production/cycles")
                .header("Authorization", bearer(token))
                .header("Idempotency-Key", key)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of(
                        "machineId", machineId,
                        "programId", programId,
                        "orderIds", orderIds,
                        "notes", "Ciclo de integración"))));
    }

    private JsonNode action(String token, String cycleId, String action, Map<String, Object> body)
            throws Exception {
        return performJson(post("/production/cycles/{id}/{action}", cycleId, action)
                .header("Authorization", bearer(token))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(body)));
    }

    private void quality(String token, String orderId, String decision, String observation) throws Exception {
        performJson(patch("/production/orders/{id}/quality-control", orderId)
                .header("Authorization", bearer(token))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of(
                        "decision", decision,
                        "observation", observation))));
    }

    private void assertOrderStatus(String token, String orderId, String expected) throws Exception {
        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get("/orders/{id}", orderId)
                        .header("Authorization", bearer(token)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value(expected));
    }

    private void saveProfile(String token, String orderId, boolean dryer, boolean exclusive) throws Exception {
        performJson(put("/orders/{id}/compatibility-profile", orderId)
                .header("Authorization", bearer(token))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.ofEntries(
                        Map.entry("colorGroup", "LIGHT"),
                        Map.entry("materialGroup", "COTTON"),
                        Map.entry("maxTemperatureC", 40),
                        Map.entry("dryerAllowed", dryer),
                        Map.entry("fragrancePolicy", "STANDARD"),
                        Map.entry("softenerAllowed", true),
                        Map.entry("hypoallergenic", false),
                        Map.entry("babyClothes", false),
                        Map.entry("petContact", false),
                        Map.entry("heavySoil", false),
                        Map.entry("exclusiveCycle", exclusive)))));
    }

    private void evaluate(String token, String first, String second) throws Exception {
        performJson(post("/compatibility/evaluate")
                .header("Authorization", bearer(token))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of("orderAId", first, "orderBId", second))));
    }

    private String createClassifiedOrder(String token, String label, int actualWeight,
                                         boolean approveDifference) throws Exception {
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
                .header("Idempotency-Key", "production-reception-" + UUID.randomUUID())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of(
                        "actualWeightGrams", actualWeight,
                        "items", List.of(Map.of(
                                "equivalenceCode", "TSHIRT",
                                "actualPhysicalPieces", 2,
                                "damageDetected", false,
                                "stainDetected", false))))));
        if (approveDifference) {
            performJson(post("/orders/{id}/reception/decision", orderId)
                    .header("Authorization", bearer(token))
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(Map.of(
                            "decision", "APPROVED",
                            "notes", "Peso excepcional aprobado"))));
        }
        return orderId;
    }

    private JsonNode createClient(String token, String label) throws Exception {
        String suffix = UUID.randomUUID().toString().replace("-", "").substring(0, 10);
        return performJson(post("/clients")
                .header("Authorization", bearer(token))
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                          "firstName":"Producción",
                          "lastName":"%s",
                          "phone":"1144%s",
                          "whatsapp":"1188%s",
                          "addresses":[{
                            "zoneCode":"MARCOS_PAZ",
                            "street":"Belgrano",
                            "number":"789",
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

    private String login(Role role) throws Exception {
        String username = role.name().toLowerCase() + "-production-" + UUID.randomUUID();
        String password = "Test-password-123!";
        users.save(new UserAccount(username, passwordEncoder.encode(password), Set.of(role)));
        JsonNode response = performJson(post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of("username", username, "password", password))));
        return response.path("data").path("accessToken").asText();
    }

    private String bearer(String token) {
        return "Bearer " + token;
    }
}
