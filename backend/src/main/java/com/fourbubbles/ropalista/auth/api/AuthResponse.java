package com.fourbubbles.ropalista.auth.api;

import java.time.Instant;
import java.util.UUID;

public record AuthResponse(
        String accessToken,
        String refreshToken,
        Instant accessTokenExpiresAt,
        UUID userId,
        String displayName,
        String role
) {
}
