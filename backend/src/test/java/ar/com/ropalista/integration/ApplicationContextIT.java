package ar.com.ropalista.integration;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
class ApplicationContextIT extends PostgresIntegrationTestSupport {
    @Test
    void contextStartsAndFlywayMigrationsValidateJpaModel() {
        // Si el contexto inicia, Flyway aplicó las migraciones y Hibernate validó el esquema.
    }
}
