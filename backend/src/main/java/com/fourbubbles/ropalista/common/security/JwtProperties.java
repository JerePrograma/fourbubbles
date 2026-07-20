package com.fourbubbles.ropalista.common.security;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

@ConfigurationProperties(prefix = "app.jwt")
public record JwtProperties(String secretBase64, Duration accessTtl, Duration refreshTtl) {
}
