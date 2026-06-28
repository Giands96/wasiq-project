package com.wasiq.inmobiliaria.shared.dto;

import com.wasiq.inmobiliaria.property.enums.OperationType;
import com.wasiq.inmobiliaria.property.enums.PropertyType;
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
    private String slug;
    private List<String> images;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String ownerName;
    private String ownerEmail;
    private Long ownerId;
    private String ownerPhone;
}
