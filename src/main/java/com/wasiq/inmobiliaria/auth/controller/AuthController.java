package com.wasiq.inmobiliaria.auth.controller;

import com.wasiq.inmobiliaria.auth.dto.AuthResponse;
import com.wasiq.inmobiliaria.auth.dto.LoginRequest;
import com.wasiq.inmobiliaria.auth.dto.RegisterRequest;
import com.wasiq.inmobiliaria.auth.dto.UpdateUserRequest;
import com.wasiq.inmobiliaria.auth.service.AuthService;
import com.wasiq.inmobiliaria.jwt.JwtService;
import com.wasiq.inmobiliaria.models.enums.Role;
import com.wasiq.inmobiliaria.models.User;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final JwtService jwtService;

    @PostMapping("/refresh")
    public TokenResponse refreshToken(@RequestHeader(HttpHeaders.AUTHORIZATION) final String authHeader) {
        final TokenResponse token = authService.authenticate(request);
        return ResponseEntity.ok(token);
    }

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@RequestBody RegisterRequest request, HttpServletResponse response) {
        AuthResponse authResponse = authService.register(request);
        User user = authService.getUserByEmail(request.getEmail());
        String token = jwtService.generateToken(user);
        ResponseCookie springCookie = ResponseCookie.from("auth-token", token)
                .httpOnly(true)
                .secure(true) // Requerido para SameSite="None"
                .sameSite("None") // Magia real cross-domain
                .path("/")
                .maxAge(86400) // 1 día
                .build();

        // Inyectarla directamente dentro de los headers de la respuesta
        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, springCookie.toString())
                .body(authResponse);

    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody  LoginRequest request, HttpServletResponse response) throws Exception {

        AuthResponse authResponse = authService.authenticate(request);

        User user = authService.getUserByEmail(request.getEmail());
        String token = jwtService.generateToken(user);
        ResponseCookie springCookie = ResponseCookie.from("auth-token", token)
                .httpOnly(true)
                .secure(true) // Requerido para SameSite="None"
                .sameSite("None") // Magia real cross-domain
                .path("/")
                .maxAge(86400) // 1 día
                .build();

        return ResponseEntity.ok().header(HttpHeaders.SET_COOKIE, springCookie.toString()).body(authResponse);
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout() {
        ResponseCookie deleteCookie = ResponseCookie.from("auth-token", "")
                .httpOnly(true)
                .secure(true)
                .sameSite("None")
                .path("/")
                .maxAge(0) // 0 le dice al navegador que elimine la cookie inmediatamente
                .build();

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, deleteCookie.toString())
                .build();
    }

    @PostMapping("/profile/update/{id}")
    @PreAuthorize("hasAnyAuthority('USER', 'ADMIN')")
    public ResponseEntity<AuthResponse> updateUser(@PathVariable Long id, @RequestBody UpdateUserRequest user,
                                                   @AuthenticationPrincipal User currentUser, HttpServletResponse response) {
        if (!id.equals(currentUser.getId()) && !currentUser.getRole().equals(Role.ADMIN)) {
            return ResponseEntity.status(403).build();
        }

        //* Actualizar primero
        AuthResponse authResponse = authService.updateUser(id, user);

        //* Obtener usuario actualizado
        User updatedUser = authService.getUserByEmail(currentUser.getUsername());

        //* Generar token y cookie
        String token = jwtService.generateToken(updatedUser);
        Cookie cookie = new Cookie("auth-token", token);
        cookie.setHttpOnly(true);
        cookie.setSecure(true);
        cookie.setAttribute("SameSite", "None");
        cookie.setPath("/");
        cookie.setMaxAge(86400);
        response.addCookie(cookie);

        return ResponseEntity.ok(authResponse);
    }
}
