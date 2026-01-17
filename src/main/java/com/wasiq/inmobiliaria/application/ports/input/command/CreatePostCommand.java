package com.wasiq.inmobiliaria.application.ports.input.command;

import com.wasiq.inmobiliaria.domain.model.post.Currency;
import com.wasiq.inmobiliaria.domain.model.post.OperationType;
import com.wasiq.inmobiliaria.domain.model.post.PropertyType;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class CreatePostCommand {

    @NotBlank(message = "El id del propietario es obligatorio")
    private Long ownerId;

    @NotBlank(message = "El titulo es obligatorio")
    private String title;

    @NotNull(message = "El tipo de operación es obligatorio")
    private OperationType operationType;

    @NotNull(message = "El precio es obligatorio")
    @DecimalMin(value = "0.0", inclusive = false, message = "El precio debe ser mayor a 0")
    private BigDecimal price;

    @NotNull(message = "La moneda es obligatoria")
    private Currency currency;

    @NotNull(message = "El tipo de propiedad es obligatorio")
    private PropertyType propertyType;

    @NotBlank(message = "La dirección es obligatoria")
    private String address;

    private String city;
    private String country;
    private Double areaM2;
    private Integer numberOfRooms;
    private Integer numberOfBathrooms;
    private Boolean hasGarage;
    private String description;
    private String imageUrl;

}
