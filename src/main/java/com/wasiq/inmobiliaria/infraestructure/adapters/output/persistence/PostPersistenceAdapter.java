package com.wasiq.inmobiliaria.infraestructure.adapters.output.persistence;

import com.wasiq.inmobiliaria.domain.model.post.Post;
import com.wasiq.inmobiliaria.domain.repository.PostRepositoryPort;
import com.wasiq.inmobiliaria.infraestructure.adapters.output.persistence.entity.PostEntity;
import com.wasiq.inmobiliaria.infraestructure.adapters.output.persistence.repository.PostJpaRepository;
import org.springframework.stereotype.Component;

@Component
public class PostPersistenceAdapter implements PostRepositoryPort {

    private final PostJpaRepository postJpaRepository;

    public PostPersistenceAdapter(PostJpaRepository postJpaRepository) {
        this.postJpaRepository = postJpaRepository;
    }

    @Override
    public Post save(Post post) {
        PostEntity entity = toEntity(post);
        PostEntity savedEntity = postJpaRepository.save(entity);
        return toDomain(savedEntity);
    }

    // * --- MAPPERS ---

    private PostEntity toEntity(Post post) {
        PostEntity entity = new PostEntity();
        entity.setId(post.getId());
        entity.setOwnerId(post.getOwnerId());

        entity.setTitle(post.getTitle());
        entity.setOperationType(post.getOperationType());
        entity.setStatus(post.getStatus());
        entity.setPrice(post.getPrice());
        entity.setCurrency(post.getCurrency());

        entity.setPropertyType(post.getPropertyType());
        entity.setAddress(post.getAddress());
        entity.setCity(post.getCity());
        entity.setCountry(post.getCountry());
        entity.setAreaM2(post.getAreaM2());
        entity.setNumberOfRooms(post.getNumberOfRooms());
        entity.setNumberOfBathrooms(post.getNumberOfBathrooms());
        entity.setHasGarage(post.getHasGarage());
        entity.setDescription(post.getDescription());
        entity.setImageUrl(post.getImageUrl());

        entity.setCreatedAt(post.getCreatedAt());

        return entity;
    }

    private Post toDomain(PostEntity entity) {
        Post post = new Post();
        post.setId(entity.getId());
        post.setOwnerId(entity.getOwnerId());

        post.setTitle(entity.getTitle());
        post.setOperationType(entity.getOperationType());
        post.setStatus(entity.getStatus());
        post.setPrice(entity.getPrice());
        post.setCurrency(entity.getCurrency());

        post.setPropertyType(entity.getPropertyType());
        post.setAddress(entity.getAddress());
        post.setCity(entity.getCity());
        post.setCountry(entity.getCountry());
        post.setAreaM2(entity.getAreaM2());
        post.setNumberOfRooms(entity.getNumberOfRooms());
        post.setNumberOfBathrooms(entity.getNumberOfBathrooms());
        post.setHasGarage(entity.getHasGarage());
        post.setDescription(entity.getDescription());
        post.setImageUrl(entity.getImageUrl());

        post.setCreatedAt(entity.getCreatedAt());

        return post;
    }
}
