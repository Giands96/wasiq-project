package com.wasiq.inmobiliaria.services;

import com.wasiq.inmobiliaria.models.Property;
import com.wasiq.inmobiliaria.repository.PropertyRepository;
import com.wasiq.inmobiliaria.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PropertyService {

    private final PropertyRepository propertyRepository;
    private final UserRepository userRepository;

    public Property saveProperty(Property property, String email) {
        userRepository.findByEmail(email).ifPresent(property::setOwner);
        return propertyRepository.save(property);
    }

    public Page<Property> findByTitleContaining(String title, int page, int size) {
        return propertyRepository.findByTitleContaining(title,PageRequest.of(page, size));
    }

    public Property softDeleteProperty(Long propertyId) {
        Property property = propertyRepository.findById(propertyId)
                .orElseThrow(() -> new RuntimeException("Error found"));
        property.setActive(false);
        return propertyRepository.save(property);
    }

}


