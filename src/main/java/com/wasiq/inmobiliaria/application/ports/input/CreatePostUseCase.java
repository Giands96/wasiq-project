package com.wasiq.inmobiliaria.application.ports.input;


import com.wasiq.inmobiliaria.domain.model.post.Currency;
import com.wasiq.inmobiliaria.domain.model.post.OperationType;
import com.wasiq.inmobiliaria.domain.model.post.Post;

import java.math.BigDecimal;

public interface CreatePostUseCase {

    Post createPost(Long propertyId, OperationType type, BigDecimal price, Currency currency);

}
