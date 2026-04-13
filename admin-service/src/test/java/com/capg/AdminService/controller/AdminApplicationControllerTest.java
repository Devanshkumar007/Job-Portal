package com.capg.AdminService.controller;

import com.capg.AdminService.dto.ApplicationResponse;
import com.capg.AdminService.dto.PagedResponse;
import com.capg.AdminService.exception.DownstreamServiceUnavailableException;
import com.capg.AdminService.exception.UnauthorizedException;
import com.capg.AdminService.service.AdminApplicationService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Map;

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
 * Controller tests for AdminApplicationController covering request handling,
 * HTTP status codes, and error scenarios for all application management endpoints.
 */
@WebMvcTest(AdminApplicationController.class)
class AdminApplicationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AdminApplicationService adminApplicationService;

    private static final String BASE_URL = "/api/admin/applications";
    private static final Long REQUESTER_ID = 10L;
    private static final String ADMIN_ROLE = "ADMIN";

    @Nested
    @DisplayName("GetAllApplications Endpoint Tests")
    class GetAllApplicationsTests {

        @Test
        @DisplayName("Should return 200 OK with paged applications")
        void shouldReturnOk_WithPagedApplications() throws Exception {
            // Arrange
            PagedResponse<ApplicationResponse> response = new PagedResponse<>();
            when(adminApplicationService.getAllApplications(REQUESTER_ID, ADMIN_ROLE, 0, 10, "appliedAt", "desc"))
                    .thenReturn(response);

            // Act & Assert
            mockMvc.perform(get(BASE_URL)
                    .header("X-User-Id", REQUESTER_ID)
                    .header("X-User-Role", ADMIN_ROLE)
                    .param("page", "0")
                    .param("size", "10")
                    .param("sortBy", "appliedAt")
                    .param("direction", "desc"))
                    .andExpect(status().isOk());
            verify(adminApplicationService).getAllApplications(REQUESTER_ID, ADMIN_ROLE, 0, 10, "appliedAt", "desc");
        }

        @Test
        @DisplayName("Should use default pagination parameters")
        void shouldUseDefaultPaginationParameters() throws Exception {
            // Arrange
            PagedResponse<ApplicationResponse> response = new PagedResponse<>();
            when(adminApplicationService.getAllApplications(REQUESTER_ID, ADMIN_ROLE, 0, 10, "appliedAt", "desc"))
                    .thenReturn(response);

            // Act & Assert
            mockMvc.perform(get(BASE_URL)
                    .header("X-User-Id", REQUESTER_ID)
                    .header("X-User-Role", ADMIN_ROLE))
                    .andExpect(status().isOk());
            verify(adminApplicationService).getAllApplications(REQUESTER_ID, ADMIN_ROLE, 0, 10, "appliedAt", "desc");
        }

        @Test
        @DisplayName("Should return 403 Forbidden for non-admin role")
        void shouldReturnForbidden_ForNonAdminRole() throws Exception {
            // Arrange
            doThrow(new UnauthorizedException("Only admins can access this resource"))
                    .when(adminApplicationService).getAllApplications(eq(REQUESTER_ID), eq("USER"), anyInt(), anyInt(), anyString(), anyString());

            // Act & Assert
            mockMvc.perform(get(BASE_URL)
                    .header("X-User-Id", REQUESTER_ID)
                    .header("X-User-Role", "USER"))
                    .andExpect(status().isForbidden())
                    .andExpect(jsonPath("$.status").value(403));
        }

        @Test
        @DisplayName("Should return 503 when service is unavailable")
        void shouldReturnServiceUnavailable_WhenDownstreamFails() throws Exception {
            // Arrange
            doThrow(new DownstreamServiceUnavailableException("Application service unreachable"))
                    .when(adminApplicationService).getAllApplications(eq(REQUESTER_ID), eq(ADMIN_ROLE), anyInt(), anyInt(), anyString(), anyString());

            // Act & Assert
            mockMvc.perform(get(BASE_URL)
                    .header("X-User-Id", REQUESTER_ID)
                    .header("X-User-Role", ADMIN_ROLE))
                    .andExpect(status().isServiceUnavailable())
                    .andExpect(jsonPath("$.status").value(503));
        }

        @Test
        @DisplayName("Should support custom sorting parameters")
        void shouldSupportCustomSortingParameters() throws Exception {
            // Arrange
            PagedResponse<ApplicationResponse> response = new PagedResponse<>();
            when(adminApplicationService.getAllApplications(REQUESTER_ID, ADMIN_ROLE, 0, 20, "jobTitle", "asc"))
                    .thenReturn(response);

            // Act & Assert
            mockMvc.perform(get(BASE_URL)
                    .header("X-User-Id", REQUESTER_ID)
                    .header("X-User-Role", ADMIN_ROLE)
                    .param("page", "0")
                    .param("size", "20")
                    .param("sortBy", "jobTitle")
                    .param("direction", "asc"))
                    .andExpect(status().isOk());
            verify(adminApplicationService).getAllApplications(REQUESTER_ID, ADMIN_ROLE, 0, 20, "jobTitle", "asc");
        }
    }

    @Nested
    @DisplayName("GetApplicationById Endpoint Tests")
    class GetApplicationByIdTests {

        @Test
        @DisplayName("Should return 200 OK with application details")
        void shouldReturnOk_WithApplicationDetails() throws Exception {
            // Arrange
            Long applicationId = 45L;
            ApplicationResponse response = new ApplicationResponse();
            response.setId(applicationId);
            when(adminApplicationService.getApplicationById(applicationId, REQUESTER_ID, ADMIN_ROLE))
                    .thenReturn(response);

            // Act & Assert
            mockMvc.perform(get(BASE_URL + "/" + applicationId)
                    .header("X-User-Id", REQUESTER_ID)
                    .header("X-User-Role", ADMIN_ROLE))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(applicationId));
            verify(adminApplicationService).getApplicationById(applicationId, REQUESTER_ID, ADMIN_ROLE);
        }

        @Test
        @DisplayName("Should return 403 for non-admin role")
        void shouldReturnForbidden_ForNonAdminRole() throws Exception {
            // Arrange
            Long applicationId = 45L;
            doThrow(new UnauthorizedException("Only admins can access this resource"))
                    .when(adminApplicationService).getApplicationById(eq(applicationId), eq(REQUESTER_ID), eq("RECRUITER"));

            // Act & Assert
            mockMvc.perform(get(BASE_URL + "/" + applicationId)
                    .header("X-User-Id", REQUESTER_ID)
                    .header("X-User-Role", "RECRUITER"))
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("Should support boundary application IDs")
        void shouldSupportBoundaryApplicationIds() throws Exception {
            // Arrange
            ApplicationResponse response = new ApplicationResponse();
            when(adminApplicationService.getApplicationById(Long.MAX_VALUE, REQUESTER_ID, ADMIN_ROLE))
                    .thenReturn(response);

            // Act & Assert
            mockMvc.perform(get(BASE_URL + "/" + Long.MAX_VALUE)
                    .header("X-User-Id", REQUESTER_ID)
                    .header("X-User-Role", ADMIN_ROLE))
                    .andExpect(status().isOk());
        }

        @Test
        @DisplayName("Should return 503 when service unavailable")
        void shouldReturnServiceUnavailable() throws Exception {
            // Arrange
            Long applicationId = 45L;
            doThrow(new DownstreamServiceUnavailableException("Application service unreachable"))
                    .when(adminApplicationService).getApplicationById(eq(applicationId), eq(REQUESTER_ID), eq(ADMIN_ROLE));

            // Act & Assert
            mockMvc.perform(get(BASE_URL + "/" + applicationId)
                    .header("X-User-Id", REQUESTER_ID)
                    .header("X-User-Role", ADMIN_ROLE))
                    .andExpect(status().isServiceUnavailable());
        }
    }

    @Nested
    @DisplayName("DeleteApplication Endpoint Tests")
    class DeleteApplicationTests {

        @Test
        @DisplayName("Should return 200 OK with success message on delete")
        void shouldReturnOk_WithDeleteMessage() throws Exception {
            // Arrange
            Long applicationId = 45L;
            doNothing().when(adminApplicationService).deleteApplication(applicationId, REQUESTER_ID, ADMIN_ROLE);

            // Act & Assert
            mockMvc.perform(delete(BASE_URL + "/" + applicationId)
                    .header("X-User-Id", REQUESTER_ID)
                    .header("X-User-Role", ADMIN_ROLE))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.message").value("Application deleted successfully"));
            verify(adminApplicationService).deleteApplication(applicationId, REQUESTER_ID, ADMIN_ROLE);
        }

        @Test
        @DisplayName("Should return 403 for non-admin role")
        void shouldReturnForbidden_ForNonAdminRole() throws Exception {
            // Arrange
            Long applicationId = 45L;
            doThrow(new UnauthorizedException("Only admins can access this resource"))
                    .when(adminApplicationService).deleteApplication(eq(applicationId), eq(REQUESTER_ID), eq("USER"));

            // Act & Assert
            mockMvc.perform(delete(BASE_URL + "/" + applicationId)
                    .header("X-User-Id", REQUESTER_ID)
                    .header("X-User-Role", "USER"))
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("Should return 503 when service unavailable")
        void shouldReturnServiceUnavailable() throws Exception {
            // Arrange
            Long applicationId = 45L;
            doThrow(new DownstreamServiceUnavailableException("Application service unreachable"))
                    .when(adminApplicationService).deleteApplication(eq(applicationId), eq(REQUESTER_ID), eq(ADMIN_ROLE));

            // Act & Assert
            mockMvc.perform(delete(BASE_URL + "/" + applicationId)
                    .header("X-User-Id", REQUESTER_ID)
                    .header("X-User-Role", ADMIN_ROLE))
                    .andExpect(status().isServiceUnavailable());
        }

        @Test
        @DisplayName("Should support boundary IDs for delete")
        void shouldSupportBoundaryIdsForDelete() throws Exception {
            // Arrange
            doNothing().when(adminApplicationService).deleteApplication(Long.MIN_VALUE, Long.MAX_VALUE, ADMIN_ROLE);

            // Act & Assert
            mockMvc.perform(delete(BASE_URL + "/" + Long.MIN_VALUE)
                    .header("X-User-Id", Long.MAX_VALUE)
                    .header("X-User-Role", ADMIN_ROLE))
                    .andExpect(status().isOk());
        }
    }

    @Nested
    @DisplayName("Common Error Handling Tests")
    class CommonErrorHandlingTests {

        @Test
        @DisplayName("Should include error details in response")
        void shouldIncludeErrorDetailsInResponse() throws Exception {
            // Arrange
            doThrow(new UnauthorizedException("Unauthorized access"))
                    .when(adminApplicationService).getAllApplications(eq(REQUESTER_ID), eq("RECRUITER"), anyInt(), anyInt(), anyString(), anyString());

            // Act & Assert
            mockMvc.perform(get(BASE_URL)
                    .header("X-User-Id", REQUESTER_ID)
                    .header("X-User-Role", "RECRUITER"))
                    .andExpect(status().isForbidden())
                    .andExpect(jsonPath("$.message").exists())
                    .andExpect(jsonPath("$.status").value(403))
                    .andExpect(jsonPath("$.timestamp").exists());
        }
    }
}
