package com.wasiq.inmobiliaria.user.dto;

import com.wasiq.inmobiliaria.user.model.enums.Role;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UpdateRoleRequest {

    @NotNull(message = "El rol es obligatorio")
    private Role role;
}