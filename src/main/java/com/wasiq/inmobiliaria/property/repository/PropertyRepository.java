package com.wasiq.inmobiliaria.property.repository;

import com.wasiq.inmobiliaria.property.enums.OperationType;
import com.wasiq.inmobiliaria.property.model.Property;
import com.wasiq.inmobiliaria.property.enums.PropertyType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface PropertyRepository extends JpaRepository<Property, Long>, JpaSpecificationExecutor<Property> {

    List<Property> findByOwnerId(Long ownerId);
    Page<Property> findAllByOrderByIdDesc(Pageable pageable);
    List<Property> findByOperationType(OperationType operationType);
    List<Property> findByPropertyType(PropertyType propertyType);
    Page<Property> findByTitleContainingAndActiveTrue(String title, Pageable pageable);
    Optional<Property> findBySlugAndActiveTrue(String slug);
}
