package com.wasiq.inmobiliaria.shared.dto;

import com.wasiq.inmobiliaria.property.enums.OperationType;
import com.wasiq.inmobiliaria.property.enums.PropertyType;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdatePropertyRequest {

    @NotBlank(message = "El titulo es obligatorio")
    private String title;

    @NotBlank(message = "La descripcion es obligatoria")
    private String description;

    @NotNull(message = "El precio es obligatorio")
    @Positive(message = "El precio debe ser mayor a 0")
    private Double price;

    @NotBlank(message = "La direccion es obligatoria")
    private String address;

    @NotNull(message = "La cantidad de dormitorios es obligatoria")
    @Min(value = 0, message = "La cantidad de dormitorios no puede ser negativa")
    private Integer bedrooms;

    @NotNull(message = "La cantidad de banos es obligatoria")
    @Min(value = 0, message = "La cantidad de banos no puede ser negativa")
    private Integer bathrooms;

    @NotNull(message = "El area es obligatoria")
    @Positive(message = "El area debe ser mayor a 0")
    private Double area;

    @NotNull(message = "El tipo de operacion es obligatorio")
    private OperationType operationType;

    @NotNull(message = "El tipo de propiedad es obligatorio")
    private PropertyType propertyType;

    @NotNull(message = "La disponibilidad es obligatoria")
    private Boolean available;
}
