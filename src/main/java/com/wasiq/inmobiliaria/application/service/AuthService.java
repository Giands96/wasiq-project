package com.wasiq.inmobiliaria.application.service;

import com.wasiq.inmobiliaria.application.ports.input.LoginUserUseCase;
import com.wasiq.inmobiliaria.application.ports.input.command.LoginUserCommand;
import com.wasiq.inmobiliaria.domain.model.user.User;
import com.wasiq.inmobiliaria.domain.repository.UserRepositoryPort;
import com.wasiq.inmobiliaria.infraestructure.configuration.JwtUtil;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.stereotype.Service;

@Service
@Transactional(readOnly = true)
public class AuthService implements LoginUserUseCase {

    private final UserRepositoryPort userRepositoryPort;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    public AuthService(UserRepositoryPort userRepositoryPort, PasswordEncoder passwordEncoder, JwtUtil jwtUtil) {
        this.userRepositoryPort = userRepositoryPort;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
    }
    @Override
    //? LoginUserCommand -> DTO
    public String login(LoginUserCommand command) {

        // * Buscar el email

        User user = userRepositoryPort.findByEmail(command.getEmail())
                .orElseThrow(() -> new RuntimeException("Credenciales inválidas"));

        // * Validación Login con Google

        boolean isAuthenticated = false;

        // si el googleId del dto es diferente de nulo y no está vacío
        if (command.getGoogleId() != null && !command.getGoogleId().isBlank()) {
            // si el googleId del usuario es diferente de nulo y es igual al googleId del dto
            if (user.getGoogleId() != null && user.getGoogleId().equals(command.getGoogleId())) {
                isAuthenticated = true;
            }
        } else if (command.getPassword() != null && !command.getPassword().isBlank()) { //Si el password del dto es diferente de nulo y no está vacío
            if (passwordEncoder.matches(command.getPassword(), user.getPassword())) { // Si el password del dto coincide con el password del usuario
                isAuthenticated = true;
            }
        } // sino es falso

        if(!isAuthenticated){
            throw new RuntimeException("Credenciales inválidas");
        }


        return jwtUtil.generateToken(user);

    }

}

