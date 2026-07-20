package com.fourbubbles.ropalista.auth.application;

import com.fourbubbles.ropalista.auth.api.AuthResponse;
import com.fourbubbles.ropalista.auth.api.LoginRequest;
import com.fourbubbles.ropalista.auth.domain.RefreshToken;
import com.fourbubbles.ropalista.auth.domain.UserAccount;
import com.fourbubbles.ropalista.auth.infrastructure.RefreshTokenRepository;
import com.fourbubbles.ropalista.auth.infrastructure.UserAccountRepository;
import com.fourbubbles.ropalista.common.application.BusinessRuleException;
import com.fourbubbles.ropalista.common.security.JwtProperties;
import com.fourbubbles.ropalista.common.security.JwtService;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Instant;
import java.util.Base64;
import java.util.HexFormat;

@Service
public class AuthService {
    private final AuthenticationManager authenticationManager;
    private final UserAccountRepository users;
    private final RefreshTokenRepository refreshTokens;
    private final JwtService jwtService;
    private final JwtProperties jwtProperties;
    private final SecureRandom secureRandom = new SecureRandom();

    public AuthService(AuthenticationManager authenticationManager, UserAccountRepository users,
                       RefreshTokenRepository refreshTokens, JwtService jwtService, JwtProperties jwtProperties) {
        this.authenticationManager = authenticationManager;
        this.users = users;
        this.refreshTokens = refreshTokens;
        this.jwtService = jwtService;
        this.jwtProperties = jwtProperties;
    }

    @Transactional
    public AuthResponse login(LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.email(), request.password()));
        UserAccount user = users.findByEmailIgnoreCaseAndDeletedAtIsNull(request.email())
                .orElseThrow();
        return issue(user);
    }

    @Transactional
    public AuthResponse refresh(String rawToken) {
        RefreshToken stored = refreshTokens.findByTokenHashAndDeletedAtIsNull(hash(rawToken))
                .filter(token -> token.isUsable(Instant.now()))
                .orElseThrow(() -> new BusinessRuleException("INVALID_REFRESH_TOKEN",
                        "El refresh token no es válido o expiró"));
        stored.setRevokedAt(Instant.now());
        return issue(stored.getUser());
    }

    private AuthResponse issue(UserAccount user) {
        String access = jwtService.createAccessToken(user);
        String rawRefresh = randomToken();
        RefreshToken entity = new RefreshToken();
        entity.setUser(user);
        entity.setTokenHash(hash(rawRefresh));
        entity.setExpiresAt(Instant.now().plus(jwtProperties.refreshTtl()));
        refreshTokens.save(entity);
        return new AuthResponse(access, rawRefresh, Instant.now().plus(jwtProperties.accessTtl()),
                user.getId(), user.getDisplayName(), user.getRole().name());
    }

    private String randomToken() {
        byte[] bytes = new byte[64];
        secureRandom.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    private String hash(String value) {
        try {
            return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256")
                    .digest(value.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException(ex);
        }
    }
}
