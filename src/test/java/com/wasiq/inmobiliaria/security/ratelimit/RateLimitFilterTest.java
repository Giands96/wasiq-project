package com.wasiq.inmobiliaria.security.ratelimit;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

class RateLimitFilterTest {

    private final RateLimitFilter filter = new RateLimitFilter(new RateLimiter());

    private MockHttpServletResponse send(String method, String uri, String ip) throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest(method, uri);
        request.setRemoteAddr(ip);
        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain chain = new MockFilterChain();
        filter.doFilter(request, response, chain);
        if (response.getStatus() != 429) {
            assertNotNull(chain.getRequest(), "request should reach the next filter");
        } else {
            assertNull(chain.getRequest(), "blocked request must not reach the next filter");
        }
        return response;
    }

    @Test
    void loginIsLimitedPerIp() throws Exception {
        for (int i = 0; i < 10; i++) {
            assertEquals(200, send("POST", "/auth/login", "1.1.1.1").getStatus());
        }

        MockHttpServletResponse blocked = send("POST", "/auth/login", "1.1.1.1");

        assertEquals(429, blocked.getStatus());
        assertEquals("60", blocked.getHeader("Retry-After"));
        assertEquals("application/json;charset=UTF-8", blocked.getContentType());
        assertEquals("{\"status\":429,\"message\":\"Demasiadas solicitudes, intenta nuevamente mas tarde\"}",
                blocked.getContentAsString());
        assertEquals(200, send("POST", "/auth/login", "2.2.2.2").getStatus());
    }

    @Test
    void encodedPathDoesNotBypassLimit() throws Exception {
        for (int i = 0; i < 10; i++) {
            send("POST", "/auth/login", "1.1.1.1");
        }

        assertEquals(429, send("POST", "/auth/%6Cogin", "1.1.1.1").getStatus());
    }

    @Test
    void registerIsLimitedPerIp() throws Exception {
        for (int i = 0; i < 5; i++) {
            assertEquals(200, send("POST", "/auth/register", "1.1.1.1").getStatus());
        }

        assertEquals(429, send("POST", "/auth/register", "1.1.1.1").getStatus());
    }

    @Test
    void propertyUpdateIsLimitedByPrefix() throws Exception {
        for (int i = 0; i < 30; i++) {
            send("PUT", "/properties/update/casa-" + i, "1.1.1.1");
        }

        assertEquals(429, send("PUT", "/properties/update/otra-casa", "1.1.1.1").getStatus());
    }

    @Test
    void unlistedRequestsAreNotLimited() throws Exception {
        for (int i = 0; i < 100; i++) {
            assertEquals(200, send("GET", "/properties/", "1.1.1.1").getStatus());
            assertEquals(200, send("OPTIONS", "/auth/login", "1.1.1.1").getStatus());
        }
    }
}
