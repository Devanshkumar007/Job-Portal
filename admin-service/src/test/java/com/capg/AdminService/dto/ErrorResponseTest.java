package com.capg.AdminService.dto;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

class ErrorResponseTest {

    @Test
    @DisplayName("all-args constructor should populate message status and timestamp")
    void constructor_ShouldPopulateAllFields() {
        LocalDateTime now = LocalDateTime.of(2026, 3, 24, 12, 0);
        ErrorResponse response = new ErrorResponse("Forbidden", 403, now);

        assertEquals("Forbidden", response.getMessage());
        assertEquals(403, response.getStatus());
        assertEquals(now, response.getTimestamp());
    }

    @Test
    @DisplayName("equals and hashCode should match for identical error payloads")
    void equalsAndHashCode_ShouldMatch_WhenValuesAreSame() {
        LocalDateTime now = LocalDateTime.of(2026, 3, 24, 12, 0);
        ErrorResponse first = new ErrorResponse("Bad Request", 400, now);
        ErrorResponse second = new ErrorResponse("Bad Request", 400, now);

        assertEquals(first, second);
        assertEquals(first.hashCode(), second.hashCode());
    }

    @Test
    @DisplayName("equals should fail when status differs even if message is same")
    void equals_ShouldNotMatch_WhenStatusDiffers() {
        LocalDateTime now = LocalDateTime.of(2026, 3, 24, 12, 0);
        ErrorResponse first = new ErrorResponse("Error", 400, now);
        ErrorResponse second = new ErrorResponse("Error", 500, now);

        assertNotEquals(first, second);
    }
}
