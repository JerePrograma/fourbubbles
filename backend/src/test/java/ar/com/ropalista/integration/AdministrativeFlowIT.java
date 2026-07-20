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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AdministrativeFlowIT extends PostgresIntegrationTestSupport {
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private UserAccountRepository users;
    @Autowired
    private PasswordEncoder passwordEncoder;

    @Test
    void versionedAddressesManualQuotePaymentHistoryAndAuditWorkTogether() throws Exception {
        String token = login(Role.ADMIN);
        JsonNode client = createClient(token);
        String clientId = client.path("data").path("id").asText();
        String originalAddressId = client.path("data").path("addresses").get(0).path("id").asText();

        JsonNode withSecondAddress = performJson(post("/clients/{id}/addresses", clientId)
                .header("Authorization", bearer(token))
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                          "zoneCode": "MARIANO_ACOSTA",
                          "street": "Constitución",
                          "number": "456",
                          "locality": "Mariano Acosta",
                          "neighborhood": "Centro",
                          "references": "Casa blanca",
                          "primaryAddress": false
                        }
                        """));
        String secondAddressId = withSecondAddress.path("data").path("addresses").get(1).path("id").asText();

        mockMvc.perform(post("/clients/{id}/addresses/{addressId}/make-primary", clientId, secondAddressId)
                        .header("Authorization", bearer(token)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.addresses[0].id").value(secondAddressId))
                .andExpect(jsonPath("$.data.addresses[0].primaryAddress").value(true));

        mockMvc.perform(delete("/clients/{id}/addresses/{addressId}", clientId, originalAddressId)
                        .header("Authorization", bearer(token)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.addresses.length()").value(1))
                .andExpect(jsonPath("$.data.addressHistory.length()").value(1))
                .andExpect(jsonPath("$.data.addressHistory[0].active").value(false));

        JsonNode order = createOrder(token, clientId, secondAddressId, "COMFORTER", null,
                "COMFORTER", 1);
        String orderId = order.path("data").path("id").asText();
        assertTrue(order.path("data").path("requiresQuote").asBoolean());
        assertEquals(11000.0, order.path("data").path("automaticQuotedPrice").asDouble());

        mockMvc.perform(post("/orders/{id}/manual-quote", orderId)
                        .header("Authorization", bearer(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"amount": 14500.00, "reason": "Acolchado king con tratamiento especial"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.automaticQuotedPrice").value(11000.00))
                .andExpect(jsonPath("$.data.quotedPrice").value(14500.00))
                .andExpect(jsonPath("$.data.requiresQuote").value(false))
                .andExpect(jsonPath("$.data.manualQuoteReason").value("Acolchado king con tratamiento especial"))
                .andExpect(jsonPath("$.data.status").value("QUOTED"));

        mockMvc.perform(patch("/orders/{id}/planning", orderId)
                        .header("Authorization", bearer(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "pickupScheduledAt": "2026-07-21T10:00:00-03:00",
                                  "promisedAt": "2026-07-23T18:00:00-03:00",
                                  "notes": "Retirar por portón lateral"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.notes").value("Retirar por portón lateral"));

        mockMvc.perform(post("/orders/{id}/confirm-price", orderId)
                        .header("Authorization", bearer(token)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.confirmedPrice").value(14500.00));

        mockMvc.perform(patch("/orders/{id}/planning", orderId)
                        .header("Authorization", bearer(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.code").value("ORDER_NOT_EDITABLE"));

        mockMvc.perform(post("/payments")
                        .header("Authorization", bearer(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "orderId", orderId,
                                "methodCode", "TRANSFER",
                                "amount", 5000,
                                "reference", "ADMIN-IT-1"))))
                .andExpect(status().isOk());

        mockMvc.perform(get("/payments")
                        .header("Authorization", bearer(token))
                        .queryParam("orderId", orderId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(1))
                .andExpect(jsonPath("$.data[0].amount").value(5000.00))
                .andExpect(jsonPath("$.data[0].methodCode").value("TRANSFER"));

        mockMvc.perform(get("/audit")
                        .header("Authorization", bearer(token))
                        .queryParam("entityType", "ORDER")
                        .queryParam("entityId", orderId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.totalElements").value(org.hamcrest.Matchers.greaterThanOrEqualTo(3)));
    }

    @Test
    void onlyOneConcurrentConfirmationConsumesOnePerAddressPromotion() throws Exception {
        String token = login(Role.ADMIN);
        JsonNode client = createClient(token);
        String clientId = client.path("data").path("id").asText();
        String addressId = client.path("data").path("addresses").get(0).path("id").asText();

        String firstOrderId = createOrder(token, clientId, addressId, "SECOND_BAG", "SECOND_BAG", "TSHIRT", 2)
                .path("data").path("id").asText();
        String secondOrderId = createOrder(token, clientId, addressId, "SECOND_BAG", "SECOND_BAG", "TSHIRT", 2)
                .path("data").path("id").asText();

        var start = new CountDownLatch(1);
        var executor = Executors.newFixedThreadPool(2);
        List<Integer> statuses = Collections.synchronizedList(new ArrayList<>());
        try {
            var first = executor.submit(() -> confirmAfterSignal(start, token, firstOrderId, statuses));
            var second = executor.submit(() -> confirmAfterSignal(start, token, secondOrderId, statuses));
            start.countDown();
            first.get(30, TimeUnit.SECONDS);
            second.get(30, TimeUnit.SECONDS);
        } finally {
            executor.shutdownNow();
        }

        Collections.sort(statuses);
        assertEquals(List.of(200, 422), statuses);
    }

    private void confirmAfterSignal(CountDownLatch start, String token, String orderId, List<Integer> statuses) {
        try {
            start.await(10, TimeUnit.SECONDS);
            int status = mockMvc.perform(post("/orders/{id}/confirm-price", orderId)
                            .header("Authorization", bearer(token)))
                    .andReturn().getResponse().getStatus();
            statuses.add(status);
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    private JsonNode createClient(String token) throws Exception {
        String suffix = UUID.randomUUID().toString().replace("-", "").substring(0, 10);
        return performJson(post("/clients")
                .header("Authorization", bearer(token))
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                          "firstName": "Administración",
                          "lastName": "Integrada",
                          "phone": "1122%s",
                          "whatsapp": "1199%s",
                          "preferences": {"dryerAllowed": true, "separateColors": true},
                          "addresses": [{
                            "zoneCode": "MARCOS_PAZ",
                            "street": "Sarmiento",
                            "number": "123",
                            "locality": "Marcos Paz",
                            "primaryAddress": true
                          }]
                        }
                        """.formatted(suffix, suffix)));
    }

    private JsonNode createOrder(String token, String clientId, String addressId, String serviceCode,
                                 String promotionCode, String equivalenceCode, int pieces) throws Exception {
        var body = new java.util.LinkedHashMap<String, Object>();
        body.put("clientId", clientId);
        body.put("addressId", addressId);
        body.put("serviceCode", serviceCode);
        body.put("promotionCode", promotionCode);
        body.put("exclusiveCycle", false);
        body.put("items", List.of(Map.of("equivalenceCode", equivalenceCode, "physicalPieces", pieces)));
        return performJson(post("/orders")
                .header("Authorization", bearer(token))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(body)));
    }

    private JsonNode performJson(org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder request)
            throws Exception {
        var result = mockMvc.perform(request).andExpect(status().isOk()).andReturn();
        return objectMapper.readTree(result.getResponse().getContentAsString());
    }

    private String login(Role role) throws Exception {
        String username = role.name().toLowerCase() + "-admin-it-" + UUID.randomUUID();
        String password = "Test-password-123!";
        users.save(new UserAccount(username, passwordEncoder.encode(password), Set.of(role)));
        JsonNode login = performJson(post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of("username", username, "password", password))));
        return login.path("data").path("accessToken").asText();
    }

    private String bearer(String token) {
        return "Bearer " + token;
    }
}
