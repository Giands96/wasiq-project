package com.wasiq.inmobiliaria.property.repository;

import com.wasiq.inmobiliaria.property.enums.OperationType;
import com.wasiq.inmobiliaria.property.enums.PropertyType;
import com.wasiq.inmobiliaria.property.model.Property;
import org.springframework.data.jpa.domain.Specification;

public final class PropertySpecifications {

    private PropertySpecifications() {
    }

    public static Specification<Property> active() {
        return (root, query, criteriaBuilder) -> criteriaBuilder.isTrue(root.get("active"));
    }

    public static Specification<Property> titleContains(String value) {
        return (root, query, criteriaBuilder) -> criteriaBuilder.like(
                criteriaBuilder.lower(root.get("title")),
                "%" + value.toLowerCase() + "%"
        );
    }

    public static Specification<Property> propertyTypeEquals(PropertyType value) {
        return (root, query, criteriaBuilder) -> criteriaBuilder.equal(root.get("propertyType"), value);
    }

    public static Specification<Property> operationTypeEquals(OperationType value) {
        return (root, query, criteriaBuilder) -> criteriaBuilder.equal(root.get("operationType"), value);
    }

    public static Specification<Property> priceGreaterThanOrEqual(Double value) {
        return (root, query, criteriaBuilder) -> criteriaBuilder.greaterThanOrEqualTo(root.get("price"), value);
    }

    public static Specification<Property> priceLessThanOrEqual(Double value) {
        return (root, query, criteriaBuilder) -> criteriaBuilder.lessThanOrEqualTo(root.get("price"), value);
    }

    public static Specification<Property> bedroomsGreaterThanOrEqual(Integer value) {
        return (root, query, criteriaBuilder) -> criteriaBuilder.greaterThanOrEqualTo(root.get("bedrooms"), value);
    }

    public static Specification<Property> bathroomsGreaterThanOrEqual(Integer value) {
        return (root, query, criteriaBuilder) -> criteriaBuilder.greaterThanOrEqualTo(root.get("bathrooms"), value);
    }
}
