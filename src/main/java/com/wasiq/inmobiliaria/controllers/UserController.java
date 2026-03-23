package com.wasiq.inmobiliaria.controllers;

import com.wasiq.inmobiliaria.auth.dto.AuthResponse;
import com.wasiq.inmobiliaria.auth.dto.UpdateUserRequest;
import com.wasiq.inmobiliaria.auth.service.AuthService;
import com.wasiq.inmobiliaria.models.Role;
import com.wasiq.inmobiliaria.models.User;
import com.wasiq.inmobiliaria.services.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;


@RestController
@RequiredArgsConstructor
@RequestMapping("/users")
public class UserController {
    private final UserService userService;
    private final AuthService authService;

    @GetMapping("/by-role")
    @PreAuthorize("hasAnyAuthority('ADMIN')")
    public ResponseEntity<Page<User>> findByRole(Pageable pageable, Role role){
        return ResponseEntity.ok().body(userService.findByRole(
                role,
                pageable.getPageNumber(),
                pageable.getPageSize()
        ));
    }




    @GetMapping("/all-desc")
    @PreAuthorize("hasAnyAuthority('ADMIN')")
    public ResponseEntity<Page<User>> findAllByOrderDesc(Pageable pageable){
        return ResponseEntity.ok().body(userService.findAllByOrderByIdDesc(
                pageable.getPageNumber(),
                pageable.getPageSize()
        ));
    }

}
