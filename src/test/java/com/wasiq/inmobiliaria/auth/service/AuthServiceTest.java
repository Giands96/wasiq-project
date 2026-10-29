package com.wasiq.inmobiliaria.auth.service;

import com.wasiq.inmobiliaria.auth.dto.AuthResponse;
import com.wasiq.inmobiliaria.shared.exceptions.UnauthorizedException;
import com.wasiq.inmobiliaria.shared.jwt.JwtService;
import com.wasiq.inmobiliaria.user.model.User;
import com.wasiq.inmobiliaria.user.model.enums.Role;
import com.wasiq.inmobiliaria.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class AuthServiceTest {

    private static final String EMAIL = "user@test.com";
    private static final String SECRET = Base64.getEncoder()
            .encodeToString("test-secret-key-with-at-least-32-bytes!!".getBytes(StandardCharsets.UTF_8));

    private UserRepository userRepository;
    private AuthenticationManager authenticationManager;
    private JwtService jwtService;
    private AuthService authService;
    private User user;

    @BeforeEach
    void setUp() {
        userRepository = mock(UserRepository.class);
        authenticationManager = mock(AuthenticationManager.class);
        jwtService = new JwtService();
        ReflectionTestUtils.setField(jwtService, "jwtSecret", SECRET);
        ReflectionTestUtils.setField(jwtService, "jwtExpiration", 900_000L);
        ReflectionTestUtils.setField(jwtService, "refreshExpiration", 2_592_000_000L);
        authService = new AuthService(mock(PasswordEncoder.class), userRepository, authenticationManager,
                jwtService);

        user = User.builder().id(1L).email(EMAIL).password("hash").role(Role.USER).active(true).build();
        when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.of(user));
    }

    @Test
    void refreshWithValidRefreshTokenReturnsUser() {
        AuthResponse response = authService.refreshToken(jwtService.generateRefreshToken(user));

        assertEquals(EMAIL, response.getUser().getEmail());
    }

    @Test
    void refreshRejectsDisabledUser() {
        String refreshToken = jwtService.generateRefreshToken(user);
        user.setActive(false);

        assertThrows(UnauthorizedException.class, () -> authService.refreshToken(refreshToken));
    }

    @Test
    void refreshRejectsAccessToken() {
        String accessToken = jwtService.generateToken(user);

        assertThrows(UnauthorizedException.class, () -> authService.refreshToken(accessToken));
    }

    @Test
    void refreshRejectsMalformedToken() {
        assertThrows(UnauthorizedException.class, () -> authService.refreshToken("not-a-jwt"));
    }
}
