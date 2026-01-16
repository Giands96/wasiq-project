package com.wasiq.inmobiliaria.infraestructure.adapters.output.persistence;

import com.wasiq.inmobiliaria.domain.model.user.User;
import com.wasiq.inmobiliaria.domain.repository.UserRepositoryPort;
import com.wasiq.inmobiliaria.infraestructure.adapters.output.persistence.entity.UserEntity;
import com.wasiq.inmobiliaria.infraestructure.adapters.output.persistence.repository.UserJpaRepository;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class UserPersistenceAdapter implements UserRepositoryPort {

    private final UserJpaRepository userJpaRepository;

    public UserPersistenceAdapter(UserJpaRepository userJpaRepository) {
        this.userJpaRepository = userJpaRepository;
    }

    @Override
    public User save(User user) {
        // Convertir Dominio a Entidad
        UserEntity userEntity = toEntity(user);
        // Guardar en BD
        UserEntity savedEntity = userJpaRepository.save(userEntity);
        // Convertir Entidad guardada -> Dominio
        return toDomain(savedEntity);
    }

    @Override
    public Optional<User> findByEmail(String email) {

        return userJpaRepository.findByEmail(email).map(this::toDomain);
    }

    //? METODOS DE CONVERSION

    private UserEntity toEntity(User user) {
        UserEntity entity = new UserEntity();
        //Mapeo de atributos
        entity.setId(user.getId());
        entity.setEmail(user.getEmail());
        entity.setPassword(user.getPassword());
        entity.setGoogleId(user.getGoogleId());
        entity.setFullName(user.getFullName());
        entity.setPictureUrl(user.getPictureUrl());
        entity.setPhoneNumber(user.getPhoneNumber());
        entity.setRole(user.getRole());
        entity.setActive(user.isActive());
        entity.setCreatedAt(user.getCreatedAt());

        return entity;
    }

    private User toDomain(UserEntity entity) {
        return new User(
                entity.getId(),
                entity.getEmail(),
                entity.getGoogleId(),
                entity.getFullName(),
                entity.getPictureUrl(),
                entity.getPhoneNumber(),
                entity.getPassword(),
                entity.getRole(),
                entity.getCreatedAt(),
                entity.isActive()
        );
    }

}
