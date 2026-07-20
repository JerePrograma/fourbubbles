package ar.com.ropalista.integration;

import ar.com.ropalista.auth.domain.Role;
import ar.com.ropalista.auth.domain.UserAccount;
import ar.com.ropalista.auth.persistence.UserAccountRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Set;
import java.util.UUID;

import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.blankOrNullString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ApiContractIT extends PostgresIntegrationTestSupport {
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private UserAccountRepository users;
    @Autowired
    private PasswordEncoder passwordEncoder;

    @Test
    void unauthenticatedRequestsUseUniformContractAndCorrelationId() throws Exception {
        mockMvc.perform(get("/clients").header("X-Request-ID", "contract-test-0001"))
                .andExpect(status().isUnauthorized())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(header().string("X-Request-ID", "contract-test-0001"))
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.code").value("AUTHENTICATION_REQUIRED"))
                .andExpect(jsonPath("$.status").value(401));
    }

    @Test
    void unsafeCorrelationIdentifiersAreReplaced() throws Exception {
        mockMvc.perform(get("/clients").header("X-Request-ID", "not safe id"))
                .andExpect(status().isUnauthorized())
                .andExpect(header().string("X-Request-ID", not(blankOrNullString())))
                .andExpect(header().string("X-Request-ID", not("not safe id")));
    }

    @Test
    void reportViewerCannotCreateClients() throws Exception {
        String token = login(Role.REPORT_VIEWER);
        String body = """
                {
                  "firstName": "Ana",
                  "lastName": "Reporte",
                  "phone": "1122334455",
                  "whatsapp": "1199%s",
                  "preferences": {"dryerAllowed": true},
                  "addresses": [{
                    "zoneCode": "MARCOS_PAZ",
                    "street": "Sarmiento",
                    "number": "123",
                    "locality": "Marcos Paz",
                    "primaryAddress": true
                  }]
                }
                """.formatted(UUID.randomUUID().toString().substring(0, 8));

        mockMvc.perform(post("/clients")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.code").value("ACCESS_DENIED"))
                .andExpect(jsonPath("$.status").value(403));
    }

    @Test
    void invalidPayloadReturnsFieldViolations() throws Exception {
        String token = login(Role.OPERATOR);
        String body = """
                {
                  "firstName": "",
                  "lastName": "Validación",
                  "phone": "1122334455",
                  "whatsapp": "1188%s",
                  "preferences": {"dryerAllowed": true},
                  "addresses": [{
                    "zoneCode": "MARCOS_PAZ",
                    "street": "Sarmiento",
                    "number": "123",
                    "locality": "Marcos Paz",
                    "primaryAddress": true
                  }]
                }
                """.formatted(UUID.randomUUID().toString().substring(0, 8));

        mockMvc.perform(post("/clients")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.violations[?(@.field == 'firstName')]").exists());
    }

    @Test
    void invalidEnumQueryParameterReturnsBadRequest() throws Exception {
        String token = login(Role.REPORT_VIEWER);

        mockMvc.perform(get("/orders")
                        .queryParam("status", "NOT_A_REAL_STATUS")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_PARAMETER"))
                .andExpect(jsonPath("$.violations[0].field").value("status"));
    }

    private String login(Role role) throws Exception {
        String username = role.name().toLowerCase() + "-" + UUID.randomUUID();
        String password = "Test-password-123!";
        users.save(new UserAccount(username, passwordEncoder.encode(password), Set.of(role)));

        var result = mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(java.util.Map.of(
                                "username", username,
                                "password", password))))
                .andExpect(status().isOk())
                .andReturn();
        return objectMapper.readTree(result.getResponse().getContentAsString())
                .path("data").path("accessToken").asText();
    }
}
