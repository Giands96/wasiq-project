package com.wasiq.inmobiliaria.domain.model.properties;

public class Property {
    // ID Atributos
    private Long id;
    private Long ownerId;

    // Caracteristicas Físicas

    private PropertyType propertyType;  //Tipo de propiedad
    private String address;             // Dirección
    private String city;                //Ciudad
    private String country;             //Pais

    //Medidas
    private Double areaM2;              // Metros cuadrados
    private Integer numberOfRooms;      // Número de habitaciones
    private Integer numberOfBathrooms;  // Número de baños
    private Boolean hasGarage;          // Tiene garaje
    private String description;

    //Constructor vacío
    public Property() {
    }

    // Regla de negocio

    public void validateAddress() {
        if (this.address == null || this.address.isBlank()) {
            throw new IllegalArgumentException("La propiedad debe tener una dirección válida.");
        }
    }

    // Getters y Setters
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
}
