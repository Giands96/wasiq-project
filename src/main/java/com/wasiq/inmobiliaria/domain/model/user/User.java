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
    private String password;

    //Logica de Negocio
    private UserRole
            role;
    private LocalDateTime createdAt;
    private boolean active;

    // Para registro
    public User(String email, String googleId, String fullName, String pictureUrl, String phoneNumber, String password, UserRole role, LocalDateTime createdAt, boolean active) {
        this.email = email;
        this.googleId = googleId;
        this.fullName = fullName;
        this.pictureUrl = pictureUrl;
        this.phoneNumber = phoneNumber;
        this.password = password;
        this.role = UserRole.BUYER;
        this.createdAt = createdAt;
        this.active = true;
    }

    // Para persistencia de datos
    public User(Long id, String email, String googleId, String fullName, String pictureUrl, String phoneNumber, String password, UserRole role, LocalDateTime createdAt, boolean active) {
        this.id = id;
        this.email = email;
        this.googleId = googleId;
        this.fullName = fullName;
        this.pictureUrl = pictureUrl;
        this.phoneNumber = phoneNumber;
        this.password = password;
        this.role = role;
        this.createdAt = createdAt;
        this.active = active;
    }

    public boolean canPublish() {
        //* el rol debe ser AGENT y para publicar debe tener un número de teléfono válido
        //* a su vez el numero debe ser diferente de nulo y no estar vacío
        return this.role == UserRole.AGENT &&
                this.phoneNumber != null &&
                !this.phoneNumber.isBlank();
    }

    public void promoteToAgent(String phoneNumber){
        if(phoneNumber == null || phoneNumber.isBlank()){
            throw new IllegalArgumentException("El número de telefono es obligatorio para ser un Agente");
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

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
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
