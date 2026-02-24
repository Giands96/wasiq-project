package com.wasiq.inmobiliaria.auth.service;

import com.wasiq.inmobiliaria.auth.dto.AuthResponse;
import com.wasiq.inmobiliaria.auth.dto.LoginRequest;
import com.wasiq.inmobiliaria.auth.dto.RegisterRequest;
import com.wasiq.inmobiliaria.auth.dto.UserResponse;
import com.wasiq.inmobiliaria.jwt.JwtService;
import com.wasiq.inmobiliaria.models.Role;
import com.wasiq.inmobiliaria.models.User;
import com.wasiq.inmobiliaria.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;
    private final UserRepository userRepository;
    private final AuthenticationManager authenticationManager;

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
        String token = jwtService.generateToken(user);
        return AuthResponse.builder()
                .token(token)
                .user(mapToUserResponse(user))
                .build();
    }

    public AuthResponse authenticate(LoginRequest request) throws Exception{

        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
        );

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("Bad credentials"));

        String token = jwtService.generateToken(user);
        return AuthResponse.builder().token(token).user(mapToUserResponse(user)).build();
    }

    private UserResponse mapToUserResponse(User user) {
        return UserResponse.builder()
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .email(user.getEmail())
                .role(user.getRole())
                .id(user.getId())
                .build();
    }
}
