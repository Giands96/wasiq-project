package com.wasiq.inmobiliaria.auth.controller;

import com.wasiq.inmobiliaria.auth.dto.AuthResponse;
import com.wasiq.inmobiliaria.auth.dto.LoginRequest;
import com.wasiq.inmobiliaria.auth.dto.RegisterRequest;
import com.wasiq.inmobiliaria.auth.dto.UpdateUserRequest;
import com.wasiq.inmobiliaria.auth.service.AuthService;
import com.wasiq.inmobiliaria.jwt.JwtService;
import com.wasiq.inmobiliaria.models.Role;
import com.wasiq.inmobiliaria.models.User;
import com.wasiq.inmobiliaria.repository.UserRepository;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
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
    private final UserRepository userRepository;

    @PostMapping("/test")
    public String testAuth() {
        return "Auth Controller is working!";
    }

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@RequestBody RegisterRequest request, HttpServletResponse response) {
        AuthResponse authResponse = authService.register(request);
        User user = authService.getUserByEmail(request.getEmail());
        String token = jwtService.generateToken(user);
        Cookie cookie = new Cookie("auth-token", token );
        cookie.setHttpOnly(true);
        cookie.setSecure(true);     //* Solo se enviará a través de HTTPS
        cookie.setPath("/");        //* Disponible para toda la aplicación
        cookie.setMaxAge(86400);    //* 1 día de duración

        //* Agregar la cookie a la respuesta
        response.addCookie(cookie);
        return ResponseEntity.ok(authResponse);
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody  LoginRequest request, HttpServletResponse response) throws Exception {

        AuthResponse authResponse = authService.authenticate(request);

        User user = authService.getUserByEmail(request.getEmail());
        String token = jwtService.generateToken(user);

        Cookie cookie = new Cookie("auth-token", token);
        cookie.setHttpOnly(true);
        cookie.setSecure(true);     //* Solo se enviará a través de HTTPS
        cookie.setPath("/");        //* Disponible para toda la aplicación
        cookie.setMaxAge(86400);    //* 1 día de duración

        //* Agregar la cookie a la respuesta
        response.addCookie(cookie);

        return ResponseEntity.ok(authResponse);
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
        cookie.setPath("/");
        cookie.setMaxAge(86400);
        response.addCookie(cookie);

        return ResponseEntity.ok(authResponse);
    }
}
