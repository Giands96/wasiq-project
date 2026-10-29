package com.wasiq.inmobiliaria.auth.exceptions;

import com.wasiq.inmobiliaria.shared.dto.ApiErrorResponse;
import com.wasiq.inmobiliaria.shared.exceptions.TooManyRequestsException;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletRequest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class GlobalExceptionHandlerTest {

    @Test
    void tooManyRequestsBecomes429WithMessage() {
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/auth/login");

        ResponseEntity<ApiErrorResponse> response = new GlobalExceptionHandler()
                .handleTooManyRequests(new TooManyRequestsException("Demasiados intentos"), request);

        assertEquals(429, response.getStatusCode().value());
        ApiErrorResponse body = response.getBody();
        assertNotNull(body);
        assertEquals(429, body.getStatus());
        assertEquals("Demasiados intentos", body.getMessage());
        assertEquals("/auth/login", body.getPath());
    }
}
