package com.wasiq.inmobiliaria.application.ports.output;

import com.wasiq.inmobiliaria.domain.model.post.Post;

public interface PostRepositoryPort {
    //Guardar post
    Post save(Post post);

}
