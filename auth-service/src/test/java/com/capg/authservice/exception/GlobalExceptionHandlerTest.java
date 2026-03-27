package com.capg.authservice.exception;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GlobalExceptionHandlerTest {

    @Mock
    private MethodArgumentNotValidException methodArgumentNotValidException;
    @Mock
    private BindingResult bindingResult;

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    void handleUserNotFoundShouldReturn404() {
        ResponseEntity<Map<String, String>> response =
                handler.handleUserNotFound(new UserNotFoundException("User not found with id: 1"));

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertEquals("User not found with id: 1", response.getBody().get("error"));
    }

    @Test
    void handleDuplicateEmailShouldReturn409() {
        ResponseEntity<Map<String, String>> response =
                handler.handleDuplicateEmail(new DuplicateEmailException("Email already registered"));

        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        assertEquals("Email already registered", response.getBody().get("error"));
    }

    @Test
    void handleInvalidCredentialsShouldReturn401() {
        ResponseEntity<Map<String, String>> response =
                handler.handleInvalidCredentials(new InvalidCredentialsException("Invalid email or password"));

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertEquals("Invalid email or password", response.getBody().get("error"));
    }

    @Test
    void handleValidationForFieldErrorsShouldReturn400WithFieldMap() {
        when(methodArgumentNotValidException.getBindingResult()).thenReturn(bindingResult);
        when(bindingResult.getFieldErrors()).thenReturn(List.of(
                new FieldError("registerRequest", "email", "Invalid email format"),
                new FieldError("registerRequest", "password", "Password must be at least 6 characters")
        ));

        ResponseEntity<Map<String, String>> response = handler.handleValidation(methodArgumentNotValidException);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Invalid email format", response.getBody().get("email"));
        assertEquals("Password must be at least 6 characters", response.getBody().get("password"));
    }

    @Test
    void handleRestrictedUserShouldReturn400WithMessage() {
        ResponseEntity<String> response = handler.handleValidation(new RestrictedUser("Cannot assign ADMIN role"));

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Cannot assign ADMIN role", response.getBody());
    }

    @Test
    void handleIllegalArgumentShouldReturn400WithMessage() {
        ResponseEntity<String> response = handler.handleValidation(new IllegalArgumentException("Invalid Role"));

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Invalid Role", response.getBody());
    }

    @Test
    void handleAccessDeniedShouldReturn403() {
        ResponseEntity<Map<String, String>> response =
                handler.handleAccessDenied(new AccessDeniedException("Forbidden"));

        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        assertEquals("Forbidden", response.getBody().get("error"));
    }
}
