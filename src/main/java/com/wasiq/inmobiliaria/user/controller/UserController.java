package com.wasiq.inmobiliaria.user.controller;

import com.wasiq.inmobiliaria.auth.service.AuthService;
import com.wasiq.inmobiliaria.user.model.enums.Role;
import com.wasiq.inmobiliaria.user.model.User;
import com.wasiq.inmobiliaria.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;


@RestController
@RequiredArgsConstructor
@RequestMapping("/users")
public class UserController {
    private final UserService userService;
    private final AuthService authService;

    @GetMapping("/all")
    @PreAuthorize("hasAnyAuthority('ADMIN')")
    public ResponseEntity<Page<User>> findAll(Pageable pageable){
        return ResponseEntity.ok().body(userService.findAllByOrderByIdDesc(
                pageable.getPageNumber(),
                pageable.getPageSize()
        ));
    }

    @GetMapping("/by-role/{role}")
    @PreAuthorize("hasAnyAuthority('ADMIN')")
    public ResponseEntity<Page<User>> findByRole(Pageable pageable, @PathVariable Role role){
        return ResponseEntity.ok().body(userService.findByRole(
                role,
                pageable.getPageNumber(),
                pageable.getPageSize()
        ));
    }

}
