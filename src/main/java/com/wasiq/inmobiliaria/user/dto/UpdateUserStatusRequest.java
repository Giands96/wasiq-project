package com.wasiq.inmobiliaria.user.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UpdateUserStatusRequest {

    @NotNull(message = "El estado es obligatorio")
    private Boolean active;
}