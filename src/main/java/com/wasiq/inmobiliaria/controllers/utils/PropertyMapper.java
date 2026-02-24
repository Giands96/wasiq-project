package com.wasiq.inmobiliaria.controllers.utils;

import com.wasiq.inmobiliaria.controllers.dto.PropertyResponse;
import com.wasiq.inmobiliaria.models.Media;
import com.wasiq.inmobiliaria.models.Property;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class PropertyMapper {

    public PropertyResponse toResponse(Property property) {
        if (property == null) return null;

        return PropertyResponse.builder()
                .id(property.getId())
                .title(property.getTitle())
                .description(property.getDescription())
                .price(property.getPrice())
                .address(property.getAddress())
                .bedrooms(property.getBedrooms())
                .bathrooms(property.getBathrooms())
                .area(property.getArea())
                .operationType(property.getOperationType())
                .propertyType(property.getPropertyType())
                .available(property.getAvailable())
                .slug(property.getSlug())
                .createdAt(property.getCreatedAt())
                .updatedAt(property.getUpdatedAt())
                .ownerId(property.getOwner().getId())
                .ownerName(property.getOwner().getFirstName() + " " + property.getOwner().getLastName())
                .ownerEmail(property.getOwner().getEmail())
                // Transformar la Lista de Objetos Media a Lista de Strings (URLs)
                .images(property.getImages() != null ?
                property.getImages().stream().map(Media::getUrl).collect(Collectors.toList()) :
                List.of())
                .build();
    }
}
