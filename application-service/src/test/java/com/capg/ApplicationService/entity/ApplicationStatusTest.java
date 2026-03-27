package com.capg.ApplicationService.entity;

import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class ApplicationStatusTest {

    @Test
    @Order(1)
    void valueOf_accepts_valid_status() {
        ApplicationStatus status = ApplicationStatus.valueOf("APPLIED");
        assertEquals(ApplicationStatus.APPLIED, status);
    }

    @Test
    @Order(2)
    void valueOf_rejects_invalid_status_edge_case() {
        assertThrows(IllegalArgumentException.class,
                () -> ApplicationStatus.valueOf("applied"));
    }
}
