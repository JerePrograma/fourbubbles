package com.fourbubbles.ropalista;

import com.fourbubbles.ropalista.common.security.JwtProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing(auditorAwareRef = "currentUserAuditorAware")
@EnableConfigurationProperties(JwtProperties.class)
public class RopaListaApplication {
    public static void main(String[] args) {
        SpringApplication.run(RopaListaApplication.class, args);
    }
}
