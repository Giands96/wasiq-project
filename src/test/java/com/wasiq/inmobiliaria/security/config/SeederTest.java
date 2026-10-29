package com.wasiq.inmobiliaria.security.config;

import com.wasiq.inmobiliaria.user.model.User;
import com.wasiq.inmobiliaria.user.model.enums.Role;
import com.wasiq.inmobiliaria.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(OutputCaptureExtension.class)
class SeederTest {

    private static final String ADMIN_EMAIL = "admin@test.com";
    private static final String ADMIN_PASSWORD = "configured-admin-password";

    private UserRepository userRepository;
    private PasswordEncoder passwordEncoder;

    @BeforeEach
    void setUp() {
        userRepository = mock(UserRepository.class);
        passwordEncoder = mock(PasswordEncoder.class);
        when(userRepository.findByEmail(ADMIN_EMAIL)).thenReturn(Optional.empty());
        when(passwordEncoder.encode(ADMIN_PASSWORD)).thenReturn("encoded");
    }

    private Seeder seeder(String password) {
        Seeder seeder = new Seeder(userRepository, passwordEncoder);
        ReflectionTestUtils.setField(seeder, "adminEmail", ADMIN_EMAIL);
        ReflectionTestUtils.setField(seeder, "adminPassword", password);
        return seeder;
    }

    @Test
    void createsAdminWithConfiguredPasswordWithoutLoggingIt(CapturedOutput output) throws Exception {
        seeder(ADMIN_PASSWORD).run();

        ArgumentCaptor<User> saved = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(saved.capture());
        assertEquals("encoded", saved.getValue().getPassword());
        assertEquals(Role.ADMIN, saved.getValue().getRole());
        assertFalse(output.getAll().contains(ADMIN_PASSWORD), output.getAll());
    }

    @Test
    void skipsAdminCreationWhenPasswordIsMissing(CapturedOutput output) throws Exception {
        seeder("").run();

        verify(userRepository, never()).save(any());
        assertTrue(output.getAll().contains("ADMIN_PASSWORD"), output.getAll());
    }

    @Test
    void skipsAdminCreationWhenPasswordIsTooShort() throws Exception {
        seeder("short").run();

        verify(userRepository, never()).save(any());
    }
}
