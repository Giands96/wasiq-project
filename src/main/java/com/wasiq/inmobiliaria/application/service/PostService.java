package com.wasiq.inmobiliaria.application.service;

import com.wasiq.inmobiliaria.application.ports.input.CreatePostUseCase;
import com.wasiq.inmobiliaria.application.ports.output.PostRepositoryPort;
import com.wasiq.inmobiliaria.application.ports.output.PropertyRepositoryPort;
import com.wasiq.inmobiliaria.domain.model.post.Currency;
import com.wasiq.inmobiliaria.domain.model.post.OperationType;
import com.wasiq.inmobiliaria.domain.model.post.Post;
import com.wasiq.inmobiliaria.domain.model.post.PostStatus;
import com.wasiq.inmobiliaria.domain.model.properties.Property;

import java.math.BigDecimal;

public class PostService implements CreatePostUseCase {
    private final PropertyRepositoryPort propertyRepositoryPort;
    private final PostRepositoryPort postRepositoryPort;

    public PostService(PropertyRepositoryPort propertyRepositoryPort, PostRepositoryPort postRepositoryPort) {
        this.propertyRepositoryPort = propertyRepositoryPort;
        this.postRepositoryPort = postRepositoryPort;
    }


    @Override
    public Post createPost(Long propertyId, OperationType type, BigDecimal price, Currency currency) {

        Property property = propertyRepositoryPort.findById(propertyId).orElseThrow(
                () -> new IllegalArgumentException("Propiedad no encontrada con el ID: " + propertyId)
        );

        // ? Validar direccion
        property.validateAddress();

        // ? Crear nuevo Post con ID, Tipo, Precio y Moneda
        Post newPost = new Post(propertyId, type, price, currency);

        return postRepositoryPort.save(newPost);
    }
}
