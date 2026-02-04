package com.wasiq.inmobiliaria.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

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

    @JsonIgnore
    @JoinColumn(name = "owner_id", nullable = false)
    @ManyToOne(fetch = FetchType.LAZY)
    private User owner;
    @Column(nullable = false, name = "available")
    private Boolean available;
    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY, mappedBy = "property")
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
    }

    @PostPersist
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

}
