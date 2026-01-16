package com.wasiq.inmobiliaria.application.service;

import com.wasiq.inmobiliaria.application.ports.input.RegisterUserUseCase;
import com.wasiq.inmobiliaria.application.ports.input.command.RegisterUserCommand;
import com.wasiq.inmobiliaria.domain.model.user.User;
import com.wasiq.inmobiliaria.domain.model.user.UserRole;
import com.wasiq.inmobiliaria.domain.repository.UserRepositoryPort;
import jakarta.transaction.Transactional;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@Transactional
public class UserService implements RegisterUserUseCase {

    private final UserRepositoryPort userRepositoryPort;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepositoryPort userRepositoryPort, PasswordEncoder passwordEncoder) {
        this.userRepositoryPort = userRepositoryPort;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public User registerUser(RegisterUserCommand command) {
        //! Validar si el email ya está registrado
        //! si en userRepositoryPort se encuentra un usuario con el email del comando, lanzar una excepción
        if(userRepositoryPort.findByEmail(command.getEmail()).isPresent()){
            throw new IllegalArgumentException("El email" + command.getEmail() + " ya está registrado.");
        }

        boolean hasPassword = command.getPassword() != null && !command.getPassword().isEmpty();
        boolean hasGoogleId = command.getGoogleId() != null && !command.getGoogleId().isEmpty();

        if(!hasPassword && !hasGoogleId){
            throw new IllegalArgumentException("Se debe proporcionar una contraseña o un ID de Google.");
        }

        //! Encriptar password

        String encodedPassword = null;
        if(hasPassword){
            encodedPassword = passwordEncoder.encode(command.getPassword());
        }

        // Crear usuario

        User newUser = new User(
                command.getEmail(),
                command.getGoogleId(),
                command.getFullName(),
                command.getPictureUrl(),
                command.getPhoneNumber(),
                encodedPassword,
                UserRole.BUYER,
                LocalDateTime.now(),
                true
        );

        // Retornar usuario guardado
        return userRepositoryPort.save(newUser);

    }
}
