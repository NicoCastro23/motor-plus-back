package com.motorplus.motorplus.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.time.Instant;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class LoginRateLimiterTest {

    private LoginRateLimiter rateLimiter;

    @BeforeEach
    void setUp() {
        rateLimiter = new LoginRateLimiter();
    }

    @Test
    void isBlocked_returnsFalse_whenNoAttempts() {
        assertThat(rateLimiter.isBlocked("192.168.1.1")).isFalse();
    }

    @Test
    void isBlocked_returnsFalse_belowMaxAttempts() {
        String ip = "10.0.0.1";
        rateLimiter.recordFailure(ip);
        rateLimiter.recordFailure(ip);
        rateLimiter.recordFailure(ip);
        rateLimiter.recordFailure(ip);

        assertThat(rateLimiter.isBlocked(ip)).isFalse();
    }

    @Test
    void isBlocked_returnsTrue_afterMaxAttempts() {
        String ip = "10.0.0.2";
        for (int i = 0; i < 5; i++) {
            rateLimiter.recordFailure(ip);
        }

        assertThat(rateLimiter.isBlocked(ip)).isTrue();
    }

    @Test
    void clearFailures_removesBlock() {
        String ip = "10.0.0.3";
        for (int i = 0; i < 5; i++) {
            rateLimiter.recordFailure(ip);
        }
        assertThat(rateLimiter.isBlocked(ip)).isTrue();

        rateLimiter.clearFailures(ip);

        assertThat(rateLimiter.isBlocked(ip)).isFalse();
    }

    @Test
    void clearFailures_onUnknownIp_doesNotThrow() {
        rateLimiter.clearFailures("99.99.99.99");
        assertThat(rateLimiter.isBlocked("99.99.99.99")).isFalse();
    }

    @Test
    void recordFailure_differentIps_trackedIndependently() {
        String ip1 = "1.1.1.1";
        String ip2 = "2.2.2.2";

        for (int i = 0; i < 5; i++) {
            rateLimiter.recordFailure(ip1);
        }
        rateLimiter.recordFailure(ip2);

        assertThat(rateLimiter.isBlocked(ip1)).isTrue();
        assertThat(rateLimiter.isBlocked(ip2)).isFalse();
    }

    @Test
    void isBlocked_returnsFalse_whenAllAttemptsExpired() throws Exception {
        String ip = "5.5.5.5";

        // Inject 5 timestamps older than the 600-second window
        Deque<Instant> oldTimestamps = new ArrayDeque<>();
        Instant expired = Instant.now().minusSeconds(700);
        for (int i = 0; i < 5; i++) {
            oldTimestamps.addLast(expired);
        }

        @SuppressWarnings("unchecked")
        Map<String, Deque<Instant>> attemptsField = (Map<String, Deque<Instant>>)
                getPrivateField(rateLimiter, "attempts");
        attemptsField.put(ip, oldTimestamps);

        assertThat(rateLimiter.isBlocked(ip)).isFalse();
    }

    @Test
    void isBlocked_returnsTrue_whenMixedFreshAndExpiredAttempts() throws Exception {
        String ip = "6.6.6.6";

        Deque<Instant> timestamps = new ArrayDeque<>();
        Instant expired = Instant.now().minusSeconds(700);
        for (int i = 0; i < 3; i++) {
            timestamps.addLast(expired);
        }

        @SuppressWarnings("unchecked")
        Map<String, Deque<Instant>> attemptsField = (Map<String, Deque<Instant>>)
                getPrivateField(rateLimiter, "attempts");
        attemptsField.put(ip, timestamps);

        // Add 5 fresh failures on top
        for (int i = 0; i < 5; i++) {
            rateLimiter.recordFailure(ip);
        }

        assertThat(rateLimiter.isBlocked(ip)).isTrue();
    }

    private Object getPrivateField(Object target, String fieldName) throws Exception {
        Field field = target.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        return field.get(target);
    }
}
