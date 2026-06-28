package com.wasiq.inmobiliaria.auth.dto;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UpdateUserRequest {

    @Size(min = 8, max = 100, message = "La contrasena debe tener entre 8 y 100 caracteres")
    private String password;

    @Size(max = 30, message = "El telefono no puede superar 30 caracteres")
    @Pattern(regexp = "^[0-9+()\\-\\s]*$", message = "El telefono contiene caracteres invalidos")
    private String phoneNumber;
}
