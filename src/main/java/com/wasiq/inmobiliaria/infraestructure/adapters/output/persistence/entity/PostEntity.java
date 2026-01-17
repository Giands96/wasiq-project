package com.wasiq.inmobiliaria.infraestructure.adapters.output.persistence.entity;

import com.wasiq.inmobiliaria.domain.model.post.Currency;
import com.wasiq.inmobiliaria.domain.model.post.OperationType;
import com.wasiq.inmobiliaria.domain.model.post.PostStatus;
import com.wasiq.inmobiliaria.domain.model.post.PropertyType;
import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "posts")
@Data
public class PostEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "owner_id", nullable = false)
    private Long ownerId;

    @Column(nullable = false)
    private String title;

    @Enumerated(EnumType.STRING)
    @Column(name = "operation_type", nullable = false)
    private OperationType operationType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PostStatus status;

    @Column(nullable = false)
    private BigDecimal price;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Currency currency;

    @Enumerated(EnumType.STRING)
    @Column(name = "property_type", nullable = false)
    private PropertyType propertyType;

    @Column(nullable = false)
    private String address;
    private String city;
    private String country;

    @Column(name = "area_m2")
    private Double areaM2;

    @Column(name = "num_rooms")
    private Integer numberOfRooms;

    @Column(name = "num_bathrooms")
    private Integer numberOfBathrooms;

    @Column(name = "has_garage")
    private Boolean hasGarage;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "image_url")
    private String imageUrl;

    private LocalDateTime createdAt;
}
