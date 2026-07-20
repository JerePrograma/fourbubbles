package ar.com.ropalista.auth.application;

import ar.com.ropalista.common.api.BusinessException;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class LoginAttemptServiceTest {
    @Test
    void blocksAfterConfiguredFailures() {
        var clock = new MutableClock(Instant.parse("2026-07-20T12:00:00Z"));
        var service = new LoginAttemptService(3, Duration.ofMinutes(15), Duration.ofMinutes(10), clock);

        service.recordFailure("admin", "127.0.0.1");
        service.recordFailure("admin", "127.0.0.1");
        assertDoesNotThrow(() -> service.assertAllowed("admin", "127.0.0.1"));
        service.recordFailure("admin", "127.0.0.1");

        BusinessException error = assertThrows(BusinessException.class,
                () -> service.assertAllowed("admin", "127.0.0.1"));
        assertEquals("LOGIN_RATE_LIMITED", error.code());
        assertEquals(HttpStatus.TOO_MANY_REQUESTS, error.status());
    }

    @Test
    void successfulLoginClearsPreviousFailures() {
        var clock = new MutableClock(Instant.parse("2026-07-20T12:00:00Z"));
        var service = new LoginAttemptService(3, Duration.ofMinutes(15), Duration.ofMinutes(10), clock);

        service.recordFailure("admin", "127.0.0.1");
        service.recordFailure("admin", "127.0.0.1");
        service.recordSuccess("admin", "127.0.0.1");

        assertDoesNotThrow(() -> service.assertAllowed("admin", "127.0.0.1"));
        service.recordFailure("admin", "127.0.0.1");
        service.recordFailure("admin", "127.0.0.1");
        assertDoesNotThrow(() -> service.assertAllowed("admin", "127.0.0.1"));
    }

    @Test
    void expiredBlockAllowsAnotherAttempt() {
        var clock = new MutableClock(Instant.parse("2026-07-20T12:00:00Z"));
        var service = new LoginAttemptService(1, Duration.ofMinutes(15), Duration.ofMinutes(10), clock);

        service.recordFailure("admin", "127.0.0.1");
        assertThrows(BusinessException.class, () -> service.assertAllowed("admin", "127.0.0.1"));

        clock.advance(Duration.ofMinutes(11));
        assertDoesNotThrow(() -> service.assertAllowed("admin", "127.0.0.1"));
    }

    private static final class MutableClock extends Clock {
        private Instant instant;

        private MutableClock(Instant instant) {
            this.instant = instant;
        }

        void advance(Duration duration) {
            instant = instant.plus(duration);
        }

        @Override
        public ZoneId getZone() {
            return ZoneId.of("UTC");
        }

        @Override
        public Clock withZone(ZoneId zone) {
            return this;
        }

        @Override
        public Instant instant() {
            return instant;
        }
    }
}
