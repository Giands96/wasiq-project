package com.wasiq.inmobiliaria.repository;

import com.wasiq.inmobiliaria.models.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);
    Page<User> findByRole(String role, Pageable pageable);


}
