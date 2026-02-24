package com.wasiq.inmobiliaria.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "properties")
@Builder
public class Property {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, name = "title")
    private String title;

    @Column(nullable = false, length = 1000, name = "description")
    private String description;

    @Column(nullable = false, name = "price")
    private Double price;

    @Column(nullable = false, name = "address")
    private String address;

    @Column(nullable = false, name = "bedrooms")
    private Integer bedrooms;

    @Column(nullable = false, name = "bathrooms")
    private Integer bathrooms;

    @Column(nullable = false, name = "area")
    private Double area;

    @Enumerated(EnumType.STRING)
    private OperationType operationType;

    @Enumerated(EnumType.STRING)
    private PropertyType propertyType;

    @Column(name = "active")
    private Boolean active = true;

    @Column(unique = true, nullable = false, name = "slug")
    private String slug;

    @JsonIgnore
    @JoinColumn(name = "owner_id", nullable = false)
    @ManyToOne(fetch = FetchType.LAZY)
    private User owner;
    @Column(nullable = false, name = "available")
    private Boolean available;
    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY, mappedBy = "property", orphanRemoval = true)
    private List<Media> images;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.available = true;
        this.active = true;
        if(this.title != null) {
            String baseSlug = this.title.toLowerCase().replaceAll("[^a-z0-9\\s-]","").replaceAll("\\s+", "-").replaceAll("-+", "-");
            //* Agregar un sufijo único para garantizar la unicidad del slug *//
            String uniqueId = UUID.randomUUID().toString().substring(0,6);
            this.slug = baseSlug + "-" + uniqueId;
        }
    }

    @PostPersist
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

}
