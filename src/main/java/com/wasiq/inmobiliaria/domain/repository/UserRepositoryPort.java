package com.wasiq.inmobiliaria.domain.repository;

import com.wasiq.inmobiliaria.domain.model.user.User;

import java.util.Optional;

public interface UserRepositoryPort {
    User save(User user);
    Optional<User> findByEmail(String email);
}
