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

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ProductionMetricsIT extends PostgresIntegrationTestSupport {
    private static final String WASH_PROGRAM_ID = "95000000-0000-0000-0000-000000000002";

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;
    @Autowired UserAccountRepository users;
    @Autowired PasswordEncoder passwordEncoder;

    @Test
    void completedCycleChangesMetricsAndReporterCanReadThem() throws Exception {
        String admin = login(Role.ADMIN);
        OffsetDateTime from = OffsetDateTime.now().minusMinutes(1);
        OffsetDateTime to = OffsetDateTime.now().plusMinutes(10);
        JsonNode baseline = metrics(admin, from, to).path("data");

        String machineId = createMachine(admin);
        String orderId = createClassifiedOrder(admin);
        saveProfile(admin, orderId);
        JsonNode cycle = createCycle(admin, machineId, orderId);
        String cycleId = cycle.path("data").path("id").asText();
        action(admin, cycleId, "start", Map.of("observation", "Inicio medido"));
        action(admin, cycleId, "complete", Map.of(
                "actualWeightGrams", 2500,
                "observation", "Fin medido"));

        String reporter = login(Role.REPORT_VIEWER);
        JsonNode measured = metrics(reporter, from, to).path("data");
        assertThat(measured.path("totalCycles").asLong())
                .isEqualTo(baseline.path("totalCycles").asLong() + 1);
        assertThat(measured.path("completedCycles").asLong())
                .isEqualTo(baseline.path("completedCycles").asLong() + 1);
        assertThat(measured.path("completedWashCycles").asLong())
                .isEqualTo(baseline.path("completedWashCycles").asLong() + 1);
        assertThat(measured.path("assignedOrders").asLong())
                .isEqualTo(baseline.path("assignedOrders").asLong() + 1);
        assertThat(measured.path("plannedWeightGrams").asLong())
                .isEqualTo(baseline.path("plannedWeightGrams").asLong() + 2500);
        assertThat(measured.path("actualWeightGrams").asLong())
                .isEqualTo(baseline.path("actualWeightGrams").asLong() + 2500);
        assertThat(measured.path("averageDurationMinutes").isNumber()).isTrue();
        assertThat(measured.path("completionRatePercent").isNumber()).isTrue();
        assertThat(measured.path("separationReadyPercent").decimalValue())
                .isEqualByComparingTo("100");
    }

    @Test
    void invalidRangeIsRejectedAndAnonymousAccessIsDenied() throws Exception {
        String token = login(Role.ADMIN);
        OffsetDateTime now = OffsetDateTime.now();
        String invalid = query(now, now.minusHours(1));
        mockMvc.perform(get("/production/metrics" + invalid)
                        .header("Authorization", bearer(token)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_METRICS_RANGE"));

        String tooLarge = query(now.minusDays(367), now);
        mockMvc.perform(get("/production/metrics" + tooLarge)
                        .header("Authorization", bearer(token)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("METRICS_RANGE_TOO_LARGE"));

        mockMvc.perform(get("/production/metrics"))
                .andExpect(status().is4xxClientError());
    }

    private JsonNode metrics(String token, OffsetDateTime from, OffsetDateTime to) throws Exception {
        return performJson(get("/production/metrics" + query(from, to))
                .header("Authorization", bearer(token)));
    }

    private String query(OffsetDateTime from, OffsetDateTime to) {
        return "?from=" + encode(from.toString()) + "&to=" + encode(to.toString());
    }

    private String encode(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }

    private String createMachine(String token) throws Exception {
        JsonNode response = performJson(post("/production/machines")
                .header("Authorization", bearer(token))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of(
                        "code", "METRICS_" + UUID.randomUUID().toString().substring(0, 8),
                        "name", "Lavadora de métricas",
                        "machineType", "WASHER",
                        "capacityGrams", 10000,
                        "status", "ACTIVE",
                        "active", true))));
        return response.path("data").path("id").asText();
    }

    private JsonNode createCycle(String token, String machineId, String orderId) throws Exception {
        return performJson(post("/production/cycles")
                .header("Authorization", bearer(token))
                .header("Idempotency-Key", "production-metrics-" + UUID.randomUUID())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of(
                        "machineId", machineId,
                        "programId", WASH_PROGRAM_ID,
                        "orderIds", List.of(orderId)))));
    }

    private JsonNode action(String token, String cycleId, String action, Map<String, Object> body)
            throws Exception {
        return performJson(post("/production/cycles/{id}/{action}", cycleId, action)
                .header("Authorization", bearer(token))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(body)));
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

    private String createClassifiedOrder(String token) throws Exception {
        JsonNode client = createClient(token);
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
                .header("Idempotency-Key", "metrics-reception-" + UUID.randomUUID())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of(
                        "actualWeightGrams", 2500,
                        "items", List.of(Map.of(
                                "equivalenceCode", "TSHIRT",
                                "actualPhysicalPieces", 2,
                                "damageDetected", false,
                                "stainDetected", false))))));
        return orderId;
    }

    private JsonNode createClient(String token) throws Exception {
        String suffix = UUID.randomUUID().toString().replace("-", "").substring(0, 10);
        return performJson(post("/clients")
                .header("Authorization", bearer(token))
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                          "firstName":"Métricas",
                          "lastName":"Productivas",
                          "phone":"1122%s",
                          "whatsapp":"1166%s",
                          "addresses":[{
                            "zoneCode":"MARCOS_PAZ",
                            "street":"Mitre",
                            "number":"123",
                            "locality":"Marcos Paz",
                            "primaryAddress":true
                          }]
                        }
                        """.formatted(suffix, suffix)));
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
        String username = role.name().toLowerCase() + "-metrics-" + UUID.randomUUID();
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
