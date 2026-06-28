package com.wasiq.inmobiliaria.services;

import com.wasiq.inmobiliaria.cloudinary.CloudinaryService;
import com.wasiq.inmobiliaria.controllers.dto.CreatePropertyRequest;
import com.wasiq.inmobiliaria.controllers.dto.UpdatePropertyRequest;
import com.wasiq.inmobiliaria.controllers.exceptions.UnauthorizedException;
import com.wasiq.inmobiliaria.models.*;
import com.wasiq.inmobiliaria.models.enums.OperationType;
import com.wasiq.inmobiliaria.models.enums.PropertyType;
import com.wasiq.inmobiliaria.models.enums.Role;
import com.wasiq.inmobiliaria.repository.PropertyRepository;
import com.wasiq.inmobiliaria.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;

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


    @Transactional
    public Property savePropertyWithImage(CreatePropertyRequest request, List<MultipartFile> files, String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
        Property property = buildProperty(request);
        property.setOwner(user);
        Property savedProperty = propertyRepository.save(property);

        if (files != null && !files.isEmpty()) {
            for (MultipartFile file : files) {
                uploadImage(savedProperty.getId(), file);
            }
        }
        return savedProperty;
    }

    @Transactional
    public Property updateProperty(String slug, UpdatePropertyRequest request, List<MultipartFile> files,
                                   List<Long> keptImageIds,
                                   String email) {

        Property existingProperty = propertyRepository.findBySlugAndActiveTrue(slug)
                .orElseThrow(() -> new RuntimeException("Property not found"));

        if(existingProperty.getOwner() != null && !existingProperty.getOwner().getEmail().equals(email)) {
            throw new UnauthorizedException("You are not the owner of this property");
        }

        applyUpdate(request, existingProperty);

        List<Long> idsKept = keptImageIds == null ? new ArrayList<>() : keptImageIds;
        existingProperty.getImages().removeIf(image -> !idsKept.contains(image.getId()));

        Property savedProperty = propertyRepository.save(existingProperty);

        if (files != null && !files.isEmpty()) {
           for(MultipartFile file : files) {
               uploadImage(savedProperty.getId(), file);
           }
        }

        return propertyRepository.save(existingProperty);
    }

    private Property buildProperty(CreatePropertyRequest request) {
        return Property.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .price(request.getPrice())
                .address(request.getAddress())
                .bedrooms(request.getBedrooms())
                .bathrooms(request.getBathrooms())
                .area(request.getArea())
                .operationType(request.getOperationType())
                .propertyType(request.getPropertyType())
                .available(request.getAvailable() != null ? request.getAvailable() : true)
                .build();
    }

    private void applyUpdate(UpdatePropertyRequest request, Property property) {
        property.setTitle(request.getTitle());
        property.setDescription(request.getDescription());
        property.setPrice(request.getPrice());
        property.setAddress(request.getAddress());
        property.setBedrooms(request.getBedrooms());
        property.setBathrooms(request.getBathrooms());
        property.setArea(request.getArea());
        property.setOperationType(request.getOperationType());
        property.setPropertyType(request.getPropertyType());
        property.setAvailable(request.getAvailable());
    }

    public Property findBySlugAndActiveTrue(String slug) {
        return propertyRepository.findBySlugAndActiveTrue(slug)
                .orElseThrow(() -> new RuntimeException("Property not found"));

    }



    public Page<Property> findByTitleContaining(String title, int page, int size) {
        return propertyRepository.findByTitleContainingAndActiveTrue(title,PageRequest.of(page, size));
    }

    public Property softDeleteProperty(String slug, String email) {

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Property propertyDB = propertyRepository.findBySlugAndActiveTrue(slug)
                .orElseThrow(() -> new RuntimeException("Property not found"));

        if (user.getRole() != Role.ADMIN && (propertyDB.getOwner() == null || !propertyDB.getOwner().getEmail().equals(email))) {
            throw new UnauthorizedException("No tienes permiso para eliminar esta propiedad");
        }

        propertyDB.setActive(false);

        return propertyRepository.save(propertyDB);
    }

    public Page<Property> getFilteredProperties(
            String query, String propertyTypeStr, String operationTypeStr,
            Double minPrice, Double maxPrice, Integer rooms, Integer bathrooms,
            int page, int size) {

        PropertyType propertyType = null;
        if(propertyTypeStr != null) {
            try {
                propertyType = PropertyType.valueOf(propertyTypeStr.toUpperCase());
            } catch (IllegalArgumentException e) {
                throw new RuntimeException("Invalid property type: " + propertyTypeStr);
            }
        }
        OperationType operationType = null;
        if (operationTypeStr != null && !operationTypeStr.trim().isEmpty()) {
            try {
                operationType = OperationType.valueOf(operationTypeStr.toUpperCase());
            } catch (IllegalArgumentException e) {
                // Ignoramos errores de tipeo
            }
        }

        Pageable pageable = PageRequest.of(page, size, Sort.by("id").descending());

        return propertyRepository.findWithDynamicFilters(
                query, propertyType, operationType,
                minPrice, maxPrice, rooms, bathrooms, pageable);
    }

    public PropertyPageFilter resolvePageFilter(String filter) {
        if (filter == null || filter.trim().isEmpty()) {
            return new PropertyPageFilter(
                    "Propiedades",
                    "Encuentra casas, departamentos y terrenos disponibles.",
                    null,
                    null,
                    null
            );
        }

        return switch (filter.trim().toLowerCase()) {
            case "venta" -> new PropertyPageFilter(
                    "Propiedades en venta",
                    "Explora propiedades disponibles para comprar.",
                    "venta",
                    null,
                    OperationType.SALE.name()
            );
            case "alquiler" -> new PropertyPageFilter(
                    "Propiedades en alquiler",
                    "Explora propiedades disponibles para alquilar.",
                    "alquiler",
                    null,
                    OperationType.RENT.name()
            );
            case "departamento", "departamentos" -> new PropertyPageFilter(
                    "Departamentos",
                    "Encuentra departamentos disponibles.",
                    "departamento",
                    PropertyType.APARTMENT.name(),
                    null
            );
            case "terreno", "terrenos" -> new PropertyPageFilter(
                    "Terrenos",
                    "Encuentra terrenos disponibles.",
                    "terreno",
                    PropertyType.LAND.name(),
                    null
            );
            case "casa", "casas" -> new PropertyPageFilter(
                    "Casas",
                    "Encuentra casas disponibles.",
                    "casa",
                    PropertyType.HOUSE.name(),
                    null
            );
            default -> throw new RuntimeException("Invalid property page filter: " + filter);
        };
    }

    public record PropertyPageFilter(
            String title,
            String description,
            String filter,
            String propertyType,
            String operationType
    ) {
    }

}


