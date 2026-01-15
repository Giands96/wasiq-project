package com.wasiq.inmobiliaria.domain.model.leads;

import java.time.LocalDateTime;

public class Lead {
    private Long id;
    private Long postId;        // El puente con la publicación
    private Long buyerId;       // El usuario interesado

    // Mensaje del interesado
    private String message;
    private LocalDateTime createdAt;

    // Constructor
    public Lead() {}

    public Lead(Long postId, Long buyerId, String message) {
        this.postId = postId;
        this.buyerId = buyerId;
        this.message = message;
        this.createdAt = LocalDateTime.now();
    }

    // Getters y Setters


    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getPostId() {
        return postId;
    }

    public void setPostId(Long postId) {
        this.postId = postId;
    }

    public Long getBuyerId() {
        return buyerId;
    }

    public void setBuyerId(Long buyerId) {
        this.buyerId = buyerId;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
