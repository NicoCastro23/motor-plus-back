package com.motorplus.motorplus.security;

import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Rate limiter en memoria para el endpoint de login.
 * Bloquea una IP después de MAX_ATTEMPTS intentos fallidos en WINDOW_SECONDS.
 * Los bloqueos expiran automáticamente — no requiere Redis ni dependencias externas.
 */
@Component
public class LoginRateLimiter {

    private static final int MAX_ATTEMPTS = 5;
    private static final long WINDOW_SECONDS = 600; // 10 minutos

    private final Map<String, Deque<Instant>> attempts = new ConcurrentHashMap<>();

    public boolean isBlocked(String ip) {
        Deque<Instant> timestamps = attempts.get(ip);
        if (timestamps == null) return false;
        purgeOld(timestamps);
        return timestamps.size() >= MAX_ATTEMPTS;
    }

    public void recordFailure(String ip) {
        attempts.compute(ip, (key, timestamps) -> {
            if (timestamps == null) timestamps = new ArrayDeque<>();
            purgeOld(timestamps);
            timestamps.addLast(Instant.now());
            return timestamps;
        });
    }

    public void clearFailures(String ip) {
        attempts.remove(ip);
    }

    private void purgeOld(Deque<Instant> timestamps) {
        Instant cutoff = Instant.now().minusSeconds(WINDOW_SECONDS);
        while (!timestamps.isEmpty() && timestamps.peekFirst().isBefore(cutoff)) {
            timestamps.pollFirst();
        }
    }
}
