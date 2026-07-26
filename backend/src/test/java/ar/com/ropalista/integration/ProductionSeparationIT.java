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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ProductionSeparationIT extends PostgresIntegrationTestSupport {
    private static final String WASHER_ID = "94000000-0000-0000-0000-000000000001";
    private static final String WASH_PROGRAM_ID = "95000000-0000-0000-0000-000000000002";

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;
    @Autowired UserAccountRepository users;
    @Autowired PasswordEncoder passwordEncoder;

    @Test
    void exceptionBasedSharedCycleRequiresDistinctConfirmedContainersBeforeStart() throws Exception {
        String token = login(Role.ADMIN);
        String first = createClassifiedOrder(token, "separation-first");
        String second = createClassifiedOrder(token, "separation-second");
        saveProfile(token, first, "LIGHT");
        saveProfile(token, second, "DARK");
        String evaluationId = evaluate(token, first, second);
        authorizeException(token, evaluationId);

        JsonNode cycle = createCycle(token, first, second);
        String cycleId = cycle.path("data").path("id").asText();
        assertThat(cycle.path("data").path("orders").get(0).path("separationRequired").asBoolean()).isTrue();
        assertThat(cycle.path("data").path("orders").get(1).path("separationRequired").asBoolean()).isTrue();

        mockMvc.perform(post("/production/cycles/{id}/start", cycleId)
                        .header("Authorization", bearer(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("PRODUCTION_CYCLE_NOT_STARTABLE"));

        JsonNode firstConfirmation = confirm(token, cycleId, first, "BAG-SEPARATION-A");
        assertThat(firstConfirmation.path("data").path("containerCode").asText())
                .isEqualTo("BAG-SEPARATION-A");
        assertThat(firstConfirmation.path("data").path("confirmedBy").asText()).isNotBlank();

        JsonNode replay = confirm(token, cycleId, first, "bag-separation-a");
        assertThat(replay.path("data").path("containerCode").asText())
                .isEqualTo("BAG-SEPARATION-A");

        mockMvc.perform(put("/production/cycles/{cycleId}/separations/{orderId}", cycleId, second)
                        .header("Authorization", bearer(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("containerCode", "BAG-SEPARATION-A"))))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("SEPARATION_CONTAINER_ALREADY_USED"));

        confirm(token, cycleId, second, "BAG-SEPARATION-B");
        JsonNode list = performJson(get("/production/cycles/{cycleId}/separations", cycleId)
                .header("Authorization", bearer(token)));
        assertThat(list.path("data")).hasSize(2);
        assertThat(list.path("data").get(0).path("confirmedAt").isTextual()).isTrue();
        assertThat(list.path("data").get(1).path("confirmedAt").isTextual()).isTrue();

        JsonNode running = performJson(post("/production/cycles/{id}/start", cycleId)
                .header("Authorization", bearer(token))
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"));
        assertThat(running.path("data").path("status").asText()).isEqualTo("RUNNING");

        mockMvc.perform(put("/production/cycles/{cycleId}/separations/{orderId}", cycleId, first)
                        .header("Authorization", bearer(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("containerCode", "BAG-LATE"))))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("PRODUCTION_CYCLE_ALREADY_STARTED"));
    }

    @Test
    void driverCanReadSeparationButCannotConfirmIt() throws Exception {
        String token = login(Role.DRIVER);

        mockMvc.perform(get("/production/cycles/{cycleId}/separations", UUID.randomUUID())
                        .header("Authorization", bearer(token)))
                .andExpect(status().isNotFound());

        mockMvc.perform(put("/production/cycles/{cycleId}/separations/{orderId}",
                        UUID.randomUUID(), UUID.randomUUID())
                        .header("Authorization", bearer(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("containerCode", "BAG-AUTH"))))
                .andExpect(status().isForbidden());
    }

    private JsonNode confirm(String token, String cycleId, String orderId, String containerCode) throws Exception {
        return performJson(put("/production/cycles/{cycleId}/separations/{orderId}", cycleId, orderId)
                .header("Authorization", bearer(token))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of("containerCode", containerCode))));
    }

    private JsonNode createCycle(String token, String first, String second) throws Exception {
        return performJson(post("/production/cycles")
                .header("Authorization", bearer(token))
                .header("Idempotency-Key", "production-separation-" + UUID.randomUUID())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of(
                        "machineId", WASHER_ID,
                        "programId", WASH_PROGRAM_ID,
                        "orderIds", List.of(first, second),
                        "notes", "Separación física obligatoria"))));
    }

    private String evaluate(String token, String first, String second) throws Exception {
        JsonNode response = performJson(post("/compatibility/evaluate")
                .header("Authorization", bearer(token))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of("orderAId", first, "orderBId", second))));
        assertThat(response.path("data").path("compatible").asBoolean()).isFalse();
        return response.path("data").path("id").asText();
    }

    private void authorizeException(String token, String evaluationId) throws Exception {
        performJson(post("/compatibility/evaluations/{id}/exception", evaluationId)
                .header("Authorization", bearer(token))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of(
                        "reason", "Separación física mediante contenedores identificados"))));
    }

    private void saveProfile(String token, String orderId, String colorGroup) throws Exception {
        performJson(put("/orders/{id}/compatibility-profile", orderId)
                .header("Authorization", bearer(token))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.ofEntries(
                        Map.entry("colorGroup", colorGroup),
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
                .header("Idempotency-Key", "production-separation-reception-" + UUID.randomUUID())
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

    private JsonNode createClient(String token, String label) throws Exception {
        String suffix = UUID.randomUUID().toString().replace("-", "").substring(0, 10);
        return performJson(post("/clients")
                .header("Authorization", bearer(token))
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                          "firstName":"Separación",
                          "lastName":"%s",
                          "phone":"1133%s",
                          "whatsapp":"1177%s",
                          "addresses":[{
                            "zoneCode":"MARCOS_PAZ",
                            "street":"Sarmiento",
                            "number":"456",
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
        String username = role.name().toLowerCase() + "-production-separation-" + UUID.randomUUID();
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
