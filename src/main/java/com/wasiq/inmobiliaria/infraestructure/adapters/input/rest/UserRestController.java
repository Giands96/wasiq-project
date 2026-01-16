package com.wasiq.inmobiliaria.infraestructure.adapters.input.rest;

import com.wasiq.inmobiliaria.application.ports.input.LoginUserUseCase;
import com.wasiq.inmobiliaria.application.ports.input.RegisterUserUseCase;
import com.wasiq.inmobiliaria.application.ports.input.command.LoginUserCommand;
import com.wasiq.inmobiliaria.application.ports.input.command.RegisterUserCommand;
import com.wasiq.inmobiliaria.application.service.UserService;
import com.wasiq.inmobiliaria.domain.model.user.User;
import jakarta.validation.Valid;
import org.apache.coyote.Response;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collections;
import java.util.Map;

@RestController
@RequestMapping("/api/users")
public class UserRestController {

    private final RegisterUserUseCase registerUserUseCase;
    private final LoginUserUseCase loginUserUseCase;

    public UserRestController(RegisterUserUseCase registerUserUseCase, LoginUserUseCase loginUserUseCase) {
        this.registerUserUseCase = registerUserUseCase;
        this.loginUserUseCase = loginUserUseCase;
    }

    @PostMapping("/register")
    public ResponseEntity<User> register(@Valid @RequestBody RegisterUserCommand command) {
        User createdUser = registerUserUseCase.registerUser(command);
        return new ResponseEntity<>(createdUser, HttpStatus.CREATED);
    }

    @PostMapping("/login")
    public ResponseEntity<Map<String, String>> login(@Valid @RequestBody LoginUserCommand command) {
        String token = loginUserUseCase.login(command);
        return ResponseEntity.ok(Collections.singletonMap("token", token));
    }
}
