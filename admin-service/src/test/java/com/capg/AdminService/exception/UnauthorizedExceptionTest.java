package com.capg.AdminService.exception;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class UnauthorizedExceptionTest {

    @Test
    @DisplayName("exception should retain the provided message")
    void shouldRetainProvidedMessage() {
        UnauthorizedException exception = new UnauthorizedException("Only admins can access this resource");

        assertEquals("Only admins can access this resource", exception.getMessage());
    }
}
