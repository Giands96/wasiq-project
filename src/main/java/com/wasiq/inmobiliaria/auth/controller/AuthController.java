package com.wasiq.inmobiliaria.auth.controller;



import com.wasiq.inmobiliaria.auth.dto.AuthResponse;
import com.wasiq.inmobiliaria.auth.dto.LoginRequest;
import com.wasiq.inmobiliaria.auth.dto.RegisterRequest;
import com.wasiq.inmobiliaria.auth.dto.UpdateUserRequest;
import com.wasiq.inmobiliaria.auth.service.AuthService;
import com.wasiq.inmobiliaria.shared.jwt.JwtService;
import com.wasiq.inmobiliaria.user.model.User;
import com.wasiq.inmobiliaria.user.model.enums.Role;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Duration;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private static final String ACCESS_TOKEN_COOKIE = "auth-token";
    private static final String REFRESH_TOKEN_COOKIE = "refresh-token";
    private static final Duration ACCESS_TOKEN_MAX_AGE = Duration.ofMinutes(15);
    private static final Duration REFRESH_TOKEN_MAX_AGE = Duration.ofDays(30);

    private final AuthService authService;
    private final JwtService jwtService;

    @PostMapping("/refresh")
    public ResponseEntity<AuthResponse> refreshToken(
            @CookieValue(name = REFRESH_TOKEN_COOKIE, required = false) String refreshToken) {
        AuthResponse response = authService.refreshToken(refreshToken);
        User user = authService.getUserByEmail(response.getUser().getEmail());
        return ResponseEntity.ok().headers(buildAuthCookies(user)).body(response);
    }

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@RequestBody RegisterRequest request) {
        AuthResponse authResponse = authService.register(request);
        User user = authService.getUserByEmail(request.getEmail());

        return ResponseEntity.ok()
                .headers(buildAuthCookies(user))
                .body(authResponse);
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody LoginRequest request) {
        AuthResponse authResponse = authService.authenticate(request);
        User user = authService.getUserByEmail(request.getEmail());

        return ResponseEntity.ok()
                .headers(buildAuthCookies(user))
                .body(authResponse);
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout() {
        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.SET_COOKIE, deleteCookie(ACCESS_TOKEN_COOKIE).toString());
        headers.add(HttpHeaders.SET_COOKIE, deleteCookie(REFRESH_TOKEN_COOKIE).toString());

        return ResponseEntity.ok()
                .headers(headers)
                .build();
    }

    @PostMapping("/profile/update/{id}")
    @PreAuthorize("hasAnyAuthority('USER', 'ADMIN')")
    public ResponseEntity<AuthResponse> updateUser(@PathVariable Long id, @RequestBody UpdateUserRequest user,
                                                   @AuthenticationPrincipal User currentUser) {
        if (!id.equals(currentUser.getId()) && !currentUser.getRole().equals(Role.ADMIN)) {
            return ResponseEntity.status(403).build();
        }

        AuthResponse authResponse = authService.updateUser(id, user);
        User updatedUser = authService.getUserByEmail(currentUser.getUsername());

        return ResponseEntity.ok()
                .headers(buildAuthCookies(updatedUser))
                .body(authResponse);
    }

    //*

    private HttpHeaders buildAuthCookies(UserDetails user) {
        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.SET_COOKIE, accessTokenCookie(jwtService.generateToken(user)).toString());
        headers.add(HttpHeaders.SET_COOKIE, refreshTokenCookie(jwtService.generateRefreshToken(user)).toString());
        return headers;
    }


    private ResponseCookie accessTokenCookie(String token) {
        return ResponseCookie.from(ACCESS_TOKEN_COOKIE, token)
                .httpOnly(true)
                .secure(true)
                .sameSite("None")
                .path("/")
                .maxAge(ACCESS_TOKEN_MAX_AGE)
                .build();
    }

    private ResponseCookie refreshTokenCookie(String token) {
        return ResponseCookie.from(REFRESH_TOKEN_COOKIE, token)
                .httpOnly(true)
                .secure(true)
                .sameSite("None")
                .path("/auth/refresh")
                .maxAge(REFRESH_TOKEN_MAX_AGE)
                .build();
    }

    private ResponseCookie deleteCookie(String name) {
        return ResponseCookie.from(name, "")
                .httpOnly(true)
                .secure(true)
                .sameSite("None")
                .path(REFRESH_TOKEN_COOKIE.equals(name) ? "/auth/refresh" : "/")
                .maxAge(0)
                .build();
    }
}