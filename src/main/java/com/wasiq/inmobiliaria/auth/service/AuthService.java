package com.wasiq.inmobiliaria.auth.service;

import com.wasiq.inmobiliaria.auth.dto.*;
import com.wasiq.inmobiliaria.security.ratelimit.RateLimiter;
import com.wasiq.inmobiliaria.shared.exceptions.TooManyRequestsException;
import com.wasiq.inmobiliaria.shared.exceptions.UnauthorizedException;
import com.wasiq.inmobiliaria.shared.jwt.JwtService;
import com.wasiq.inmobiliaria.user.model.enums.Role;
import com.wasiq.inmobiliaria.user.model.User;
import com.wasiq.inmobiliaria.user.repository.UserRepository;
import io.jsonwebtoken.JwtException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.Duration;
import java.util.Locale;

@Service
@RequiredArgsConstructor
public class AuthService {

    private static final int MAX_LOGIN_ATTEMPTS_PER_CLIENT = 5;
    private static final int MAX_LOGIN_ATTEMPTS_PER_ACCOUNT = 20;
    private static final Duration LOGIN_ATTEMPTS_WINDOW = Duration.ofMinutes(15);
    private static final int MAX_PASSWORD_CHANGE_ATTEMPTS = 5;

    private final PasswordEncoder passwordEncoder;
    private final UserRepository userRepository;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final RateLimiter rateLimiter;

    // Metodo Register
    public AuthResponse register(RegisterRequest request) {

        User user = User.builder()
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .email(request.getEmail())
                .role(Role.USER)
                .password(passwordEncoder.encode(request.getPassword()))
                .phoneNumber(request.getPhoneNumber())
                .build();

        userRepository.save(user);
        return AuthResponse.builder()
                .user(mapToUserResponse(user))
                .build();
    }

    public AuthResponse authenticate(LoginRequest request, String clientIp) {
        String email = request.getEmail().trim().toLowerCase(Locale.ROOT);
        //* Strict limit per email+IP, so one attacker cannot lock the real user out from elsewhere
        String clientAttemptsKey = "login-attempts:" + email + ":" + clientIp;
        //* Looser limit per email, so guessing spread across many IPs is still capped
        String accountAttemptsKey = "login-attempts:" + email;

        //* Reserve the attempt atomically before checking the password; concurrent requests cannot exceed the limit
        if (!rateLimiter.tryConsume(clientAttemptsKey, MAX_LOGIN_ATTEMPTS_PER_CLIENT, LOGIN_ATTEMPTS_WINDOW)
                || !rateLimiter.tryConsume(accountAttemptsKey, MAX_LOGIN_ATTEMPTS_PER_ACCOUNT, LOGIN_ATTEMPTS_WINDOW)) {
            throw new TooManyRequestsException("Demasiados intentos fallidos, intenta nuevamente mas tarde");
        }

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail(),
                        request.getPassword()
                ));
        rateLimiter.reset(clientAttemptsKey);
        rateLimiter.reset(accountAttemptsKey);

        User user = (User) authentication.getPrincipal();
        return AuthResponse.builder()
                .user(mapToUserResponse(user))
                .build();
    }

    public AuthResponse updateUser(Long userId, UpdateUserRequest request, User actingUser) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UnauthorizedException("User not found"));

        boolean changesPassword = request.getPassword() != null && !request.getPassword().isEmpty();
        if (changesPassword) {
            verifyCurrentPassword(actingUser, request.getCurrentPassword());
        }

        if (request.getPhoneNumber() != null && !request.getPhoneNumber().isEmpty()) {
            user.setPhoneNumber(request.getPhoneNumber());
        }

        if (changesPassword) {
            user.setPassword(passwordEncoder.encode(request.getPassword()));
        }
        userRepository.save(user);
        return AuthResponse.builder()
                .user(mapToUserResponse(user))
                .build();
    }

    //* A stolen session alone must not be enough to take over the account */
    private void verifyCurrentPassword(User actingUser, String currentPassword) {
        String attemptsKey = "password-change:" + actingUser.getId();
        if (!rateLimiter.tryConsume(attemptsKey, MAX_PASSWORD_CHANGE_ATTEMPTS, LOGIN_ATTEMPTS_WINDOW)) {
            throw new TooManyRequestsException("Demasiados intentos fallidos, intenta nuevamente mas tarde");
        }
        if (currentPassword == null || currentPassword.isEmpty()
                || !passwordEncoder.matches(currentPassword, actingUser.getPassword())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "La contrasena actual es incorrecta");
        }
        rateLimiter.reset(attemptsKey);
    }

    public AuthResponse buildAuthResponse(User user) {
        return AuthResponse.builder()
                .user(mapToUserResponse(user))
                .build();
    }

    private UserResponse mapToUserResponse(User user) {
        return UserResponse.builder()
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .email(user.getEmail())
                .role(user.getRole())
                .id(user.getId())
                .active(user.getActive())
                .build();
    }

    public AuthResponse refreshToken(String refreshToken) {
        if (refreshToken == null || refreshToken.isBlank()) {
            throw new UnauthorizedException("Refresh token not found");
        }

        String userEmail;
        try {
            userEmail = jwtService.extractUsername(refreshToken);
        } catch (JwtException | IllegalArgumentException e) {
            throw new UnauthorizedException("Invalid refresh token");
        }
        User user = getUserByEmail(userEmail);

        if (!jwtService.isRefreshTokenValid(refreshToken, user)) {
            throw new UnauthorizedException("Invalid refresh token");
        }

        return buildAuthResponse(user);
    }

    public User getUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new UnauthorizedException("User not found"));
    }
}
