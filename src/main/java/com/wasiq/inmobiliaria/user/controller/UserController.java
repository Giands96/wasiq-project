package com.wasiq.inmobiliaria.user.controller;

import com.wasiq.inmobiliaria.auth.dto.UserResponse;
import com.wasiq.inmobiliaria.auth.service.AuthService;
import com.wasiq.inmobiliaria.user.model.enums.Role;
import com.wasiq.inmobiliaria.user.model.User;
import com.wasiq.inmobiliaria.user.dto.UpdateRoleRequest;
import com.wasiq.inmobiliaria.user.dto.UpdateUserStatusRequest;
import com.wasiq.inmobiliaria.user.service.UserService;
import jakarta.validation.Valid;
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
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<Page<User>> findAll(Pageable pageable){
        return ResponseEntity.ok().body(userService.findAllByOrderByIdDesc(
                pageable.getPageNumber(),
                pageable.getPageSize()
        ));
    }

    @GetMapping("/by-role/{role}")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<Page<User>> findByRole(Pageable pageable, @PathVariable Role role){
        return ResponseEntity.ok().body(userService.findByRole(
                role,
                pageable.getPageNumber(),
                pageable.getPageSize()
        ));
    }

    @PutMapping("/{id}/role")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<UserResponse> updateRole(@PathVariable Long id,
                                                   @Valid @RequestBody UpdateRoleRequest request) {
        User user = userService.updateRole(id, request.getRole());
        return ResponseEntity.ok(authService.buildAuthResponse(user).getUser());
    }

    @PutMapping("/{id}/status")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<UserResponse> updateStatus(@PathVariable Long id,
                                                     @Valid @RequestBody UpdateUserStatusRequest request) {
        User user = userService.updateStatus(id, request.getActive());
        return ResponseEntity.ok(authService.buildAuthResponse(user).getUser());
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        userService.delete(id);
        return ResponseEntity.noContent().build();
    }

}