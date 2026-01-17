package com.wasiq.inmobiliaria.domain.model.post;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class Post {

    // Atributos core
    private Long id;
    private Long ownerId;

    //Datos de la publicación
    private String title;
    private String slug;
    private OperationType operationType;
    private PostStatus status;
    private BigDecimal price;
    private Currency currency;

    // Datos de la Propiedad
    private PropertyType propertyType;
    private String address;
    private String city;
    private String country;

    // Características Físicas
    private Double areaM2;
    private Integer numberOfRooms;
    private Integer numberOfBathrooms;
    private Boolean hasGarage;
    private String description;
    private String imageUrl; // Foto principal

    // Auditoría
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;


    public Post() {
    }

    // Constructor Maestro
    public Post(Long ownerId, String title, OperationType operationType, BigDecimal price, Currency currency,
                PropertyType propertyType, String address, String city, String country,
                Double areaM2, Integer rooms, Integer bathrooms, Boolean garage,
                String description, String imageUrl) {

        this.ownerId = ownerId;
        this.title = title;
        this.operationType = operationType;
        this.price = price;
        this.currency = currency;
        this.propertyType = propertyType;
        this.address = address;
        this.city = city;
        this.country = country;
        this.areaM2 = areaM2;
        this.numberOfRooms = rooms;
        this.numberOfBathrooms = bathrooms;
        this.hasGarage = garage;
        this.description = description;
        this.imageUrl = imageUrl;

        // Valores por defecto
        this.status = PostStatus.ACTIVE;
        this.createdAt = LocalDateTime.now();

        validate();
    }

    private void validate() {
        if (price == null || price.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("El precio debe ser mayor a 0");
        }
        if (title == null || title.isBlank()) {
            throw new IllegalArgumentException("El título es obligatorio");
        }
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getOwnerId() {
        return ownerId;
    }

    public void setOwnerId(Long ownerId) {
        this.ownerId = ownerId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getSlug() {
        return slug;
    }

    public void setSlug(String slug) {
        this.slug = slug;
    }

    public OperationType getOperationType() {
        return operationType;
    }

    public void setOperationType(OperationType operationType) {
        this.operationType = operationType;
    }

    public PostStatus getStatus() {
        return status;
    }

    public void setStatus(PostStatus status) {
        this.status = status;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public Currency getCurrency() {
        return currency;
    }

    public void setCurrency(Currency currency) {
        this.currency = currency;
    }

    public PropertyType getPropertyType() {
        return propertyType;
    }

    public void setPropertyType(PropertyType propertyType) {
        this.propertyType = propertyType;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public Double getAreaM2() {
        return areaM2;
    }

    public void setAreaM2(Double areaM2) {
        this.areaM2 = areaM2;
    }

    public Integer getNumberOfRooms() {
        return numberOfRooms;
    }

    public void setNumberOfRooms(Integer numberOfRooms) {
        this.numberOfRooms = numberOfRooms;
    }

    public Integer getNumberOfBathrooms() {
        return numberOfBathrooms;
    }

    public void setNumberOfBathrooms(Integer numberOfBathrooms) {
        this.numberOfBathrooms = numberOfBathrooms;
    }

    public Boolean getHasGarage() {
        return hasGarage;
    }

    public void setHasGarage(Boolean hasGarage) {
        this.hasGarage = hasGarage;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}
