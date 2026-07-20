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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ConcurrentPaymentIT extends PostgresIntegrationTestSupport {
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private UserAccountRepository users;
    @Autowired
    private PasswordEncoder passwordEncoder;

    @Test
    void concurrentPaymentsCannotExceedConfirmedBalance() throws Exception {
        String token = login();
        JsonNode client = createClient(token);
        String clientId = client.path("data").path("id").asText();
        String addressId = client.path("data").path("addresses").get(0).path("id").asText();
        String orderId = createOrder(token, clientId, addressId).path("data").path("id").asText();

        mockMvc.perform(post("/orders/{id}/confirm-price", orderId)
                        .header("Authorization", bearer(token)))
                .andExpect(status().isOk());

        var start = new CountDownLatch(1);
        var executor = Executors.newFixedThreadPool(2);
        List<Integer> statuses = Collections.synchronizedList(new ArrayList<>());
        try {
            var first = executor.submit(() -> payAfterSignal(start, token, orderId, statuses, "PAY-A"));
            var second = executor.submit(() -> payAfterSignal(start, token, orderId, statuses, "PAY-B"));
            start.countDown();
            first.get(30, TimeUnit.SECONDS);
            second.get(30, TimeUnit.SECONDS);
        } finally {
            executor.shutdownNow();
        }

        Collections.sort(statuses);
        assertEquals(List.of(200, 422), statuses);

        mockMvc.perform(get("/payments")
                        .header("Authorization", bearer(token))
                        .queryParam("orderId", orderId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(1))
                .andExpect(jsonPath("$.data[0].amount").value(5000.00));
    }

    private void payAfterSignal(CountDownLatch start, String token, String orderId,
                                List<Integer> statuses, String reference) {
        try {
            start.await(10, TimeUnit.SECONDS);
            int responseStatus = mockMvc.perform(post("/payments")
                            .header("Authorization", bearer(token))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(Map.of(
                                    "orderId", orderId,
                                    "methodCode", "TRANSFER",
                                    "amount", 5000,
                                    "reference", reference))))
                    .andReturn().getResponse().getStatus();
            statuses.add(responseStatus);
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
                          "firstName": "Pago",
                          "lastName": "Concurrente",
                          "phone": "1122%s",
                          "whatsapp": "1188%s",
                          "addresses": [{
                            "zoneCode": "MARCOS_PAZ",
                            "street": "Belgrano",
                            "number": "789",
                            "locality": "Marcos Paz",
                            "primaryAddress": true
                          }]
                        }
                        """.formatted(suffix, suffix)));
    }

    private JsonNode createOrder(String token, String clientId, String addressId) throws Exception {
        return performJson(post("/orders")
                .header("Authorization", bearer(token))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of(
                        "clientId", clientId,
                        "addressId", addressId,
                        "serviceCode", "ROPA_LISTA_12",
                        "exclusiveCycle", false,
                        "items", List.of(Map.of("equivalenceCode", "TSHIRT", "physicalPieces", 2))))));
    }

    private JsonNode performJson(org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder request)
            throws Exception {
        var result = mockMvc.perform(request).andExpect(status().isOk()).andReturn();
        return objectMapper.readTree(result.getResponse().getContentAsString());
    }

    private String login() throws Exception {
        String username = "admin-payment-" + UUID.randomUUID();
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
