package com.wasiq.inmobiliaria.auth.exceptions;

import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler extends RuntimeException {
    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<String> handleBadCredentials(BadCredentialsException e) {
        System.out.println("BadCredentials: " + e.getMessage());
        return ResponseEntity.status(401).body("Credenciales incorrectas");
    }

    @ExceptionHandler(DisabledException.class)
    public ResponseEntity<String> handleDisabled(DisabledException e) {
        System.out.println(" Usuario deshabilitado (active=false): " + e.getMessage());
        return ResponseEntity.status(403).body("Usuario deshabilitado");
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<String> handleGeneral(Exception e) {
        System.out.println("Error general: " + e.getClass().getName() + " - " + e.getMessage());
        return ResponseEntity.status(500).body(e.getMessage());
    }
}
