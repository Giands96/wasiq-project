package com.wasiq.inmobiliaria.user.controller;

import com.wasiq.inmobiliaria.user.dto.AdminUserResponse;
import com.wasiq.inmobiliaria.user.model.User;
import com.wasiq.inmobiliaria.user.model.enums.Role;
import com.wasiq.inmobiliaria.user.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import tools.jackson.databind.json.JsonMapper;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class UserControllerTest {

    private final Pageable pageable = PageRequest.of(0, 10);
    private UserService userService;
    private UserController controller;
    private User user;

    @BeforeEach
    void setUp() {
        userService = mock(UserService.class);
        controller = new UserController(userService, null);
        user = User.builder()
                .id(7L)
                .firstName("Ana")
                .lastName("Perez")
                .email("ana@test.com")
                .password("$2a$10$secret-hash")
                .phoneNumber("+51999")
                .role(Role.USER)
                .active(true)
                .build();
    }

    private void assertSafe(AdminUserResponse response) {
        assertEquals(7L, response.getId());
        assertEquals("ana@test.com", response.getEmail());
        assertEquals("+51999", response.getPhoneNumber());
        String json = JsonMapper.builder().build().writeValueAsString(response);
        assertFalse(json.contains("secret-hash"), json);
        assertFalse(json.contains("password"), json);
    }

    @Test
    void findByIdDoesNotExposePasswordHash() {
        when(userService.getById(7L)).thenReturn(user);

        assertSafe(controller.findById(7L).getBody());
    }

    @Test
    void findAllDoesNotExposePasswordHash() {
        when(userService.findAllByOrderByIdDesc(0, 10)).thenReturn(new PageImpl<>(List.of(user)));

        Page<AdminUserResponse> page = controller.findAll(pageable).getBody();

        assertSafe(page.getContent().getFirst());
    }

    @Test
    void findByRoleDoesNotExposePasswordHash() {
        when(userService.findByRole(Role.USER, 0, 10)).thenReturn(new PageImpl<>(List.of(user)));

        Page<AdminUserResponse> page = controller.findByRole(pageable, Role.USER).getBody();

        assertSafe(page.getContent().getFirst());
    }

    @Test
    void userEntityNeverSerializesPasswordHash() {
        String json = JsonMapper.builder().build().writeValueAsString(
                User.builder().id(1L).email("a@b.com").password("$2a$10$secret-hash").role(Role.USER).build());

        assertFalse(json.contains("secret-hash"), json);
    }
}
