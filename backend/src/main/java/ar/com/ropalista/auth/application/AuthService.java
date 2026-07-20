package ar.com.ropalista.auth.application;

import ar.com.ropalista.auth.domain.RefreshToken;
import ar.com.ropalista.auth.domain.UserAccount;
import ar.com.ropalista.auth.infrastructure.JwtService;
import ar.com.ropalista.auth.persistence.RefreshTokenRepository;
import ar.com.ropalista.auth.persistence.UserAccountRepository;
import ar.com.ropalista.common.api.BusinessException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.Base64;

@Service
public class AuthService {
    private final AuthenticationManager authenticationManager;
    private final UserAccountRepository users;
    private final RefreshTokenRepository refreshTokens;
    private final JwtService jwtService;
    private final Duration refreshTtl;
    private final SecureRandom secureRandom = new SecureRandom();

    public AuthService(AuthenticationManager authenticationManager,
                       UserAccountRepository users,
                       RefreshTokenRepository refreshTokens,
                       JwtService jwtService,
                       @Value("${app.security.refresh-ttl}") Duration refreshTtl) {
        this.authenticationManager = authenticationManager;
        this.users = users;
        this.refreshTokens = refreshTokens;
        this.jwtService = jwtService;
        this.refreshTtl = refreshTtl;
    }

    @Transactional
    public AuthResult login(String username, String password) {
        authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(username, password));
        UserAccount user = users.findByUsernameIgnoreCase(username).orElseThrow();
        return issueSession(user);
    }

    @Transactional
    public AuthResult refresh(String rawRefreshToken) {
        OffsetDateTime now = OffsetDateTime.now();
        RefreshToken stored = refreshTokens.findByTokenHash(hash(rawRefreshToken))
                .filter(token -> token.isUsable(now))
                .orElseThrow(() -> new BusinessException("INVALID_REFRESH_TOKEN", "La sesión no puede renovarse", HttpStatus.UNAUTHORIZED));
        stored.revoke(now);
        return issueSession(stored.getUser());
    }

    @Transactional
    public void logout(String rawRefreshToken) {
        if (rawRefreshToken == null || rawRefreshToken.isBlank()) {
            return;
        }
        refreshTokens.findByTokenHash(hash(rawRefreshToken))
                .filter(token -> token.getRevokedAt() == null)
                .ifPresent(token -> token.revoke(OffsetDateTime.now()));
    }

    private AuthResult issueSession(UserAccount user) {
        String raw = randomToken();
        refreshTokens.save(new RefreshToken(user, hash(raw), OffsetDateTime.now().plus(refreshTtl)));
        return new AuthResult(jwtService.createAccessToken(user), raw, jwtService.accessTtlSeconds(), user);
    }

    private String randomToken() {
        byte[] bytes = new byte[48];
        secureRandom.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    private String hash(String value) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256").digest(value.getBytes(StandardCharsets.UTF_8));
            return java.util.HexFormat.of().formatHex(digest);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 no disponible", e);
        }
    }

    public long refreshTtlSeconds() {
        return refreshTtl.toSeconds();
    }

    public record AuthResult(String accessToken, String refreshToken, long expiresInSeconds, UserAccount user) {}
}
