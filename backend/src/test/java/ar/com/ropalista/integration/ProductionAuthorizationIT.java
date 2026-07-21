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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ProductionAuthorizationIT extends PostgresIntegrationTestSupport {
    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;
    @Autowired UserAccountRepository users;
    @Autowired PasswordEncoder passwordEncoder;

    @Test
    void reporterCanReadButCannotOperateProduction() throws Exception {
        String token = login(Role.REPORT_VIEWER);
        mockMvc.perform(get("/production/machines").header("Authorization", bearer(token)))
                .andExpect(status().isOk());
        mockMvc.perform(get("/production/programs").header("Authorization", bearer(token)))
                .andExpect(status().isOk());
        mockMvc.perform(post("/production/cycles")
                        .header("Authorization", bearer(token))
                        .header("Idempotency-Key", "authorization-cycle-key")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "machineId", UUID.randomUUID(),
                                "programId", UUID.randomUUID(),
                                "orderIds", java.util.List.of(UUID.randomUUID())))))
                .andExpect(status().isForbidden());
    }

    @Test
    void operatorCannotCreateMachineAndDriverCannotDecideQuality() throws Exception {
        String operator = login(Role.OPERATOR);
        mockMvc.perform(post("/production/machines")
                        .header("Authorization", bearer(operator))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "code", "AUTH_MACHINE",
                                "name", "Máquina autorización",
                                "machineType", "WASHER",
                                "capacityGrams", 5000,
                                "status", "ACTIVE",
                                "active", true))))
                .andExpect(status().isForbidden());

        String driver = login(Role.DRIVER);
        mockMvc.perform(patch("/production/orders/{id}/quality-control", UUID.randomUUID())
                        .header("Authorization", bearer(driver))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "decision", "PASS",
                                "observation", "Payload válido"))))
                .andExpect(status().isForbidden());
    }

    private String login(Role role) throws Exception {
        String username = role.name().toLowerCase() + "-production-auth-" + UUID.randomUUID();
        String password = "Test-password-123!";
        users.save(new UserAccount(username, passwordEncoder.encode(password), Set.of(role)));
        var result = mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "username", username,
                                "password", password))))
                .andExpect(status().isOk())
                .andReturn();
        JsonNode body = objectMapper.readTree(result.getResponse().getContentAsString());
        return body.path("data").path("accessToken").asText();
    }

    private String bearer(String token) {
        return "Bearer " + token;
    }
}
