package com.wasiq.inmobiliaria.services;

import com.wasiq.inmobiliaria.cloudinary.CloudinaryService;
import com.wasiq.inmobiliaria.controllers.exceptions.UnauthorizedException;
import com.wasiq.inmobiliaria.models.Media;
import com.wasiq.inmobiliaria.models.Property;
import com.wasiq.inmobiliaria.models.Role;
import com.wasiq.inmobiliaria.models.User;
import com.wasiq.inmobiliaria.repository.PropertyRepository;
import com.wasiq.inmobiliaria.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class PropertyService {

    private final PropertyRepository propertyRepository;
    private final UserRepository userRepository;
    private final CloudinaryService cloudinary;


    public void uploadImage(Long propertyId, MultipartFile file){
        Property property = propertyRepository.findById(propertyId)
                .orElseThrow(() -> new RuntimeException("Property not found"));

        String url = cloudinary.uploadFile(file);

        Media newMedia = Media.builder()
                .url(url)
                .type(file.getContentType())
                .property(property)
                .build();

        if(property.getImages() == null) {
            property.setImages(new ArrayList<>());
        }

        property.getImages().add(newMedia);
        propertyRepository.save(property);
    }


    public Property saveProperty(Property property, String email) {
        userRepository.findByEmail(email).ifPresent(property::setOwner);
        return propertyRepository.save(property);
    }
    @Transactional
    public Property savePropertyWithImage(Property property, MultipartFile file, String email) {
        Property saveProperty = saveProperty(property, email);
        if (file != null && !file.isEmpty()) {
            uploadImage(saveProperty.getId(), file);
        }
        return saveProperty;
    }

    @Transactional
    public Property updateProperty(Long propertyId, Property updatedProperty, MultipartFile file, String email) {

        Property existingProperty = propertyRepository.findById(propertyId)
                .orElseThrow(() -> new RuntimeException("Property not found"));

        if(existingProperty.getOwner() != null && !existingProperty.getOwner().getEmail().equals(email)) {
            throw new UnauthorizedException("You are not the owner of this property");
        }

        existingProperty.setTitle(updatedProperty.getTitle());
        existingProperty.setDescription(updatedProperty.getDescription());
        existingProperty.setPrice(updatedProperty.getPrice());
        existingProperty.setAddress(updatedProperty.getAddress());
        existingProperty.setBedrooms(updatedProperty.getBedrooms());
        existingProperty.setBathrooms(updatedProperty.getBathrooms());
        existingProperty.setArea(updatedProperty.getArea());
        existingProperty.setOperationType(updatedProperty.getOperationType());
        existingProperty.setPropertyType(updatedProperty.getPropertyType());
        existingProperty.setAvailable(updatedProperty.getAvailable());

        Property savedProperty = propertyRepository.save(existingProperty);

        if (file != null && !file.isEmpty()) {
            uploadImage(savedProperty.getId(), file);
        }

        return savedProperty;
    }




    public Page<Property> findByTitleContaining(String title, int page, int size) {
        return propertyRepository.findByTitleContainingAndActiveTrue(title,PageRequest.of(page, size));
    }

    public Property softDeleteProperty(Long propertyId, String email) {

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Property propertyDB = propertyRepository.findById(propertyId)
                .orElseThrow(() -> new RuntimeException("Property not found"));

        if (user.getRole() != Role.ADMIN && (propertyDB.getOwner() == null || !propertyDB.getOwner().getEmail().equals(email))) {
            throw new RuntimeException("No tienes permiso para eliminar esta propiedad");
        }

        propertyDB.setActive(false);

        return propertyRepository.save(propertyDB);
    }


}


