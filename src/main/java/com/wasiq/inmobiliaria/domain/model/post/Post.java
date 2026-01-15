package com.wasiq.inmobiliaria.domain.model.post;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public class Post {

    // Atributos core
    private Long id;
    private Long propertyId;

    // Tipo de Operacion
    private OperationType operationType;
    // Estado del post
    private PostStatus status;

    // Precio y moneda
    private BigDecimal price;
    private Currency currency; // Ejemplo: "USD", "PEN"

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Constructor
    public Post() {};

    public Post(Long propertyId, OperationType operationType, BigDecimal price, Currency currency) {
        this.propertyId = propertyId;
        this.operationType = operationType;
        this.status = PostStatus.ACTIVE;
        this.price = price;
        this.currency = currency;
        this.createdAt = LocalDateTime.now();
        validatePrice();
    }


    // REGLAS DE NEGOCIO
    public void validatePrice(){
        if (this.price.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("El precio debe ser mayor que cero.");
        }
    }

    public void markAsClosed(){
        if(this.operationType== OperationType.SALE){
            this.status = PostStatus.SOLD;
        } else if (this.operationType== OperationType.RENT){
            this.status = PostStatus.RENTED;
        }
    }

    // Getters y Setters


    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getPropertyId() {
        return propertyId;
    }

    public void setPropertyId(Long propertyId) {
        this.propertyId = propertyId;
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
