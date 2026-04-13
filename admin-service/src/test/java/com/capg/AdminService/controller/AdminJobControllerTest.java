package com.capg.AdminService.controller;

import com.capg.AdminService.dto.JobResponseDto;
import com.capg.AdminService.dto.PagedResponse;
import com.capg.AdminService.exception.DownstreamServiceUnavailableException;
import com.capg.AdminService.exception.UnauthorizedException;
import com.capg.AdminService.service.AdminJobService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Controller tests for AdminJobController covering request handling, response codes,
 * and error scenarios for all job management endpoints.
 */
@WebMvcTest(AdminJobController.class)
class AdminJobControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AdminJobService adminJobService;

    private static final String BASE_URL = "/api/admin/jobs";
    private static final String ADMIN_ROLE = "ADMIN";

    @Nested
    @DisplayName("GetAllJobs Endpoint Tests")
    class GetAllJobsTests {

        @Test
        @DisplayName("Should return 200 OK with paged jobs")
        void shouldReturnOk_WithPagedJobs() throws Exception {
            // Arrange
            Long requesterId = 10L;
            PagedResponse<JobResponseDto> response = new PagedResponse<>();
            when(adminJobService.getAllJobs(ADMIN_ROLE, 0, 10, "appliedAt", "desc"))
                    .thenReturn(response);

            // Act & Assert
            mockMvc.perform(get(BASE_URL)
                    .header("X-User-Id", requesterId)
                    .header("X-User-Role", ADMIN_ROLE)
                    .param("page", "0")
                    .param("size", "10")
                    .param("sortBy", "appliedAt")
                    .param("direction", "desc"))
                    .andExpect(status().isOk());
            verify(adminJobService).getAllJobs(ADMIN_ROLE, 0, 10, "appliedAt", "desc");
        }

        @Test
        @DisplayName("Should use default parameters when not provided")
        void shouldUseDefaultParameters() throws Exception {
            // Arrange
            Long requesterId = 10L;
            PagedResponse<JobResponseDto> response = new PagedResponse<>();
            when(adminJobService.getAllJobs(ADMIN_ROLE, 0, 10, "createdAt", "desc"))
                    .thenReturn(response);

            // Act & Assert
            mockMvc.perform(get(BASE_URL)
                    .header("X-User-Id", requesterId)
                    .header("X-User-Role", ADMIN_ROLE))
                    .andExpect(status().isOk());
            verify(adminJobService).getAllJobs(ADMIN_ROLE, 0, 10, "createdAt", "desc");
        }

        @Test
        @DisplayName("Should return 403 Forbidden for non-admin role")
        void shouldReturnForbidden_ForNonAdminRole() throws Exception {
            // Arrange
            Long requesterId = 10L;
            doThrow(new UnauthorizedException("Only admins can access this resource"))
                    .when(adminJobService).getAllJobs(eq("RECRUITER"), anyInt(), anyInt(), anyString(), anyString());

            // Act & Assert
            mockMvc.perform(get(BASE_URL)
                    .header("X-User-Id", requesterId)
                    .header("X-User-Role", "RECRUITER"))
                    .andExpect(status().isForbidden())
                    .andExpect(jsonPath("$.status").value(403));
        }

        @Test
        @DisplayName("Should return 503 when service is unavailable")
        void shouldReturnServiceUnavailable_WhenDownstreamFails() throws Exception {
            // Arrange
            Long requesterId = 10L;
            doThrow(new DownstreamServiceUnavailableException("Job service unreachable"))
                    .when(adminJobService).getAllJobs(eq(ADMIN_ROLE), anyInt(), anyInt(), anyString(), anyString());

            // Act & Assert
            mockMvc.perform(get(BASE_URL)
                    .header("X-User-Id", requesterId)
                    .header("X-User-Role", ADMIN_ROLE))
                    .andExpect(status().isServiceUnavailable())
                    .andExpect(jsonPath("$.status").value(503));
        }

        @Test
        @DisplayName("Should support custom sorting parameters")
        void shouldSupportCustomSortingParameters() throws Exception {
            // Arrange
            Long requesterId = 10L;
            PagedResponse<JobResponseDto> response = new PagedResponse<>();
            when(adminJobService.getAllJobs(ADMIN_ROLE, 2, 25, "salary", "asc"))
                    .thenReturn(response);

            // Act & Assert
            mockMvc.perform(get(BASE_URL)
                    .header("X-User-Id", requesterId)
                    .header("X-User-Role", ADMIN_ROLE)
                    .param("page", "2")
                    .param("size", "25")
                    .param("sortBy", "salary")
                    .param("direction", "asc"))
                    .andExpect(status().isOk());
            verify(adminJobService).getAllJobs(ADMIN_ROLE, 2, 25, "salary", "asc");
        }
    }

    @Nested
    @DisplayName("GetJobById Endpoint Tests")
    class GetJobByIdTests {

        @Test
        @DisplayName("Should return 200 OK with job details")
        void shouldReturnOk_WithJobDetails() throws Exception {
            // Arrange
            Long requesterId = 10L;
            Long jobId = 22L;
            JobResponseDto response = new JobResponseDto();
            response.setId(jobId);
            when(adminJobService.getJobById(ADMIN_ROLE, jobId))
                    .thenReturn(response);

            // Act & Assert
            mockMvc.perform(get(BASE_URL + "/" + jobId)
                    .header("X-User-Id", requesterId)
                    .header("X-User-Role", ADMIN_ROLE))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(jobId));
            verify(adminJobService).getJobById(ADMIN_ROLE, jobId);
        }

        @Test
        @DisplayName("Should return 403 for non-admin role")
        void shouldReturnForbidden_ForNonAdminRole() throws Exception {
            // Arrange
            Long requesterId = 10L;
            Long jobId = 22L;
            doThrow(new UnauthorizedException("Only admins can access this resource"))
                    .when(adminJobService).getJobById(eq("USER"), eq(jobId));

            // Act & Assert
            mockMvc.perform(get(BASE_URL + "/" + jobId)
                    .header("X-User-Id", requesterId)
                    .header("X-User-Role", "USER"))
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("Should support boundary job IDs")
        void shouldSupportBoundaryJobIds() throws Exception {
            // Arrange
            Long requesterId = 10L;
            JobResponseDto response = new JobResponseDto();
            when(adminJobService.getJobById(ADMIN_ROLE, Long.MAX_VALUE))
                    .thenReturn(response);

            // Act & Assert
            mockMvc.perform(get(BASE_URL + "/" + Long.MAX_VALUE)
                    .header("X-User-Id", requesterId)
                    .header("X-User-Role", ADMIN_ROLE))
                    .andExpect(status().isOk());
        }

        @Test
        @DisplayName("Should return 503 when service unavailable")
        void shouldReturnServiceUnavailable() throws Exception {
            // Arrange
            Long requesterId = 10L;
            Long jobId = 22L;
            doThrow(new DownstreamServiceUnavailableException("Job service unreachable"))
                    .when(adminJobService).getJobById(eq(ADMIN_ROLE), eq(jobId));

            // Act & Assert
            mockMvc.perform(get(BASE_URL + "/" + jobId)
                    .header("X-User-Id", requesterId)
                    .header("X-User-Role", ADMIN_ROLE))
                    .andExpect(status().isServiceUnavailable());
        }
    }

    @Nested
    @DisplayName("DeleteJob Endpoint Tests")
    class DeleteJobTests {

        @Test
        @DisplayName("Should return 204 No Content on delete")
        void shouldReturnOk_WithDeleteMessage() throws Exception {
            // Arrange
            Long requesterId = 10L;
            Long jobId = 33L;
            doNothing().when(adminJobService).deleteJob(ADMIN_ROLE, jobId);

            // Act & Assert
            mockMvc.perform(delete(BASE_URL + "/" + jobId)
                    .header("X-User-Id", requesterId)
                    .header("X-User-Role", ADMIN_ROLE))
                    .andExpect(status().isNoContent());
            verify(adminJobService).deleteJob(ADMIN_ROLE, jobId);
        }

        @Test
        @DisplayName("Should return 403 for non-admin role")
        void shouldReturnForbidden_ForNonAdminRole() throws Exception {
            // Arrange
            Long requesterId = 10L;
            Long jobId = 33L;
            doThrow(new UnauthorizedException("Only admins can access this resource"))
                    .when(adminJobService).deleteJob(eq("RECRUITER"), eq(jobId));

            // Act & Assert
            mockMvc.perform(delete(BASE_URL + "/" + jobId)
                    .header("X-User-Id", requesterId)
                    .header("X-User-Role", "RECRUITER"))
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("Should return 503 when service unavailable")
        void shouldReturnServiceUnavailable() throws Exception {
            // Arrange
            Long requesterId = 10L;
            Long jobId = 33L;
            doThrow(new DownstreamServiceUnavailableException("Job service unreachable"))
                    .when(adminJobService).deleteJob(eq(ADMIN_ROLE), eq(jobId));

            // Act & Assert
            mockMvc.perform(delete(BASE_URL + "/" + jobId)
                    .header("X-User-Id", requesterId)
                    .header("X-User-Role", ADMIN_ROLE))
                    .andExpect(status().isServiceUnavailable());
        }

        @Test
        @DisplayName("Should handle IllegalStateException when recruiter ID is missing")
        void shouldHandleIllegalStateException() throws Exception {
            // Arrange
            Long requesterId = 10L;
            Long jobId = 33L;
            doThrow(new IllegalStateException("Recruiter id is missing for job: " + jobId))
                    .when(adminJobService).deleteJob(eq(ADMIN_ROLE), eq(jobId));

            // Act & Assert
            mockMvc.perform(delete(BASE_URL + "/" + jobId)
                    .header("X-User-Id", requesterId)
                    .header("X-User-Role", ADMIN_ROLE))
                    .andExpect(status().isInternalServerError());
        }

        @Test
        @DisplayName("Should support boundary IDs for delete")
        void shouldSupportBoundaryIdsForDelete() throws Exception {
            // Arrange
            Long requesterId = Long.MAX_VALUE;
            Long jobId = Long.MIN_VALUE;
            doNothing().when(adminJobService).deleteJob(ADMIN_ROLE, jobId);

            // Act & Assert
            mockMvc.perform(delete(BASE_URL + "/" + jobId)
                    .header("X-User-Id", requesterId)
                    .header("X-User-Role", ADMIN_ROLE))
                    .andExpect(status().isNoContent());
        }
    }

    @Nested
    @DisplayName("Common Error Handling Tests")
    class CommonErrorHandlingTests {

        @Test
        @DisplayName("Should include error details in response")
        void shouldIncludeErrorDetailsInResponse() throws Exception {
            // Arrange
            Long requesterId = 10L;
            doThrow(new UnauthorizedException("Insufficient permissions"))
                    .when(adminJobService).getAllJobs(eq("USER"), anyInt(), anyInt(), anyString(), anyString());

            // Act & Assert
            mockMvc.perform(get(BASE_URL)
                    .header("X-User-Id", requesterId)
                    .header("X-User-Role", "USER"))
                    .andExpect(status().isForbidden())
                    .andExpect(jsonPath("$.message").exists())
                    .andExpect(jsonPath("$.status").value(403))
                    .andExpect(jsonPath("$.timestamp").exists());
        }
    }
}
