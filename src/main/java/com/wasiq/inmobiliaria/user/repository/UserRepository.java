package com.wasiq.inmobiliaria.user.repository;

import com.wasiq.inmobiliaria.user.model.enums.Role;
import com.wasiq.inmobiliaria.user.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);
    Page<User> findByRole(Role role, Pageable pageable);
    Page<User> findAllByOrderByIdDesc(Pageable pageable);

}
