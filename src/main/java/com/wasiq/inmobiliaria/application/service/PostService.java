package com.wasiq.inmobiliaria.application.service;

import com.wasiq.inmobiliaria.application.ports.input.CreatePostUseCase;
import com.wasiq.inmobiliaria.application.ports.input.command.CreatePostCommand;
import com.wasiq.inmobiliaria.application.ports.output.PostRepositoryPort;
import com.wasiq.inmobiliaria.domain.model.post.Post;


public class PostService implements CreatePostUseCase {

    private final PostRepositoryPort postRepositoryPort;

    public PostService(PostRepositoryPort postRepositoryPort) {
        this.postRepositoryPort = postRepositoryPort;
    }

    @Override
    public Post createPost(CreatePostCommand command) {
        Post newPost = new Post(
                command.getOwnerId(),
                command.getTitle(),
                command.getOperationType(),
                command.getPrice(),
                command.getCurrency(),
                command.getPropertyType(),
                command.getAddress(),
                command.getCity(),
                command.getCountry(),
                command.getAreaM2(),
                command.getNumberOfRooms(),
                command.getNumberOfBathrooms(),
                command.getHasGarage(),
                command.getDescription(),
                command.getImageUrl()
        );
        return postRepositoryPort.save(newPost);
    }
}
