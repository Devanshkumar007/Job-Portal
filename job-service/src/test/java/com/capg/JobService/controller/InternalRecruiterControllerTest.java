package com.capg.JobService.controller;

import com.capg.JobService.dto.RecruiterJobSummaryDto;
import com.capg.JobService.dto.RecruiterOpenRolesCountDto;
import com.capg.JobService.dto.RecruiterRecentJobDto;
import com.capg.JobService.exceptions.UnauthorizedException;
import com.capg.JobService.service.JobService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class InternalRecruiterControllerTest {

    @Mock
    private JobService jobService;

    @InjectMocks
    private InternalRecruiterController internalRecruiterController;

    @Test
    @DisplayName("getRecruiterJobIds should return 200 with ids")
    void getRecruiterJobIds_success() {
        when(jobService.getRecruiterJobIds(10L, 10L, "RECRUITER")).thenReturn(List.of(101L, 102L, 103L));

        ResponseEntity<List<Long>> response = internalRecruiterController.getRecruiterJobIds(10L, "RECRUITER", 10L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(List.of(101L, 102L, 103L), response.getBody());
    }

    @Test
    @DisplayName("getRecruiterOpenRolesCount should return 200 with count")
    void getRecruiterOpenRolesCount_success() {
        when(jobService.getRecruiterOpenRolesCount(11L, 11L, "RECRUITER")).thenReturn(new RecruiterOpenRolesCountDto(12));

        ResponseEntity<RecruiterOpenRolesCountDto> response =
                internalRecruiterController.getRecruiterOpenRolesCount(11L, "RECRUITER", 11L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(12L, response.getBody().getOpenRoles());
    }

    @Test
    @DisplayName("getRecruiterRecentJobs should pass default-like limit and return 200")
    void getRecruiterRecentJobs_success() {
        List<RecruiterRecentJobDto> recent = List.of(
                new RecruiterRecentJobDto(101L, "Java Senior Developer", "Oracle", "ACTIVE")
        );
        when(jobService.getRecruiterRecentJobs(12L, 5, 12L, "RECRUITER")).thenReturn(recent);

        ResponseEntity<List<RecruiterRecentJobDto>> response =
                internalRecruiterController.getRecruiterRecentJobs(12L, "RECRUITER", 12L, 5);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(1, response.getBody().size());
        assertEquals("ACTIVE", response.getBody().get(0).getStatus());
    }

    @Test
    @DisplayName("getRecruiterJobIds should propagate forbidden from service")
    void getRecruiterJobIds_forbidden() {
        when(jobService.getRecruiterJobIds(eq(13L), eq(13L), eq("CANDIDATE")))
                .thenThrow(new UnauthorizedException("Only recruiters or internal service can access this endpoint"));

        UnauthorizedException ex = assertThrows(
                UnauthorizedException.class,
                () -> internalRecruiterController.getRecruiterJobIds(13L, "CANDIDATE", 13L)
        );

        assertEquals("Only recruiters or internal service can access this endpoint", ex.getMessage());
        verify(jobService).getRecruiterJobIds(13L, 13L, "CANDIDATE");
    }

    @Test
    @DisplayName("getRecruiterJobSummary should return 200 with summary")
    void getRecruiterJobSummary_success() {
        RecruiterJobSummaryDto summary = new RecruiterJobSummaryDto(12L, 8L, 3L, 1L);
        when(jobService.getRecruiterJobSummary(77L, null, "INTERNAL_SERVICE")).thenReturn(summary);

        ResponseEntity<RecruiterJobSummaryDto> response =
                internalRecruiterController.getRecruiterJobSummary(77L, "INTERNAL_SERVICE", null);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(12L, response.getBody().getTotalJobs());
    }
}
