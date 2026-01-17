package com.wasiq.inmobiliaria.infraestructure.adapters.output.persistence.repository;

import com.wasiq.inmobiliaria.infraestructure.adapters.output.persistence.entity.PostEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PostJpaRepository extends JpaRepository<PostEntity, Long> {

}
