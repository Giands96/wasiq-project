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

import java.security.SecureRandom;
import java.util.Base64;

@Component
@RequiredArgsConstructor
@Slf4j
public class Seeder implements CommandLineRunner {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${admin.email:admin@wasiq.com}")
    private String adminEmail;


    @Override
    public void run(String... args) throws Exception {

        // 1. Verificamos si el usuario ya existe en la base de datos
        if (userRepository.findByEmail(adminEmail).isEmpty()) {

            // 2. Generamos una contraseña aleatoria segura de 12 caracteres
            String randomPassword = generateSecurePassword(12);

            // 3. Construimos el usuario
            User admin = User.builder()
                    .firstName("Super")
                    .lastName("Admin")
                    .email(adminEmail)
                    .password(passwordEncoder.encode(randomPassword)) // Encriptamos la contraseña
                    .role(Role.ADMIN)
                    .phoneNumber("+51000000000")
                    .active(true)
                    .build();

            // 4. Guardamos en la base de datos
            userRepository.save(admin);

            // 5. Imprimimos las credenciales en la consola de Spring Boot
            log.info("=====================================================");
            log.info("🛡️ ¡USUARIO ADMIN GENERADO AUTOMÁTICAMENTE!");
            log.info("✉️ Correo: {}", adminEmail);
            log.info("🔑 Contraseña: {}", randomPassword);
            log.info("⚠️ GUARDA ESTA CONTRASEÑA. NO SE VOLVERÁ A MOSTRAR.");
            log.info("=====================================================");
        } else {
            log.info("✅ El usuario admin ya existe en la base de datos. Se omitió la creación.");
        }
    }

    /**
     * Método auxiliar para generar una contraseña criptográficamente segura
     */
    private String generateSecurePassword(int length) {
        SecureRandom secureRandom = new SecureRandom();
        byte[] randomBytes = new byte[length];
        secureRandom.nextBytes(randomBytes);
        // Usamos Base64 para convertir los bytes en caracteres legibles (A-Z, a-z, 0-9)
        // Quitamos los caracteres especiales como = o + para evitar problemas al copiar
        return Base64.getUrlEncoder().withoutPadding().encodeToString(randomBytes).substring(0, length);
    }
}
