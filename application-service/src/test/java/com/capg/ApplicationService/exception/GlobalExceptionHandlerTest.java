package com.capg.ApplicationService.exception;

import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.assertEquals;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    @Order(1)
    void handleNotFound_returns_404_and_original_message() {
        ResponseEntity<String> response =
                handler.handleNotFound(new ResourceNotFoundException("Application not found"));

        assertStatus(404, response.getStatusCode());
        assertEquals("Application not found", response.getBody());
    }

    @Test
    @Order(2)
    void handleDuplicate_returns_400_and_original_message() {
        ResponseEntity<String> response =
                handler.handleDuplicate(new DuplicateApplicationException("Already applied"));

        assertStatus(400, response.getStatusCode());
        assertEquals("Already applied", response.getBody());
    }

    @Test
    @Order(3)
    void handleUnauthorized_returns_403_and_original_message() {
        ResponseEntity<String> response =
                handler.handleUnauthorized(new UnauthorizedException("Only recruiters allowed"));

        assertStatus(403, response.getStatusCode());
        assertEquals("Only recruiters allowed", response.getBody());
    }

    private void assertStatus(int expected, HttpStatusCode actual) {
        assertEquals(expected, actual.value());
    }
}
