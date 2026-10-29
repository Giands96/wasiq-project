package com.wasiq.inmobiliaria.auth.service;

import com.wasiq.inmobiliaria.auth.dto.AuthResponse;
import com.wasiq.inmobiliaria.auth.dto.LoginRequest;
import com.wasiq.inmobiliaria.security.ratelimit.RateLimiter;
import com.wasiq.inmobiliaria.shared.exceptions.TooManyRequestsException;
import com.wasiq.inmobiliaria.shared.exceptions.UnauthorizedException;
import com.wasiq.inmobiliaria.shared.jwt.JwtService;
import com.wasiq.inmobiliaria.user.model.User;
import com.wasiq.inmobiliaria.user.model.enums.Role;
import com.wasiq.inmobiliaria.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AuthServiceTest {

    private static final String EMAIL = "user@test.com";
    private static final String IP = "1.1.1.1";
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
                jwtService, new RateLimiter());

        user = User.builder().id(1L).email(EMAIL).password("hash").role(Role.USER).active(true).build();
        when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.of(user));
    }

    private LoginRequest login() {
        return LoginRequest.builder().email(EMAIL).password("wrong-password").build();
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

    @Test
    void successfulLoginReturnsUser() {
        when(authenticationManager.authenticate(any()))
                .thenReturn(new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities()));

        AuthResponse response = authService.authenticate(login(), IP);

        assertEquals(EMAIL, response.getUser().getEmail());
    }

    @Test
    void loginIsBlockedAfterRepeatedFailures() {
        when(authenticationManager.authenticate(any())).thenThrow(new BadCredentialsException("bad"));

        for (int i = 0; i < 5; i++) {
            assertThrows(BadCredentialsException.class, () -> authService.authenticate(login(), IP));
        }

        LoginRequest differentCase = LoginRequest.builder().email("  USER@test.com ").password("x").build();
        assertThrows(TooManyRequestsException.class, () -> authService.authenticate(differentCase, IP));
        verify(authenticationManager, times(5)).authenticate(any());
    }

    @Test
    void failuresFromOneIpDoNotLockOutOtherIps() {
        when(authenticationManager.authenticate(any()))
                .thenThrow(new BadCredentialsException("bad"), new BadCredentialsException("bad"),
                        new BadCredentialsException("bad"), new BadCredentialsException("bad"),
                        new BadCredentialsException("bad"))
                .thenReturn(new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities()));

        for (int i = 0; i < 5; i++) {
            assertThrows(BadCredentialsException.class, () -> authService.authenticate(login(), "6.6.6.6"));
        }

        AuthResponse response = authService.authenticate(login(), IP);
        assertEquals(EMAIL, response.getUser().getEmail());
    }

    @Test
    void accountIsBlockedWhenFailuresComeFromManyIps() {
        when(authenticationManager.authenticate(any())).thenThrow(new BadCredentialsException("bad"));

        for (int i = 0; i < 20; i++) {
            String ip = "10.0.0." + (i / 5);
            assertThrows(BadCredentialsException.class, () -> authService.authenticate(login(), ip));
        }

        assertThrows(TooManyRequestsException.class, () -> authService.authenticate(login(), "10.0.0.99"));
        verify(authenticationManager, times(20)).authenticate(any());
    }

    @Test
    void concurrentAttemptsCannotExceedLimit() throws Exception {
        AtomicInteger passwordChecks = new AtomicInteger();
        CountDownLatch release = new CountDownLatch(1);
        AuthenticationManager slowManager = authentication -> {
            passwordChecks.incrementAndGet();
            try {
                release.await(5, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            throw new BadCredentialsException("bad");
        };
        AuthService concurrentService = new AuthService(mock(PasswordEncoder.class), userRepository, slowManager,
                jwtService, new RateLimiter());

        int attempts = 20;
        ExecutorService pool = Executors.newFixedThreadPool(attempts);
        try {
            List<Future<?>> futures = new ArrayList<>();
            for (int i = 0; i < attempts; i++) {
                futures.add(pool.submit(() -> {
                    try {
                        concurrentService.authenticate(login(), IP);
                    } catch (BadCredentialsException | TooManyRequestsException expected) {
                        // both outcomes are valid; only the number of password checks matters
                    }
                }));
            }
            Thread.sleep(200);
            release.countDown();
            for (Future<?> future : futures) {
                future.get(5, TimeUnit.SECONDS);
            }
        } finally {
            pool.shutdownNow();
        }

        assertEquals(5, passwordChecks.get());
    }

    @Test
    void successfulLoginResetsFailureCount() {
        when(authenticationManager.authenticate(any()))
                .thenThrow(new BadCredentialsException("bad"), new BadCredentialsException("bad"),
                        new BadCredentialsException("bad"), new BadCredentialsException("bad"))
                .thenReturn(new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities()))
                .thenThrow(new BadCredentialsException("bad"));

        for (int i = 0; i < 4; i++) {
            assertThrows(BadCredentialsException.class, () -> authService.authenticate(login(), IP));
        }
        authService.authenticate(login(), IP);

        for (int i = 0; i < 4; i++) {
            assertThrows(BadCredentialsException.class, () -> authService.authenticate(login(), IP));
        }
        verify(authenticationManager, times(9)).authenticate(any());
    }
}
