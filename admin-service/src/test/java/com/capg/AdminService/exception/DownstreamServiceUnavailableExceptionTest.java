package com.capg.AdminService.exception;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

class DownstreamServiceUnavailableExceptionTest {

    @Test
    @DisplayName("exception should retain message and cause for downstream failures")
    void shouldRetainMessageAndCause() {
        RuntimeException cause = new RuntimeException("timeout");
        DownstreamServiceUnavailableException exception =
                new DownstreamServiceUnavailableException(
                        "Job service unreachable. Please try again after some time.", cause);

        assertEquals("Job service unreachable. Please try again after some time.", exception.getMessage());
        assertSame(cause, exception.getCause());
    }
}
