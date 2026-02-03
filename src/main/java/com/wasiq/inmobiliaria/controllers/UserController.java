package com.wasiq.inmobiliaria.controllers;

import com.wasiq.inmobiliaria.models.Role;
import com.wasiq.inmobiliaria.models.User;
import com.wasiq.inmobiliaria.services.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequiredArgsConstructor
@RequestMapping("/users")
public class UserController {

    private final UserService userService;

    @GetMapping("/by-role")
    public ResponseEntity<Page<User>> findByRole(Pageable pageable, Role role){
        return ResponseEntity.ok().body(userService.findByRole(
                role,
                pageable.getPageNumber(),
                pageable.getPageSize()
        ));
    }

    @GetMapping("/all-desc")
    public ResponseEntity<Page<User>> findAllByOrderDesc(Pageable pageable){
        return ResponseEntity.ok().body(userService.findAllByOrderByIdDesc(
                pageable.getPageNumber(),
                pageable.getPageSize()
        ));
    }

}
