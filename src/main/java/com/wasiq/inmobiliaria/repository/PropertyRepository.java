package com.wasiq.inmobiliaria.repository;

import com.wasiq.inmobiliaria.models.OperationType;
import com.wasiq.inmobiliaria.models.Property;
import com.wasiq.inmobiliaria.models.PropertyType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
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
    @Query("SELECT p FROM Property p WHERE p.active = true AND " +
            "(:query IS NULL OR :query = '' OR LOWER(p.title) LIKE LOWER(CONCAT('%', :query, '%'))) AND " +
            "(:propertyType IS NULL OR p.propertyType = :propertyType) AND " +
            "(:operationType IS NULL OR p.operationType = :operationType) AND " +
            "(:minPrice IS NULL OR p.price >= :minPrice) AND " +
            "(:maxPrice IS NULL OR p.price <= :maxPrice) AND " +
            "(:rooms IS NULL OR p.bedrooms >= :rooms) AND " +
            "(:bathrooms IS NULL OR p.bathrooms >= :bathrooms)")
    Page<Property> findWithDynamicFilters(
            @Param("query") String query,
            @Param("propertyType") PropertyType propertyType,
            @Param("operationType") OperationType operationType,
            @Param("minPrice") Double minPrice,
            @Param("maxPrice") Double maxPrice,
            @Param("rooms") Integer rooms,
            @Param("bathrooms") Integer bathrooms,
            Pageable pageable
    );
}
