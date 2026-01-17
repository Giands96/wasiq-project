package com.wasiq.inmobiliaria.application.ports.input;


import com.wasiq.inmobiliaria.application.ports.input.command.CreatePostCommand;
import com.wasiq.inmobiliaria.domain.model.post.Currency;
import com.wasiq.inmobiliaria.domain.model.post.OperationType;
import com.wasiq.inmobiliaria.domain.model.post.Post;

import java.math.BigDecimal;

public interface CreatePostUseCase {

    Post createPost(CreatePostCommand command);

}
