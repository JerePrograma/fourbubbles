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
class CompatibilityFlowIT extends PostgresIntegrationTestSupport {
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private UserAccountRepository users;
    @Autowired
    private PasswordEncoder passwordEncoder;

    @Test
    void profileVersionsProduceExplainableEvaluationsAndAdminException() throws Exception {
        String admin = login(Role.ADMIN);
        String operator = login(Role.OPERATOR);
        String driver = login(Role.DRIVER);
        String firstOrder = createClassifiedOrder(admin, "first");
        String secondOrder = createClassifiedOrder(admin, "second");

        JsonNode firstProfile = saveProfile(admin, firstOrder, "LIGHT", "COTTON", 40, true, true);
        JsonNode secondProfile = saveProfile(admin, secondOrder, "LIGHT", "SYNTHETIC", 30, false, false);
        assertThat(firstProfile.path("data").path("version").asLong()).isZero();
        assertThat(secondProfile.path("data").path("version").asLong()).isZero();

        JsonNode compatible = evaluate(admin, firstOrder, secondOrder);
        JsonNode repeated = evaluate(admin, secondOrder, firstOrder);
        assertThat(compatible.path("data").path("compatible").asBoolean()).isTrue();
        assertThat(compatible.path("data").path("id").asText()).isEqualTo(repeated.path("data").path("id").asText());
        assertThat(compatible.path("data").path("recommendation").path("maxTemperatureC").asInt()).isEqualTo(30);
        assertThat(compatible.path("data").path("recommendation").path("dryerAllowed").asBoolean()).isFalse();

        mockMvc.perform(get("/orders/{id}/compatibility-profile", firstOrder)
                        .header("Authorization", bearer(driver)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.orderId").value(firstOrder));

        JsonNode updated = saveProfile(admin, secondOrder, "DARK", "SYNTHETIC", 30, false, false);
        assertThat(updated.path("data").path("version").asLong()).isEqualTo(1);

        JsonNode incompatible = evaluate(admin, firstOrder, secondOrder);
        assertThat(incompatible.path("data").path("id").asText()).isNotEqualTo(compatible.path("data").path("id").asText());
        assertThat(incompatible.path("data").path("compatible").asBoolean()).isFalse();
        assertThat(incompatible.path("data").path("reasons").toString()).contains("COLOR_GROUP_MISMATCH");
        String evaluationId = incompatible.path("data").path("id").asText();

        mockMvc.perform(post("/compatibility/evaluations/{id}/exception", evaluationId)
                        .header("Authorization", bearer(operator))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{" +
                                "\"reason\":\"Operador no autorizado\"" +
                                "}"))
                .andExpect(status().isForbidden());

        JsonNode overridden = performJson(post("/compatibility/evaluations/{id}/exception", evaluationId)
                .header("Authorization", bearer(admin))
                .contentType(MediaType.APPLICATION_JSON)
                .content("{" +
                        "\"reason\":\"Separación interna validada por administración\"" +
                        "}"));
        assertThat(overridden.path("data").path("compatible").asBoolean()).isFalse();
        assertThat(overridden.path("data").path("overridden").asBoolean()).isTrue();
        assertThat(overridden.path("data").path("effectivelyCompatible").asBoolean()).isTrue();
        assertThat(overridden.path("data").path("exception").path("authorizedBy").asText()).contains("admin");
    }

    @Test
    void profileCannotBeCreatedBeforeReceptionAndClassification() throws Exception {
        String admin = login(Role.ADMIN);
        String orderId = createQuotedOrder(admin, "unclassified");

        mockMvc.perform(put("/orders/{id}/compatibility-profile", orderId)
                        .header("Authorization", bearer(admin))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(profileBody("LIGHT", "COTTON", 40, true, true)))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.code").value("ORDER_NOT_READY_FOR_COMPATIBILITY"));
    }

    private JsonNode saveProfile(String token, String orderId, String color, String material,
                                 int temperature, boolean dryer, boolean softener) throws Exception {
        return performJson(put("/orders/{id}/compatibility-profile", orderId)
                .header("Authorization", bearer(token))
                .contentType(MediaType.APPLICATION_JSON)
                .content(profileBody(color, material, temperature, dryer, softener)));
    }

    private String profileBody(String color, String material, int temperature,
                               boolean dryer, boolean softener) throws Exception {
        return objectMapper.writeValueAsString(Map.ofEntries(
                Map.entry("colorGroup", color),
                Map.entry("materialGroup", material),
                Map.entry("maxTemperatureC", temperature),
                Map.entry("dryerAllowed", dryer),
                Map.entry("fragrancePolicy", "STANDARD"),
                Map.entry("softenerAllowed", softener),
                Map.entry("hypoallergenic", false),
                Map.entry("babyClothes", false),
                Map.entry("petContact", false),
                Map.entry("heavySoil", false),
                Map.entry("exclusiveCycle", false),
                Map.entry("notes", "Perfil de integración")));
    }

    private JsonNode evaluate(String token, String orderA, String orderB) throws Exception {
        return performJson(post("/compatibility/evaluate")
                .header("Authorization", bearer(token))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of("orderAId", orderA, "orderBId", orderB))));
    }

    private String createClassifiedOrder(String token, String suffix) throws Exception {
        String orderId = createQuotedOrder(token, suffix);
        performJson(post("/orders/{id}/confirm-price", orderId).header("Authorization", bearer(token)));
        changeStatus(token, orderId, "RESERVED");
        changeStatus(token, orderId, "PICKUP_SCHEDULED");
        changeStatus(token, orderId, "PICKED_UP");
        performJson(post("/orders/{id}/reception", orderId)
                .header("Authorization", bearer(token))
                .header("Idempotency-Key", "compat-reception-" + UUID.randomUUID())
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                          "actualWeightGrams":2600,
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

    private String createQuotedOrder(String token, String suffix) throws Exception {
        JsonNode client = createClient(token, suffix);
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
        return order.path("data").path("id").asText();
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
                          "phone":"1122%s",
                          "whatsapp":"1166%s",
                          "addresses":[{
                            "zoneCode":"MARCOS_PAZ",
                            "street":"Moreno",
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
        String username = role.name().toLowerCase() + "-compat-" + UUID.randomUUID();
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
