package com.wasiq.inmobiliaria.property.service;

import com.wasiq.inmobiliaria.cloudinary.CloudinaryService;
import com.wasiq.inmobiliaria.shared.dto.CreatePropertyRequest;
import com.wasiq.inmobiliaria.shared.dto.UpdatePropertyRequest;
import com.wasiq.inmobiliaria.shared.exceptions.UnauthorizedException;
import com.wasiq.inmobiliaria.media.ImageFiles;
import com.wasiq.inmobiliaria.media.Media;
import com.wasiq.inmobiliaria.property.enums.OperationType;
import com.wasiq.inmobiliaria.property.enums.PropertyType;
import com.wasiq.inmobiliaria.user.model.enums.Role;
import com.wasiq.inmobiliaria.property.model.Property;
import com.wasiq.inmobiliaria.property.repository.PropertyRepository;
import com.wasiq.inmobiliaria.property.repository.PropertySpecifications;
import com.wasiq.inmobiliaria.user.repository.UserRepository;
import com.wasiq.inmobiliaria.user.model.User;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PropertyService {

    //* Upper bound for any page request, so a single call cannot load the whole catalog */
    public static final int MAX_PAGE_SIZE = 50;

    private final PropertyRepository propertyRepository;
    private final UserRepository userRepository;
    private final CloudinaryService cloudinary;


    public void uploadImage(Long propertyId, MultipartFile file){
        Property property = propertyRepository.findById(propertyId)
                .orElseThrow(() -> new RuntimeException("Property not found"));

        String contentType = ImageFiles.detectContentType(file);
        String url = cloudinary.uploadFile(file);

        Media newMedia = Media.builder()
                .url(url)
                .type(contentType)
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
        List<MultipartFile> newFiles = files == null ? List.of() : files;
        ImageFiles.requireWithinLimit(newFiles.size());
        ImageFiles.requireSupportedImages(newFiles);

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

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (user.getRole() != Role.ADMIN && (existingProperty.getOwner() == null || !existingProperty.getOwner().getEmail().equals(email))) {
            throw new UnauthorizedException("You are not the owner of this property");
        }

        List<Long> idsKept = keptImageIds == null ? new ArrayList<>() : keptImageIds;
        if (existingProperty.getImages() == null) {
            existingProperty.setImages(new ArrayList<>());
        }
        List<Media> currentImages = existingProperty.getImages();
        List<Media> removedImages = currentImages.stream()
                .filter(image -> !idsKept.contains(image.getId()))
                .toList();
        List<MultipartFile> newFiles = files == null ? List.of() : files;
        //* Validate everything before mutating the property or uploading anything */
        ImageFiles.requireWithinLimit(currentImages.size() - removedImages.size() + newFiles.size());
        ImageFiles.requireSupportedImages(newFiles);

        applyUpdate(request, existingProperty);
        currentImages.removeAll(removedImages);

        Property savedProperty = propertyRepository.save(existingProperty);

        for (MultipartFile file : newFiles) {
            uploadImage(savedProperty.getId(), file);
        }

        Property result = propertyRepository.save(existingProperty);
        deleteFromCloudinaryAfterCommit(removedImages.stream().map(Media::getUrl).toList());
        return result;
    }

    //* Replaced images are removed from Cloudinary only once the database change is committed */
    private void deleteFromCloudinaryAfterCommit(List<String> urls) {
        if (urls.isEmpty()) {
            return;
        }
        if (!TransactionSynchronizationManager.isSynchronizationActive()) {
            urls.forEach(cloudinary::deleteFile);
            return;
        }
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                urls.forEach(cloudinary::deleteFile);
            }
        });
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
                .active(true)
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

    public Property findPublicPropertyBySlug(String slug) {
        return propertyRepository.findBySlugAndActiveTrueAndAvailableTrue(slug)
                .orElseThrow(() -> new RuntimeException("Property not found"));
    }



    public Page<Property> findByTitleContaining(String title, int page, int size) {
        return propertyRepository.findByTitleContainingAndActiveTrue(title, safePage(page, size, Sort.unsorted()));
    }

    private Pageable safePage(int page, int size, Sort sort) {
        return PageRequest.of(Math.max(page, 0), Math.clamp(size, 1, MAX_PAGE_SIZE), sort);
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
                throw new RuntimeException("Invalid operation type: " + operationTypeStr);
            }
        }

        Pageable pageable = safePage(page, size, Sort.by("id").descending());
        Specification<Property> filters = buildPropertyFilters(
                query, propertyType, operationType, minPrice, maxPrice, rooms, bathrooms
        );

        return propertyRepository.findAll(filters, pageable);
    }

    private Specification<Property> buildPropertyFilters(
            String query,
            PropertyType propertyType,
            OperationType operationType,
            Double minPrice,
            Double maxPrice,
            Integer rooms,
            Integer bathrooms) {
        Specification<Property> filters = PropertySpecifications.active()
                .and(PropertySpecifications.available());

        if (query != null && !query.trim().isEmpty()) {
            filters = filters.and(PropertySpecifications.titleContains(query.trim()));
        }
        if (propertyType != null) {
            filters = filters.and(PropertySpecifications.propertyTypeEquals(propertyType));
        }
        if (operationType != null) {
            filters = filters.and(PropertySpecifications.operationTypeEquals(operationType));
        }
        if (minPrice != null) {
            filters = filters.and(PropertySpecifications.priceGreaterThanOrEqual(minPrice));
        }
        if (maxPrice != null) {
            filters = filters.and(PropertySpecifications.priceLessThanOrEqual(maxPrice));
        }
        if (rooms != null) {
            filters = filters.and(PropertySpecifications.bedroomsGreaterThanOrEqual(rooms));
        }
        if (bathrooms != null) {
            filters = filters.and(PropertySpecifications.bathroomsGreaterThanOrEqual(bathrooms));
        }

        return filters;
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

    //* ADMIN

    public Property findAdminPropertyBySlug(String slug) {
        return propertyRepository.findBySlugAndActiveTrue(slug)
                .orElseThrow(() -> new RuntimeException("Property not found"));
    }

    public Page<Property> getAdminProperties(
            String query, String propertyTypeStr, String operationTypeStr,
            Boolean available, int page, int size) {

        PropertyType propertyType = null;
        if (propertyTypeStr != null) {
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
                throw new RuntimeException("Invalid operation type: " + operationTypeStr);
            }
        }

        Pageable pageable = safePage(page, size, Sort.by("id").descending());
        Specification<Property> filters = buildAdminPropertyFilters(query, propertyType, operationType, available);

        return propertyRepository.findAll(filters, pageable);
    }

    private Specification<Property> buildAdminPropertyFilters(
            String query,
            PropertyType propertyType,
            OperationType operationType,
            Boolean available) {
        Specification<Property> filters = PropertySpecifications.active();

        if (query != null && !query.trim().isEmpty()) {
            filters = filters.and(PropertySpecifications.titleContains(query.trim()));
        }
        if (propertyType != null) {
            filters = filters.and(PropertySpecifications.propertyTypeEquals(propertyType));
        }
        if (operationType != null) {
            filters = filters.and(PropertySpecifications.operationTypeEquals(operationType));
        }
        if (available != null) {
            filters = filters.and(PropertySpecifications.availableEquals(available));
        }

        return filters;
    }

}


