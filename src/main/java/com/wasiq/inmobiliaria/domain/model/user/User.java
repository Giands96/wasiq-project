package com.wasiq.inmobiliaria.domain.model.user;

import java.time.LocalDateTime;

public class User {

    //Atributos Core
    private Long id;                // Identificador único del usuario
    private String email;           // Correo electrónico del usuario
    private String googleId;        // Identificador de Google para autenticación


    //Atributos de Perfil
    private String fullName;
    private String pictureUrl;      // Avatar desde Google
    private String phoneNumber;    // Número de teléfono del usuario

    //Logica de Negocio
    private UserRole role;
    private LocalDateTime createdAt;
    private boolean active;

    public User(Long id, String email, String googleId, String fullName, String pictureUrl, String phoneNumber, UserRole role, LocalDateTime createdAt, boolean active) {
        this.email = email;
        this.googleId = googleId;
        this.fullName = fullName;
        this.pictureUrl = pictureUrl;
        this.role = UserRole.BUYER; // Por defecto, todos los nuevos usuarios son BUYER
        this.createdAt = createdAt;
        this.active = true;
    }

    public boolean canPublish() {
        // el rol debe ser AGENT y para publicar debe tener un número de teléfono válido
        // a su vez el numero debe ser diferente de nulo y no estar vacío
        return this.role == UserRole.AGENT &&
                this.phoneNumber != null &&
                !this.phoneNumber.isBlank();
    }

    public void promoteToAgent(String phoneNumber){
        if(phoneNumber == null || phoneNumber.isBlank()){
            throw new IllegalArgumentException("Phone number is required to become an Agent.");
        }
        this.phoneNumber = phoneNumber;
        this.role = UserRole.AGENT;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getGoogleId() {
        return googleId;
    }

    public void setGoogleId(String googleId) {
        this.googleId = googleId;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getPictureUrl() {
        return pictureUrl;
    }

    public void setPictureUrl(String pictureUrl) {
        this.pictureUrl = pictureUrl;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public UserRole getRole() {
        return role;
    }

    public void setRole(UserRole role) {
        this.role = role;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }
}
