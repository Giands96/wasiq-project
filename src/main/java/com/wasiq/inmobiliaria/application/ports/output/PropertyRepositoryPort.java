package com.wasiq.inmobiliaria.application.ports.output;

import com.wasiq.inmobiliaria.domain.model.properties.Property;

import java.util.Optional;


//Output, lo que saldrá de la aplicación
public interface PropertyRepositoryPort {

    // Para recuperar una propiedad por su ID
    Optional<Property> findById(Long id);

    //Guardar cambios
    Property save(Property property);

    //Validar si existe una propiedad
    boolean existsById(Long id);

}
