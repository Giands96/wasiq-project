package com.wasiq.inmobiliaria.security;

import com.wasiq.inmobiliaria.security.ratelimit.RateLimiter;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.filter.CorsFilter;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
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

    //* Same CorsFilter that http.cors() installs; it is what blocks cross-site form/no-cors CSRF requests */
    private MockHttpServletResponse crossOriginPost(String origin, MockFilterChain chain) throws Exception {
        SecurityConfig config = new SecurityConfig(null, null, new RateLimiter());
        ReflectionTestUtils.setField(config, "allowedOrigins", new String[]{"https://wasiq-project-front.vercel.app"});
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/properties/create");
        request.setContentType("multipart/form-data; boundary=x");
        request.addHeader("Origin", origin);
        MockHttpServletResponse response = new MockHttpServletResponse();

        new CorsFilter(config.corsConfigurationSource()).doFilter(request, response, chain);
        return response;
    }

    @Test
    void crossSiteMultipartPostIsRejectedBeforeReachingTheApp() throws Exception {
        MockFilterChain chain = new MockFilterChain();

        MockHttpServletResponse response = crossOriginPost("https://evil.example", chain);

        assertEquals(403, response.getStatus());
        assertNull(chain.getRequest());
    }

    @Test
    void opaqueNullOriginPostIsRejected() throws Exception {
        MockFilterChain chain = new MockFilterChain();

        MockHttpServletResponse response = crossOriginPost("null", chain);

        assertEquals(403, response.getStatus());
        assertNull(chain.getRequest());
    }

    @Test
    void allowedOriginPostReachesTheApp() throws Exception {
        MockFilterChain chain = new MockFilterChain();

        MockHttpServletResponse response = crossOriginPost("https://wasiq-project-front.vercel.app", chain);

        assertEquals(200, response.getStatus());
        assertNotNull(chain.getRequest());
    }

    @Test
    void originsAreTrimmed() {
        CorsConfiguration cors = corsFor(" https://wasiq-project-front.vercel.app ", " http://localhost:3000");

        assertEquals("http://localhost:3000", cors.checkOrigin("http://localhost:3000"));
    }
}
