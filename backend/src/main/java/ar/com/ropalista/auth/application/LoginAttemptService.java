package ar.com.ropalista.auth.application;

import ar.com.ropalista.common.api.BusinessException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Locale;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Service
public class LoginAttemptService {
    private final ConcurrentMap<String, Attempt> attempts = new ConcurrentHashMap<>();
    private final int maxAttempts;
    private final Duration window;
    private final Duration blockDuration;
    private final Clock clock;

    @Autowired
    public LoginAttemptService(
            @Value("${app.security.login-protection.max-attempts:${LOGIN_MAX_ATTEMPTS:5}}") int maxAttempts,
            @Value("${app.security.login-protection.window:${LOGIN_ATTEMPT_WINDOW:PT15M}}") Duration window,
            @Value("${app.security.login-protection.block-duration:${LOGIN_BLOCK_DURATION:PT15M}}") Duration blockDuration) {
        this(maxAttempts, window, blockDuration, Clock.systemUTC());
    }

    LoginAttemptService(int maxAttempts, Duration window, Duration blockDuration, Clock clock) {
        if (maxAttempts < 1) {
            throw new IllegalArgumentException("maxAttempts debe ser positivo");
        }
        this.maxAttempts = maxAttempts;
        this.window = window;
        this.blockDuration = blockDuration;
        this.clock = clock;
    }

    public void assertAllowed(String username, String sourceAddress) {
        String key = key(username, sourceAddress);
        Attempt attempt = attempts.get(key);
        if (attempt == null) {
            return;
        }

        Instant now = clock.instant();
        if (attempt.blockedUntil() != null && now.isBefore(attempt.blockedUntil())) {
            throw rateLimited();
        }
        if (windowExpired(attempt, now) || blockExpired(attempt, now)) {
            attempts.remove(key, attempt);
        }
    }

    public void recordFailure(String username, String sourceAddress) {
        String key = key(username, sourceAddress);
        Instant now = clock.instant();
        attempts.compute(key, (ignored, current) -> {
            Attempt base = current;
            if (base == null || windowExpired(base, now) || blockExpired(base, now)) {
                base = new Attempt(0, now, null);
            }
            int failures = base.failures() + 1;
            Instant blockedUntil = failures >= maxAttempts ? now.plus(blockDuration) : null;
            return new Attempt(failures, base.windowStartedAt(), blockedUntil);
        });
    }

    public void recordSuccess(String username, String sourceAddress) {
        attempts.remove(key(username, sourceAddress));
    }

    private boolean windowExpired(Attempt attempt, Instant now) {
        return !now.isBefore(attempt.windowStartedAt().plus(window));
    }

    private boolean blockExpired(Attempt attempt, Instant now) {
        return attempt.blockedUntil() != null && !now.isBefore(attempt.blockedUntil());
    }

    private String key(String username, String sourceAddress) {
        String normalizedUser = username == null ? "" : username.trim().toLowerCase(Locale.ROOT);
        String normalizedSource = sourceAddress == null || sourceAddress.isBlank() ? "unknown" : sourceAddress.trim();
        return normalizedUser + '|' + normalizedSource;
    }

    private BusinessException rateLimited() {
        return new BusinessException(
                "LOGIN_RATE_LIMITED",
                "Demasiados intentos de inicio de sesión. Intente nuevamente más tarde",
                HttpStatus.TOO_MANY_REQUESTS);
    }

    private record Attempt(int failures, Instant windowStartedAt, Instant blockedUntil) {}
}
