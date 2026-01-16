package com.wasiq.inmobiliaria.infraestructure.adapters.output.persistence.entity;

import com.wasiq.inmobiliaria.domain.model.user.UserRole;
import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Entity
@Data
@Table(name = "users")
public class UserEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 255,nullable = false, unique = true)
    private String email;

    @Column(name = "google_id")
    private String googleId;

    @Column(name = "full_name", length = 255)
    private String fullName;

    private String pictureUrl;

    @Column(name = "password", length = 100)
    private String password;

    @Column(name = "phone_number", length = 20)
    private String phoneNumber;

    @Enumerated(EnumType.STRING)
    private UserRole role;

    private boolean active;
    private LocalDateTime createdAt;

}
