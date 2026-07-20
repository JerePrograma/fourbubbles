package ar.com.ropalista.auth.infrastructure;

import ar.com.ropalista.auth.domain.UserAccount;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import java.util.List;

@Component
public class JwtService {
    private final SecretKey key;
    private final Duration accessTtl;

    public JwtService(
            @Value("${app.security.jwt-secret-base64}") String secretBase64,
            @Value("${app.security.access-ttl}") Duration accessTtl) {
        byte[] decoded = Decoders.BASE64.decode(secretBase64);
        if (decoded.length < 32) {
            throw new IllegalArgumentException("JWT_SECRET_BASE64 debe representar al menos 32 bytes");
        }
        this.key = Keys.hmacShaKeyFor(decoded);
        this.accessTtl = accessTtl;
    }

    public String createAccessToken(UserAccount user) {
        Instant now = Instant.now();
        List<String> roles = user.getRoles().stream().map(Enum::name).sorted().toList();
        return Jwts.builder()
                .subject(user.getUsername())
                .claim("roles", roles)
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plus(accessTtl)))
                .signWith(key)
                .compact();
    }

    public String username(String token) {
        return claims(token).getSubject();
    }

    public boolean isValid(String token, String expectedUsername) {
        Claims claims = claims(token);
        return expectedUsername.equalsIgnoreCase(claims.getSubject())
                && claims.getExpiration().after(new Date());
    }

    public long accessTtlSeconds() {
        return accessTtl.toSeconds();
    }

    private Claims claims(String token) {
        return Jwts.parser().verifyWith(key).build().parseSignedClaims(token).getPayload();
    }
}
