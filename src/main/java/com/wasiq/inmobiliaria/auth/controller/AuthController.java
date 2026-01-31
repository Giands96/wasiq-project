package com.wasiq.inmobiliaria.auth.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
public class AuthController {

    public String testAuth() {
        return "Auth Controller is working!";
    }
}
