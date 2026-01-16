package com.wasiq.inmobiliaria.application.ports.input.command;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class LoginUserCommand {

    @NotBlank
    @Email(message = "El email no es válido", regexp = "[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}")
    private String email;

    private String password;
    private String googleId;

    public LoginUserCommand(String email, String password, String googleId) {
        this.email = email;
        this.password = password;
        this.googleId = googleId;
    }
}
