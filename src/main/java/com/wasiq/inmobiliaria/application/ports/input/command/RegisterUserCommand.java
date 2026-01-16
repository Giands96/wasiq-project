package com.wasiq.inmobiliaria.application.ports.input.command;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class RegisterUserCommand {

    @NotBlank(message="El nombre es obligatorio")
     private String fullName;

    @NotBlank(message = "El email es obligatorio")
    @Email(message = "El email no es válido", regexp = "[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}")
    private String email;

    // Si inicia sesión con Google, el password puede ser nulo
    private String password;
    private String googleId;
    // Nullables
    private String pictureUrl;
    private String phoneNumber;

    public RegisterUserCommand(String fullName, String email, String password, String googleId) {
        this.fullName = fullName;
        this.email = email;
        this.password = password;
        this.googleId = googleId;
    }
}
