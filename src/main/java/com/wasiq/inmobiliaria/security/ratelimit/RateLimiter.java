package com.wasiq.inmobiliaria.security.ratelimit;

import org.springframework.stereotype.Component;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * In-memory fixed-window rate limiter.
 * State lives in this JVM only: with several instances, move it to a shared store (e.g. Redis + Bucket4j).
 */
@Component
public class RateLimiter {

    private static final int PURGE_THRESHOLD = 10_000;
    private static final Duration PURGE_INTERVAL = Duration.ofSeconds(30);

    private final Map<String, Window> windows = new ConcurrentHashMap<>();
    private final Clock clock;
    private volatile Instant nextPurge;

    public RateLimiter() {
        this(Clock.systemUTC());
    }

    public RateLimiter(Clock clock) {
        this.clock = clock;
        this.nextPurge = clock.instant();
    }

    //* Consume one unit for the key; returns false when the current window is already exhausted
    public boolean tryConsume(String key, int limit, Duration window) {
        Instant now = clock.instant();
        purgeExpiredIfNeeded(now);

        boolean[] allowed = {false};
        windows.compute(key, (k, current) -> {
            Window active = (current == null || current.isExpired(now))
                    ? new Window(now.plus(window), 0)
                    : current;
            if (active.count() < limit) {
                allowed[0] = true;
                return new Window(active.expiresAt(), active.count() + 1);
            }
            return active;
        });
        return allowed[0];
    }

    public boolean isLimited(String key, int limit) {
        Window current = windows.get(key);
        return current != null && !current.isExpired(clock.instant()) && current.count() >= limit;
    }

    public void reset(String key) {
        windows.remove(key);
    }

    //* Bound memory growth without scanning the map on every request
    private void purgeExpiredIfNeeded(Instant now) {
        if (windows.size() > PURGE_THRESHOLD && now.isAfter(nextPurge)) {
            nextPurge = now.plus(PURGE_INTERVAL);
            windows.values().removeIf(window -> window.isExpired(now));
        }
    }

    private record Window(Instant expiresAt, int count) {
        boolean isExpired(Instant now) {
            return !now.isBefore(expiresAt);
        }
    }
}
