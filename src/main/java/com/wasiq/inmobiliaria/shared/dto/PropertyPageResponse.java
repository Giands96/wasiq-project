package com.wasiq.inmobiliaria.shared.dto;

import lombok.Builder;
import lombok.Data;
import org.springframework.data.domain.Page;

@Data
@Builder
public class PropertyPageResponse {
    private String title;
    private String description;
    private String filter;
    private String propertyType;
    private String operationType;
    private Page<PropertyResponse> properties;
}
