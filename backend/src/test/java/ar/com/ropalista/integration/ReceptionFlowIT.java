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

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ReceptionFlowIT extends PostgresIntegrationTestSupport {
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private UserAccountRepository users;
    @Autowired
    private PasswordEncoder passwordEncoder;

    @Test
    void receptionIsIdempotentAndClassifiesWhenDifferencesAreWithinTolerance() throws Exception {
        String admin = login(Role.ADMIN);
        String orderId = createPickedUpOrder(admin, "normal");
        String key = "reception-normal-" + UUID.randomUUID();
        String body = receptionBody(2, 2600, false, false, true);

        JsonNode first = performJson(post("/orders/{id}/reception", orderId)
                .header("Authorization", bearer(admin))
                .header("Idempotency-Key", key)
                .contentType(MediaType.APPLICATION_JSON)
                .content(body));
        JsonNode repeated = performJson(post("/orders/{id}/reception", orderId)
                .header("Authorization", bearer(admin))
                .header("Idempotency-Key", key)
                .contentType(MediaType.APPLICATION_JSON)
                .content(body));

        assertThat(first.path("data").path("id").asText()).isEqualTo(repeated.path("data").path("id").asText());
        assertThat(first.path("data").path("approvalStatus").asText()).isEqualTo("NOT_REQUIRED");
        assertThat(first.path("data").path("orderStatus").asText()).isEqualTo("CLASSIFIED");
        assertThat(first.path("data").path("labelCode").asText()).startsWith("RCV-");
        assertThat(first.path("data").path("evidences")).hasSize(1);

        mockMvc.perform(post("/orders/{id}/reception", orderId)
                        .header("Authorization", bearer(admin))
                        .header("Idempotency-Key", "different-key-" + UUID.randomUUID())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("ORDER_ALREADY_RECEIVED"));
    }

    @Test
    void receptionWithMaterialDifferencesWaitsForDecisionAndCanBeApproved() throws Exception {
        String admin = login(Role.ADMIN);
        String orderId = createPickedUpOrder(admin, "approval");
        String key = "reception-approval-" + UUID.randomUUID();

        JsonNode reception = performJson(post("/orders/{id}/reception", orderId)
                .header("Authorization", bearer(admin))
                .header("Idempotency-Key", key)
                .contentType(MediaType.APPLICATION_JSON)
                .content(receptionBody(1, 3100, true, true, false)));

        assertThat(reception.path("data").path("requiresCustomerApproval").asBoolean()).isTrue();
        assertThat(reception.path("data").path("approvalStatus").asText()).isEqualTo("PENDING");
        assertThat(reception.path("data").path("orderStatus").asText()).isEqualTo("WAITING_PRICE_APPROVAL");

        JsonNode approved = performJson(post("/orders/{id}/reception/decision", orderId)
                .header("Authorization", bearer(admin))
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {"decision":"APPROVED","notes":"Cliente acepta la diferencia documentada"}
                        """));

        assertThat(approved.path("data").path("approvalStatus").asText()).isEqualTo("APPROVED");
        assertThat(approved.path("data").path("orderStatus").asText()).isEqualTo("CLASSIFIED");
    }

    @Test
    void concurrentRequestsWithSameKeyCreateOnlyOneReception() throws Exception {
        String admin = login(Role.ADMIN);
        String orderId = createPickedUpOrder(admin, "concurrent");
        String key = "reception-concurrent-" + UUID.randomUUID();
        String body = receptionBody(2, 2600, false, false, false);
        var start = new CountDownLatch(1);
        var executor = Executors.newFixedThreadPool(2);
        List<Integer> statuses = Collections.synchronizedList(new ArrayList<>());
        List<String> receptionIds = Collections.synchronizedList(new ArrayList<>());
        try {
            var first = executor.submit(() -> receiveAfterSignal(start, admin, orderId, key, body, statuses, receptionIds));
            var second = executor.submit(() -> receiveAfterSignal(start, admin, orderId, key, body, statuses, receptionIds));
            start.countDown();
            first.get(30, TimeUnit.SECONDS);
            second.get(30, TimeUnit.SECONDS);
        } finally {
            executor.shutdownNow();
        }

        Collections.sort(statuses);
        assertEquals(List.of(200, 200), statuses);
        assertThat(receptionIds).hasSize(2).allMatch(receptionIds.get(0)::equals);
    }

    @Test
    void driverCanReadReceptionButCannotCreateIt() throws Exception {
        String admin = login(Role.ADMIN);
        String driver = login(Role.DRIVER);
        String orderId = createPickedUpOrder(admin, "roles");
        String key = "reception-role-" + UUID.randomUUID();
        String body = receptionBody(2, 2600, false, false, false);

        mockMvc.perform(post("/orders/{id}/reception", orderId)
                        .header("Authorization", bearer(driver))
                        .header("Idempotency-Key", key)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isForbidden());

        performJson(post("/orders/{id}/reception", orderId)
                .header("Authorization", bearer(admin))
                .header("Idempotency-Key", key)
                .contentType(MediaType.APPLICATION_JSON)
                .content(body));

        mockMvc.perform(get("/orders/{id}/reception", orderId)
                        .header("Authorization", bearer(driver)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.orderId").value(orderId));
    }

    private void receiveAfterSignal(CountDownLatch start, String token, String orderId, String key,
                                    String body, List<Integer> statuses, List<String> receptionIds) {
        try {
            start.await(10, TimeUnit.SECONDS);
            var result = mockMvc.perform(post("/orders/{id}/reception", orderId)
                            .header("Authorization", bearer(token))
                            .header("Idempotency-Key", key)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(body))
                    .andReturn().getResponse();
            statuses.add(result.getStatus());
            receptionIds.add(objectMapper.readTree(result.getContentAsString()).path("data").path("id").asText());
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    private String createPickedUpOrder(String token, String suffix) throws Exception {
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
        String orderId = order.path("data").path("id").asText();
        performJson(post("/orders/{id}/confirm-price", orderId).header("Authorization", bearer(token)));
        changeStatus(token, orderId, "RESERVED");
        changeStatus(token, orderId, "PICKUP_SCHEDULED");
        changeStatus(token, orderId, "PICKED_UP");
        return orderId;
    }

    private JsonNode createClient(String token, String label) throws Exception {
        String suffix = UUID.randomUUID().toString().replace("-", "").substring(0, 10);
        return performJson(post("/clients")
                .header("Authorization", bearer(token))
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                          "firstName": "Recepción",
                          "lastName": "%s",
                          "phone": "1122%s",
                          "whatsapp": "1199%s",
                          "addresses": [{
                            "zoneCode": "MARCOS_PAZ",
                            "street": "Rivadavia",
                            "number": "321",
                            "locality": "Marcos Paz",
                            "primaryAddress": true
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

    private String receptionBody(int actualPieces, int actualWeight, boolean damage,
                                 boolean stain, boolean evidence) throws Exception {
        Map<String, Object> item = new java.util.LinkedHashMap<>();
        item.put("equivalenceCode", "TSHIRT");
        item.put("actualPhysicalPieces", actualPieces);
        item.put("damageDetected", damage);
        item.put("stainDetected", stain);
        item.put("observations", damage ? "Costura abierta documentada" : null);
        Map<String, Object> request = new java.util.LinkedHashMap<>();
        request.put("actualWeightGrams", actualWeight);
        request.put("conditionNotes", stain ? "Mancha visible" : "Sin observaciones relevantes");
        request.put("bagCode", "BAG-" + UUID.randomUUID());
        request.put("items", List.of(item));
        if (evidence) {
            request.put("evidences", List.of(Map.of(
                    "objectKey", "receptions/" + UUID.randomUUID() + "/front.jpg",
                    "fileName", "front.jpg",
                    "contentType", "image/jpeg",
                    "sizeBytes", 1024,
                    "sha256", "a".repeat(64),
                    "caption", "Vista frontal")));
        }
        return objectMapper.writeValueAsString(request);
    }

    private JsonNode performJson(org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder request)
            throws Exception {
        var result = mockMvc.perform(request).andExpect(status().isOk()).andReturn();
        return objectMapper.readTree(result.getResponse().getContentAsString());
    }

    private String login(Role role) throws Exception {
        String username = role.name().toLowerCase() + "-reception-" + UUID.randomUUID();
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
