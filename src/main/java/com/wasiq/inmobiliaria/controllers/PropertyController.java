package com.wasiq.inmobiliaria.controllers;

import com.wasiq.inmobiliaria.controllers.dto.PropertyResponse;
import com.wasiq.inmobiliaria.controllers.utils.PropertyMapper;
import com.wasiq.inmobiliaria.models.Property;
import com.wasiq.inmobiliaria.services.PropertyService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.net.URI;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/properties")
public class PropertyController {

    private final PropertyService propertyService;
    private final PropertyMapper propertyMapper;

    @GetMapping("/")
    public ResponseEntity<Page<PropertyResponse>> getAllProperties(@RequestParam(defaultValue = "0") int page,
                                                                   @RequestParam(defaultValue = "10") int size,
                                                                   @RequestParam(defaultValue = "") String title) {
        Page<Property> propertiesPage = propertyService.findByTitleContaining(title, page, size);
        Page<PropertyResponse> responsePage = propertiesPage.map(propertyMapper::toResponse);
        return ResponseEntity.ok(responsePage);
    }

    @GetMapping("/slug/{slug}")
    public ResponseEntity<PropertyResponse> getPropertyBySlug(@PathVariable String slug ) {
        Property property = propertyService.findBySlugAndActiveTrue(slug);
        return ResponseEntity.ok(propertyMapper.toResponse(property));
    }

    @PostMapping(value = "/create",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAnyAuthority('USER', 'ADMIN')")
    public ResponseEntity<PropertyResponse> createProperty(
            @RequestPart("property") @Valid Property property,
            @RequestPart(value = "files", required = false) List<MultipartFile> files,
            Authentication authentication) {

        if (files != null && files.size() > 4) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Solo se permiten un máximo de 4 imágenes por propiedad");
        }
        String email = authentication.getName();
        Property savedProperty = propertyService.savePropertyWithImage(property, files, email);

        return ResponseEntity.created(URI.create("/properties/" + savedProperty.getId()))
                .body(propertyMapper.toResponse(savedProperty));
    }

    @PutMapping("/update/{slug}")
    @PreAuthorize("hasAnyAuthority('USER', 'ADMIN')")
    public ResponseEntity<PropertyResponse> updateProperty(
            @PathVariable String slug,
            @RequestPart(value = "property") @Valid Property property,
            @RequestPart(value = "files", required = false) List<MultipartFile> files,
            @RequestParam(value = "keptImageIds", required = false) List<Long> keptImageIds,
            Authentication authentication) {
        String email = authentication.getName();
        Property updatedProperty = propertyService.updateProperty(slug, property, files, keptImageIds, email);

        return ResponseEntity.ok(propertyMapper.toResponse(updatedProperty));
    }

    @DeleteMapping("/delete/{slug}")
    @PreAuthorize("hasAnyAuthority('USER', 'ADMIN')")
    public ResponseEntity<Void> deleteProperty(@PathVariable String slug, Authentication authentication) {
        String email = authentication.getName();
        propertyService.softDeleteProperty(slug, email);
        return ResponseEntity.noContent().build();
    }
}
