package com.wasiq.inmobiliaria.auth.controller;

import com.wasiq.inmobiliaria.auth.dto.AuthResponse;
import com.wasiq.inmobiliaria.auth.dto.LoginRequest;
import com.wasiq.inmobiliaria.auth.dto.RegisterRequest;
import com.wasiq.inmobiliaria.auth.service.AuthService;
import com.wasiq.inmobiliaria.jwt.JwtService;
import com.wasiq.inmobiliaria.models.User;
import com.wasiq.inmobiliaria.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
    public ResponseEntity<AuthResponse> register(@RequestBody RegisterRequest request) {
        AuthResponse response = authService.register(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody  LoginRequest request) throws Exception {
        return ResponseEntity.ok(authService.authenticate(request));
    }
}
