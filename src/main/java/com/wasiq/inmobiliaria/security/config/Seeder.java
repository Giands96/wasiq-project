package com.wasiq.inmobiliaria.security.config;

import com.wasiq.inmobiliaria.user.model.enums.Role;
import com.wasiq.inmobiliaria.user.model.User;
import com.wasiq.inmobiliaria.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class Seeder implements CommandLineRunner {

    private static final int MIN_ADMIN_PASSWORD_LENGTH = 12;

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${admin.email:admin@wasiq.com}")
    private String adminEmail;

    //* Supplied by the operator; credentials are never generated or written to the logs */
    @Value("${admin.password:}")
    private String adminPassword;


    @Override
    public void run(String... args) throws Exception {

        // 1. Verificamos si el usuario ya existe en la base de datos
        if (userRepository.findByEmail(adminEmail).isPresent()) {
            log.info("✅ El usuario admin ya existe en la base de datos. Se omitió la creación.");
            return;
        }

        // 2. Sin una contraseña configurada no se crea el admin
        if (adminPassword == null || adminPassword.length() < MIN_ADMIN_PASSWORD_LENGTH) {
            log.warn("No se creó el usuario admin: definí ADMIN_PASSWORD con al menos {} caracteres.",
                    MIN_ADMIN_PASSWORD_LENGTH);
            return;
        }

        // 3. Construimos el usuario
        User admin = User.builder()
                .firstName("Super")
                .lastName("Admin")
                .email(adminEmail)
                .password(passwordEncoder.encode(adminPassword)) // Encriptamos la contraseña
                .role(Role.ADMIN)
                .phoneNumber("+51000000000")
                .active(true)
                .build();

        // 4. Guardamos en la base de datos
        userRepository.save(admin);
        log.info("🛡️ Usuario admin creado: {}", adminEmail);
    }
}
