package com.wasiq.inmobiliaria.domain.repository;

import com.wasiq.inmobiliaria.domain.model.post.Post;

public interface PostRepositoryPort {
    Post save(Post post);
}
