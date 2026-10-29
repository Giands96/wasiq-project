package com.wasiq.inmobiliaria.security;

import com.wasiq.inmobiliaria.security.ratelimit.RateLimiter;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.cors.CorsConfiguration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SecurityConfigCorsTest {

    private CorsConfiguration corsFor(String... allowedOrigins) {
        SecurityConfig config = new SecurityConfig(null, null, new RateLimiter());
        ReflectionTestUtils.setField(config, "allowedOrigins", allowedOrigins);
        return config.corsConfigurationSource().getCorsConfiguration(new MockHttpServletRequest("GET", "/properties/"));
    }

    @Test
    void configuredOriginIsAllowedWithCredentials() {
        CorsConfiguration cors = corsFor("https://wasiq-project-front.vercel.app");

        assertEquals("https://wasiq-project-front.vercel.app", cors.checkOrigin("https://wasiq-project-front.vercel.app"));
        assertTrue(cors.getAllowCredentials());
    }

    @Test
    void lookalikeVercelSubdomainIsRejected() {
        CorsConfiguration cors = corsFor("https://wasiq-project-front.vercel.app");

        assertNull(cors.checkOrigin("https://wasiq-project-front-attacker.vercel.app"));
    }

    @Test
    void unlistedLocalhostIsRejected() {
        CorsConfiguration cors = corsFor("https://wasiq-project-front.vercel.app");

        assertNull(cors.checkOrigin("http://localhost:3000"));
    }

    @Test
    void originsAreTrimmed() {
        CorsConfiguration cors = corsFor(" https://wasiq-project-front.vercel.app ", " http://localhost:3000");

        assertEquals("http://localhost:3000", cors.checkOrigin("http://localhost:3000"));
    }
}
