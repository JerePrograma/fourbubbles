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

import java.util.Map;
import java.util.Set;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class OperationalFlowIT extends PostgresIntegrationTestSupport {
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private UserAccountRepository users;
    @Autowired
    private PasswordEncoder passwordEncoder;

    @Test
    void clientOrderConfirmationAndPaymentsRemainConsistent() throws Exception {
        String token = loginAdmin();
        String whatsapp = "1177" + UUID.randomUUID().toString().replace("-", "").substring(0, 10);

        JsonNode createdClient = performJson(post("/clients")
                .header("Authorization", bearer(token))
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                          "firstName": "Ana",
                          "lastName": "Integración",
                          "phone": "1122334455",
                          "whatsapp": "%s",
                          "email": "ana.integration@example.com",
                          "acquisitionSource": "TEST",
                          "preferences": {
                            "fragrance": "suave",
                            "softenerAllowed": false,
                            "dryerAllowed": true,
                            "hypoallergenic": true,
                            "separateColors": true,
                            "specialInstructions": "No mezclar con prendas de mascotas"
                          },
                          "addresses": [{
                            "zoneCode": "MARCOS_PAZ",
                            "street": "Sarmiento",
                            "number": "123",
                            "locality": "Marcos Paz",
                            "neighborhood": "Centro",
                            "references": "Portón negro",
                            "primaryAddress": true
                          }]
                        }
                        """.formatted(whatsapp)));
        String clientId = createdClient.path("data").path("id").asText();
        String addressId = createdClient.path("data").path("addresses").get(0).path("id").asText();

        mockMvc.perform(put("/clients/{id}", clientId)
                        .header("Authorization", bearer(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "firstName": "Ana María",
                                  "lastName": "Integración",
                                  "phone": "1122334455",
                                  "whatsapp": "%s",
                                  "email": "ana.integration@example.com",
                                  "status": "ACTIVE",
                                  "acquisitionSource": "TEST",
                                  "preferences": {
                                    "fragrance": "sin perfume",
                                    "softenerAllowed": false,
                                    "dryerAllowed": true,
                                    "hypoallergenic": true,
                                    "separateColors": true,
                                    "specialInstructions": "No usar suavizante"
                                  },
                                  "notes": "Perfil actualizado por prueba integrada"
                                }
                                """.formatted(whatsapp)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.firstName").value("Ana María"))
                .andExpect(jsonPath("$.data.preferences.fragrance").value("sin perfume"))
                .andExpect(jsonPath("$.data.preferences.softenerAllowed").value(false));

        JsonNode createdOrder = performJson(post("/orders")
                .header("Authorization", bearer(token))
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                          "clientId": "%s",
                          "addressId": "%s",
                          "serviceCode": "ROPA_LISTA_12",
                          "declaredWeightGrams": 2200,
                          "exclusiveCycle": false,
                          "notes": "Pedido de integración",
                          "items": [
                            {"equivalenceCode": "TSHIRT", "physicalPieces": 4},
                            {"equivalenceCode": "PANTS", "physicalPieces": 2},
                            {"equivalenceCode": "SOCKS_3_PAIRS", "physicalPieces": 12}
                          ]
                        }
                        """.formatted(clientId, addressId)));
        String orderId = createdOrder.path("data").path("id").asText();
        String orderNumber = createdOrder.path("data").path("orderNumber").asText();

        mockMvc.perform(get("/orders")
                        .header("Authorization", bearer(token))
                        .queryParam("orderNumber", orderNumber))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.totalElements").value(1))
                .andExpect(jsonPath("$.data.content[0].orderNumber").value(orderNumber))
                .andExpect(jsonPath("$.data.content[0].clientName").value("Integración, Ana María"))
                .andExpect(jsonPath("$.data.content[0].status").value("QUOTED"));

        mockMvc.perform(post("/orders/{id}/confirm-price", orderId)
                        .header("Authorization", bearer(token)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.confirmedPrice").value(6500.00))
                .andExpect(jsonPath("$.data.status").value("WAITING_CONFIRMATION"));

        mockMvc.perform(post("/payments")
                        .header("Authorization", bearer(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "orderId", orderId,
                                "methodCode", "TRANSFER",
                                "amount", 3000,
                                "reference", "IT-PARTIAL"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.totalPaid").value(3000.00))
                .andExpect(jsonPath("$.data.remainingBalance").value(3500.00))
                .andExpect(jsonPath("$.data.orderPaymentStatus").value("PARTIAL"));

        mockMvc.perform(post("/payments")
                        .header("Authorization", bearer(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "orderId", orderId,
                                "methodCode", "CASH",
                                "amount", 3500,
                                "reference", "IT-FINAL"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.totalPaid").value(6500.00))
                .andExpect(jsonPath("$.data.remainingBalance").value(0.00))
                .andExpect(jsonPath("$.data.orderPaymentStatus").value("PAID"));
    }

    private JsonNode performJson(org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder request)
            throws Exception {
        var result = mockMvc.perform(request).andExpect(status().isOk()).andReturn();
        return objectMapper.readTree(result.getResponse().getContentAsString());
    }

    private String loginAdmin() throws Exception {
        String username = "admin-it-" + UUID.randomUUID();
        String password = "Test-password-123!";
        users.save(new UserAccount(username, passwordEncoder.encode(password), Set.of(Role.ADMIN)));
        JsonNode login = performJson(post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of("username", username, "password", password))));
        return login.path("data").path("accessToken").asText();
    }

    private String bearer(String token) {
        return "Bearer " + token;
    }
}
