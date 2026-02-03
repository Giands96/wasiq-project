package com.wasiq.inmobiliaria.controllers;

import com.wasiq.inmobiliaria.models.Property;
import com.wasiq.inmobiliaria.services.PropertyService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/properties")
public class PropertyController {

    private final PropertyService propertyService;

    @GetMapping("/")
    public ResponseEntity<Page<Property>> getAllProperties(@RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "10") int size) {
        Page<Property> properties = propertyService.findByTitleContaining("", page, size);
        return ResponseEntity.ok(properties);
    }

    @PostMapping("/create")
    @PreAuthorize("hasAuthority('USER') or hasAuthority('ADMIN')")
    public ResponseEntity<Property> createProperty(@RequestBody Property property, Authentication authentication) {
        String email = authentication.getName();
        Property savedProperty = propertyService.saveProperty(property, email);
        return ResponseEntity.ok(savedProperty);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        propertyService.softDeleteProperty(id);
        return ResponseEntity.noContent().build();
    }

}
