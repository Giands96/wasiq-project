package com.wasiq.inmobiliaria.shared.jwt;

import com.wasiq.inmobiliaria.user.model.User;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetailsService;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

class JwtAuthenticationFilterTest {

    private final JwtService jwtService = JwtServiceTest.newJwtService();

    @AfterEach
    void clearContext() {
        SecurityContextHolder.clearContext();
    }

    private MockHttpServletResponse filterWithCookie(User storedUser, String token) throws Exception {
        UserDetailsService userDetailsService = username -> storedUser;
        JwtAuthenticationFilter filter = new JwtAuthenticationFilter(jwtService, userDetailsService);

        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/users/all");
        request.setCookies(new Cookie("auth-token", token));
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilter(request, response, new MockFilterChain());
        return response;
    }

    @Test
    void validAccessTokenAuthenticatesUser() throws Exception {
        User user = JwtServiceTest.user(true);

        MockHttpServletResponse response = filterWithCookie(user, jwtService.generateToken(user));

        assertEquals(200, response.getStatus());
        assertNotNull(SecurityContextHolder.getContext().getAuthentication());
    }

    @Test
    void refreshTokenInAccessCookieDoesNotAuthenticate() throws Exception {
        User user = JwtServiceTest.user(true);

        filterWithCookie(user, jwtService.generateRefreshToken(user));

        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }

    @Test
    void tokenIssuedBeforePasswordChangeDoesNotAuthenticate() throws Exception {
        User user = JwtServiceTest.user(true);
        String token = jwtService.generateToken(user);
        user.setPassword("new-hash");

        filterWithCookie(user, token);

        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }

    @Test
    void disabledUserIsNotAuthenticated() throws Exception {
        User user = JwtServiceTest.user(true);
        String token = jwtService.generateToken(user);
        user.setActive(false);

        filterWithCookie(user, token);

        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }
}
