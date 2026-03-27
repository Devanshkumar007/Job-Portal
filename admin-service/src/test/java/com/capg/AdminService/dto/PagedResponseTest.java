package com.capg.AdminService.dto;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PagedResponseTest {

    @Test
    @DisplayName("setters and getters should preserve pagination metadata and content")
    void settersAndGetters_ShouldPreserveValues() {
        PagedResponse<String> response = new PagedResponse<>();

        response.setContent(List.of("A", "B"));
        response.setNumber(2);
        response.setSize(10);
        response.setTotalElements(42);
        response.setTotalPages(5);
        response.setFirst(false);
        response.setLast(false);
        response.setNumberOfElements(2);
        response.setEmpty(false);

        assertEquals(List.of("A", "B"), response.getContent());
        assertEquals(2, response.getNumber());
        assertEquals(10, response.getSize());
        assertEquals(42, response.getTotalElements());
        assertEquals(5, response.getTotalPages());
        assertFalse(response.isFirst());
        assertFalse(response.isLast());
        assertEquals(2, response.getNumberOfElements());
        assertFalse(response.isEmpty());
    }

    @Test
    @DisplayName("empty page flags should support first and last page edge case")
    void emptyFlags_ShouldSupportFirstAndLastPageEdgeCase() {
        PagedResponse<Object> response = new PagedResponse<>();
        response.setContent(List.of());
        response.setFirst(true);
        response.setLast(true);
        response.setEmpty(true);
        response.setNumberOfElements(0);

        assertTrue(response.getContent().isEmpty());
        assertTrue(response.isFirst());
        assertTrue(response.isLast());
        assertTrue(response.isEmpty());
        assertEquals(0, response.getNumberOfElements());
    }
}
