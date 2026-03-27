package com.capg.JobService.exceptions;

import com.capg.JobService.dto.ErrorResponse;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    void handleJobNotFound_shouldReturn404WithMessage() {
        ResponseEntity<ErrorResponse> response =
                handler.handleJobNotFound(new JobNotFoundException("Job not found"));

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(404, response.getBody().getStatus());
        assertEquals("Job not found", response.getBody().getMessage());
        assertNotNull(response.getBody().getTimestamp());
    }

    @Test
    void handleRuntime_shouldReturn403WithMessage() {
        ResponseEntity<ErrorResponse> response =
                handler.handleRuntime(new UnauthorizedException("Forbidden"));

        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(403, response.getBody().getStatus());
        assertEquals("Forbidden", response.getBody().getMessage());
        assertNotNull(response.getBody().getTimestamp());
    }
}
