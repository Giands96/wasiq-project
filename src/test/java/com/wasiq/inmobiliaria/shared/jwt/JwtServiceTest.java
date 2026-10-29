package com.wasiq.inmobiliaria.shared.jwt;

import com.wasiq.inmobiliaria.user.model.User;
import com.wasiq.inmobiliaria.user.model.enums.Role;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class JwtServiceTest {

    static final String SECRET = Base64.getEncoder()
            .encodeToString("test-secret-key-with-at-least-32-bytes!!".getBytes(StandardCharsets.UTF_8));
    static final long ACCESS_EXPIRATION = 900_000L;
    static final long REFRESH_EXPIRATION = 2_592_000_000L;

    private JwtService jwtService;

    static JwtService newJwtService() {
        JwtService service = new JwtService();
        ReflectionTestUtils.setField(service, "jwtSecret", SECRET);
        ReflectionTestUtils.setField(service, "jwtExpiration", ACCESS_EXPIRATION);
        ReflectionTestUtils.setField(service, "refreshExpiration", REFRESH_EXPIRATION);
        return service;
    }

    static User user(boolean active) {
        return User.builder()
                .id(1L)
                .email("user@test.com")
                .password("hash")
                .role(Role.USER)
                .active(active)
                .build();
    }

    @BeforeEach
    void setUp() {
        jwtService = newJwtService();
    }

    @Test
    void accessTokenIsAcceptedAsAccessToken() {
        User user = user(true);
        String token = jwtService.generateToken(user);

        assertTrue(jwtService.isAccessTokenValid(token, user));
    }

    @Test
    void refreshTokenIsAcceptedAsRefreshToken() {
        User user = user(true);
        String token = jwtService.generateRefreshToken(user);

        assertTrue(jwtService.isRefreshTokenValid(token, user));
    }

    @Test
    void refreshTokenIsRejectedAsAccessToken() {
        User user = user(true);
        String refreshToken = jwtService.generateRefreshToken(user);

        assertFalse(jwtService.isAccessTokenValid(refreshToken, user));
    }

    @Test
    void accessTokenIsRejectedAsRefreshToken() {
        User user = user(true);
        String accessToken = jwtService.generateToken(user);

        assertFalse(jwtService.isRefreshTokenValid(accessToken, user));
    }

    @Test
    void tokensOfDisabledUserAreRejected() {
        User user = user(true);
        String accessToken = jwtService.generateToken(user);
        String refreshToken = jwtService.generateRefreshToken(user);
        user.setActive(false);

        assertFalse(jwtService.isAccessTokenValid(accessToken, user));
        assertFalse(jwtService.isRefreshTokenValid(refreshToken, user));
    }

    @Test
    void tokenWithoutTypeClaimIsRejected() {
        User user = user(true);
        String legacyToken = Jwts.builder()
                .subject(user.getUsername())
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + ACCESS_EXPIRATION))
                .signWith(jwtService.getSigningKey())
                .compact();

        assertFalse(jwtService.isAccessTokenValid(legacyToken, user));
        assertFalse(jwtService.isRefreshTokenValid(legacyToken, user));
    }

    @Test
    void tokensIssuedBeforePasswordChangeAreRejected() {
        User user = user(true);
        String accessToken = jwtService.generateToken(user);
        String refreshToken = jwtService.generateRefreshToken(user);
        user.setPassword("new-hash");

        assertFalse(jwtService.isAccessTokenValid(accessToken, user));
        assertFalse(jwtService.isRefreshTokenValid(refreshToken, user));
        assertTrue(jwtService.isAccessTokenValid(jwtService.generateToken(user), user));
    }

    @Test
    void tokenWithoutPasswordFingerprintIsRejected() {
        User user = user(true);
        String tokenFromPreviousRelease = Jwts.builder()
                .subject(user.getUsername())
                .claim("typ", "access")
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + ACCESS_EXPIRATION))
                .signWith(jwtService.getSigningKey())
                .compact();

        assertFalse(jwtService.isAccessTokenValid(tokenFromPreviousRelease, user));
    }

    @Test
    void tokenDoesNotContainPasswordHash() {
        User user = user(true);
        String token = jwtService.generateToken(user);
        String payload = new String(Base64.getUrlDecoder().decode(token.split("\\.")[1]), StandardCharsets.UTF_8);

        assertFalse(payload.contains(user.getPassword()), payload);
    }

    @Test
    void accessTokenLifetimeMatchesConfiguredExpiration() {
        String token = jwtService.generateToken(user(true));

        Date issuedAt = jwtService.extractClaim(token, Claims::getIssuedAt);
        Date expiration = jwtService.extractClaim(token, Claims::getExpiration);

        assertEquals(ACCESS_EXPIRATION, expiration.getTime() - issuedAt.getTime(), 1_000L);
    }
}
