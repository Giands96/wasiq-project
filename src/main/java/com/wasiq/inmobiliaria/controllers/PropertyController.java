package com.wasiq.inmobiliaria.controllers;

import com.wasiq.inmobiliaria.controllers.dto.PropertyResponse;
import com.wasiq.inmobiliaria.controllers.utils.PropertyMapper;
import com.wasiq.inmobiliaria.models.Property;
import com.wasiq.inmobiliaria.services.PropertyService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.net.URI;

@RestController
@RequiredArgsConstructor
@RequestMapping("/properties")
public class PropertyController {

    private final PropertyService propertyService;
    private final PropertyMapper propertyMapper;

    @GetMapping("/")
    public ResponseEntity<Page<PropertyResponse>> getAllProperties(@RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "10") int size) {
        Page<Property> propertiesPage = propertyService.findByTitleContaining("", page, size);
        Page<PropertyResponse> responsePage = propertiesPage.map(propertyMapper::toResponse);
        return ResponseEntity.ok(responsePage);
    }

    @PostMapping(value = "/create",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasAnyAuthority('USER', 'ADMIN')")
    public ResponseEntity<PropertyResponse> createProperty(
            @RequestPart("property") @Valid Property property,
            @RequestPart(value = "file", required = false) MultipartFile file,
            Authentication authentication) {
        String email = authentication.getName();
        Property savedProperty = propertyService.savePropertyWithImage(property, file, email);

        return ResponseEntity.created(URI.create("/api/properties/" + savedProperty.getId()))
                .body(propertyMapper.toResponse(savedProperty));
    }

    @PutMapping("/update/{id}")
    @PreAuthorize("hasAnyAuthority('USER', 'ADMIN')")
    public ResponseEntity<PropertyResponse> updateProperty(
            @PathVariable Long id,
            @RequestPart(value = "property") @Valid Property property,
            @RequestPart(value = "file", required = false) MultipartFile file,
            Authentication authentication) {
        String email = authentication.getName();
        Property updatedProperty = propertyService.updateProperty(id, property, file, email);

        return ResponseEntity.ok(propertyMapper.toResponse(updatedProperty));
    }

    @DeleteMapping("/delete/{id}")
    @PreAuthorize("hasAnyAuthority('USER', 'ADMIN')")
    public ResponseEntity<Void> deleteProperty(@PathVariable Long id, Authentication authentication) {
        String email = authentication.getName();
        propertyService.softDeleteProperty(id, email);
        return ResponseEntity.noContent().build();
    }
}
