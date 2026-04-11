package com.capg.JobService.controller;

import com.capg.JobService.dto.JobFilterDto;
import com.capg.JobService.dto.JobRequestDto;
import com.capg.JobService.dto.JobResponseDto;
import com.capg.JobService.exceptions.JobNotFoundException;
import com.capg.JobService.service.JobService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class JobControllerTest {

    @Mock
    private JobService jobService;

    @InjectMocks
    private JobController jobController;

    @Test
    @DisplayName("createJob should return 201 and created body")
    void createJob_success() {
        JobRequestDto request = buildRequest();
        JobResponseDto created = buildResponse(101L);
        when(jobService.createJob(eq(request), eq(10L), eq("RECRUITER"))).thenReturn(created);

        ResponseEntity<JobResponseDto> response = jobController.createJob(request, 10L, "RECRUITER");

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals(101L, response.getBody().getId());
    }

    @Test
    @DisplayName("getAllJobs should return 200 and page payload")
    void getAllJobs_success() {
        Page<JobResponseDto> page = new PageImpl<>(List.of(buildResponse(201L)));
        when(jobService.getAllJobs(0, 10, "createdAt", "desc")).thenReturn(page);

        ResponseEntity<Page<JobResponseDto>> response = jobController.getAllJobs(0, 10, "createdAt", "desc");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(1, response.getBody().getTotalElements());
    }

    @Test
    @DisplayName("getJobsByRecruiterId should return 200 and list payload")
    void getJobsByRecruiterId_success() {
        when(jobService.getJobsByRecruiterId(55L)).thenReturn(List.of(buildResponse(701L), buildResponse(702L)));

        ResponseEntity<List<JobResponseDto>> response = jobController.getJobsByRecruiterId(55L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(2, response.getBody().size());
        assertEquals(701L, response.getBody().get(0).getId());
    }

    @Test
    @DisplayName("getJobById should return 200 and job body")
    void getJobById_success() {
        when(jobService.getJobById(301L)).thenReturn(buildResponse(301L));

        ResponseEntity<JobResponseDto> response = jobController.getJobById(301L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(301L, response.getBody().getId());
    }

    @Test
    @DisplayName("updateJob should return 200 and updated job body")
    void updateJob_success() {
        JobRequestDto request = buildRequest();
        when(jobService.updateJob(401L, request, 99L)).thenReturn(buildResponse(401L));

        ResponseEntity<JobResponseDto> response = jobController.updateJob(401L, request, 99L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(401L, response.getBody().getId());
    }

    @Test
    @DisplayName("deleteJob should return 204 and invoke service")
    void deleteJob_success() {
        ResponseEntity<Void> response = jobController.deleteJob(501L, 99L);

        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        verify(jobService).deleteJob(501L, 99L);
    }

    @Test
    @DisplayName("searchJobs should return 200 and filtered page")
    void searchJobs_success() {
        JobFilterDto filter = new JobFilterDto();
        filter.setLocation("Pune");
        Page<JobResponseDto> page = new PageImpl<>(List.of(buildResponse(601L)));
        when(jobService.searchJobs(filter, 0, 10, "createdAt", "desc")).thenReturn(page);

        ResponseEntity<Page<JobResponseDto>> response =
                jobController.searchJobs(filter, 0, 10, "createdAt", "desc");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(1, response.getBody().getTotalElements());
    }

    @Test
    @DisplayName("getJobById should propagate service exception for missing job (edge case)")
    void getJobById_notFound_propagatesException() {
        when(jobService.getJobById(999L)).thenThrow(new JobNotFoundException("Job not found"));

        JobNotFoundException ex = assertThrows(JobNotFoundException.class, () -> jobController.getJobById(999L));

        assertEquals("Job not found", ex.getMessage());
    }

    @Test
    @DisplayName("createJob should pass through null role to service (edge case)")
    void createJob_nullRole_passedToService() {
        JobRequestDto request = buildRequest();
        when(jobService.createJob(eq(request), eq(10L), eq(null))).thenReturn(buildResponse(102L));

        ResponseEntity<JobResponseDto> response = jobController.createJob(request, 10L, null);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        verify(jobService).createJob(eq(request), eq(10L), eq(null));
    }

    private JobRequestDto buildRequest() {
        JobRequestDto dto = new JobRequestDto();
        dto.setTitle("Java Developer");
        dto.setCompanyName("Acme");
        dto.setLocation("Pune");
        dto.setSalary(10.0);
        dto.setExperience(3);
        dto.setDescription("Backend role");
        dto.setRecruiterEmail("recruiter@acme.com");
        dto.setJobType("FULL_TIME");
        return dto;
    }

    private JobResponseDto buildResponse(Long id) {
        JobResponseDto dto = new JobResponseDto();
        dto.setId(id);
        dto.setTitle("Java Developer");
        dto.setJobType("FULL_TIME");
        return dto;
    }
}
