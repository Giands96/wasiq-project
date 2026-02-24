package com.wasiq.inmobiliaria.repository;

import com.wasiq.inmobiliaria.models.OperationType;
import com.wasiq.inmobiliaria.models.Property;
import com.wasiq.inmobiliaria.models.PropertyType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface PropertyRepository extends JpaRepository<Property, Long> {

    List<Property> findByOwnerId(Long ownerId);
    Page<Property> findAllByOrderByIdDesc(Pageable pageable);
    List<Property> findByOperationType(OperationType operationType);
    List<Property> findByPropertyType(PropertyType propertyType);
    Page<Property> findByTitleContainingAndActiveTrue(String title, Pageable pageable);
    Optional<Property> findBySlugAndActiveTrue(String slug);
}
