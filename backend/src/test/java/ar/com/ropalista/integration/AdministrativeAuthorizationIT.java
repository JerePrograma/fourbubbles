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
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AdministrativeAuthorizationIT extends PostgresIntegrationTestSupport {
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private UserAccountRepository users;
    @Autowired
    private PasswordEncoder passwordEncoder;

    @Test
    void driverInheritsReadOnlyRoleButNotAdministrativeWrites() throws Exception {
        String token = login(Role.DRIVER);

        mockMvc.perform(get("/clients").header("Authorization", bearer(token)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        String suffix = UUID.randomUUID().toString().replace("-", "").substring(0, 10);
        mockMvc.perform(post("/clients")
                        .header("Authorization", bearer(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "firstName": "Sin",
                                  "lastName": "Permiso",
                                  "phone": "1122%s",
                                  "whatsapp": "1177%s",
                                  "addresses": [{
                                    "zoneCode": "MARCOS_PAZ",
                                    "street": "Mitre",
                                    "number": "100",
                                    "locality": "Marcos Paz",
                                    "primaryAddress": true
                                  }]
                                }
                                """.formatted(suffix, suffix)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("ACCESS_DENIED"));
    }

    @Test
    void operatorCannotApplyAdministratorOnlyManualQuote() throws Exception {
        String token = login(Role.OPERATOR);

        mockMvc.perform(post("/orders/{id}/manual-quote", UUID.randomUUID())
                        .header("Authorization", bearer(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{" +
                                "\"amount\":12000," +
                                "\"reason\":\"Prueba de autorización\"" +
                                "}"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("ACCESS_DENIED"));
    }

    private String login(Role role) throws Exception {
        String username = role.name().toLowerCase() + "-hierarchy-" + UUID.randomUUID();
        String password = "Test-password-123!";
        users.save(new UserAccount(username, passwordEncoder.encode(password), Set.of(role)));
        var result = mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("username", username, "password", password))))
                .andExpect(status().isOk())
                .andReturn();
        JsonNode response = objectMapper.readTree(result.getResponse().getContentAsString());
        return response.path("data").path("accessToken").asText();
    }

    private String bearer(String token) {
        return "Bearer " + token;
    }
}
