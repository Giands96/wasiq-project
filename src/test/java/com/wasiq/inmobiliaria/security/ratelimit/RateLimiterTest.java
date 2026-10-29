package com.wasiq.inmobiliaria.security.ratelimit;

import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RateLimiterTest {

    static final class MutableClock extends Clock {
        private Instant now = Instant.parse("2026-01-01T00:00:00Z");

        void advance(Duration duration) {
            now = now.plus(duration);
        }

        @Override
        public ZoneId getZone() {
            return ZoneOffset.UTC;
        }

        @Override
        public Clock withZone(ZoneId zone) {
            return this;
        }

        @Override
        public Instant instant() {
            return now;
        }
    }

    private final MutableClock clock = new MutableClock();
    private final RateLimiter rateLimiter = new RateLimiter(clock);

    @Test
    void allowsUpToLimitThenRejects() {
        assertTrue(rateLimiter.tryConsume("key", 2, Duration.ofMinutes(1)));
        assertTrue(rateLimiter.tryConsume("key", 2, Duration.ofMinutes(1)));
        assertFalse(rateLimiter.tryConsume("key", 2, Duration.ofMinutes(1)));
        assertTrue(rateLimiter.isLimited("key", 2));
    }

    @Test
    void windowExpirationRestoresQuota() {
        rateLimiter.tryConsume("key", 1, Duration.ofMinutes(1));
        assertFalse(rateLimiter.tryConsume("key", 1, Duration.ofMinutes(1)));

        clock.advance(Duration.ofMinutes(1));

        assertFalse(rateLimiter.isLimited("key", 1));
        assertTrue(rateLimiter.tryConsume("key", 1, Duration.ofMinutes(1)));
    }

    @Test
    void keysAreIndependent() {
        rateLimiter.tryConsume("a", 1, Duration.ofMinutes(1));

        assertTrue(rateLimiter.tryConsume("b", 1, Duration.ofMinutes(1)));
    }

    @Test
    void resetClearsKey() {
        rateLimiter.tryConsume("key", 1, Duration.ofMinutes(1));
        rateLimiter.reset("key");

        assertTrue(rateLimiter.tryConsume("key", 1, Duration.ofMinutes(1)));
    }
}
