package com.wasiq.inmobiliaria.controllers.dto;

import com.wasiq.inmobiliaria.models.Media;
import com.wasiq.inmobiliaria.models.OperationType;
import com.wasiq.inmobiliaria.models.PropertyType;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class PropertyResponse {
    private Long id;
    private String title;
    private String description;
    private Double price;
    private String address;
    private Integer bedrooms;
    private Integer bathrooms;
    private Double area;
    private OperationType operationType;
    private PropertyType propertyType;
    private Boolean available;
    private List<String> images;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String ownerName;
    private String ownerEmail;
}
