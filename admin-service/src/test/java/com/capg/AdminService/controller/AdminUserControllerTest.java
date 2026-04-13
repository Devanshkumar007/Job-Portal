package com.capg.AdminService.controller;

import com.capg.AdminService.dto.AdminUserUpdateRequest;
import com.capg.AdminService.dto.PagedResponse;
import com.capg.AdminService.dto.UserResponse;
import com.capg.AdminService.exception.DownstreamServiceUnavailableException;
import com.capg.AdminService.exception.UnauthorizedException;
import com.capg.AdminService.service.AdminUserService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Controller tests for AdminUserController covering request validation, response status codes,
 * and error handling for all user management endpoints.
 */
@WebMvcTest(AdminUserController.class)
class AdminUserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AdminUserService adminUserService;

    private static final String BASE_URL = "/api/admin/users";
    private static final String AUTH_HEADER = "Bearer token";
    private static final Long REQUESTER_ID = 10L;
    private static final String ADMIN_ROLE = "ADMIN";

    @Nested
    @DisplayName("GetAllUsers Endpoint Tests")
    class GetAllUsersTests {

        @Test
        @DisplayName("Should return 200 OK with paged users")
        void shouldReturnOk_WithPagedUsers() throws Exception {
            // Arrange
            PagedResponse<UserResponse> response = new PagedResponse<>();
            when(adminUserService.getAllUsers(AUTH_HEADER, ADMIN_ROLE, 0, 10))
                    .thenReturn(response);

            // Act & Assert
            mockMvc.perform(get(BASE_URL)
                    .header("Authorization", AUTH_HEADER)
                    .header("X-User-Id", REQUESTER_ID)
                    .header("X-User-Role", ADMIN_ROLE)
                    .param("page", "0")
                    .param("size", "10"))
                    .andExpect(status().isOk());
            verify(adminUserService).getAllUsers(AUTH_HEADER, ADMIN_ROLE, 0, 10);
        }

        @Test
        @DisplayName("Should return 403 Forbidden for unauthorized role")
        void shouldReturnForbidden_ForUnauthorizedRole() throws Exception {
            // Arrange
            doThrow(new UnauthorizedException("Only admins can access this resource"))
                    .when(adminUserService).getAllUsers(anyString(), eq("USER"), anyInt(), anyInt());

            // Act & Assert
            mockMvc.perform(get(BASE_URL)
                    .header("Authorization", AUTH_HEADER)
                    .header("X-User-Id", REQUESTER_ID)
                    .header("X-User-Role", "USER")
                    .param("page", "0")
                    .param("size", "10"))
                    .andExpect(status().isForbidden())
                    .andExpect(jsonPath("$.status").value(403));
        }

        @Test
        @DisplayName("Should return 503 when service is unavailable")
        void shouldReturnServiceUnavailable_WhenDownstreamFails() throws Exception {
            // Arrange
            doThrow(new DownstreamServiceUnavailableException("User service unreachable. Please try again after some time."))
                    .when(adminUserService).getAllUsers(anyString(), eq(ADMIN_ROLE), anyInt(), anyInt());

            // Act & Assert
            mockMvc.perform(get(BASE_URL)
                    .header("Authorization", AUTH_HEADER)
                    .header("X-User-Id", REQUESTER_ID)
                    .header("X-User-Role", ADMIN_ROLE)
                    .param("page", "0")
                    .param("size", "10"))
                    .andExpect(status().isServiceUnavailable())
                    .andExpect(jsonPath("$.status").value(503));
        }

        @Test
        @DisplayName("Should support custom pagination parameters")
        void shouldSupportCustomPaginationParameters() throws Exception {
            // Arrange
            PagedResponse<UserResponse> response = new PagedResponse<>();
            when(adminUserService.getAllUsers(AUTH_HEADER, ADMIN_ROLE, 5, 50))
                    .thenReturn(response);

            // Act & Assert
            mockMvc.perform(get(BASE_URL)
                    .header("Authorization", AUTH_HEADER)
                    .header("X-User-Id", REQUESTER_ID)
                    .header("X-User-Role", ADMIN_ROLE)
                    .param("page", "5")
                    .param("size", "50"))
                    .andExpect(status().isOk());
            verify(adminUserService).getAllUsers(AUTH_HEADER, ADMIN_ROLE, 5, 50);
        }
    }

    @Nested
    @DisplayName("GetUsersByRole Endpoint Tests")
    class GetUsersByRoleTests {

        @Test
        @DisplayName("Should return 200 OK with users filtered by role")
        void shouldReturnOk_WithUsersByRole() throws Exception {
            // Arrange
            PagedResponse<UserResponse> response = new PagedResponse<>();
            when(adminUserService.getUsersByRole(AUTH_HEADER, ADMIN_ROLE, "RECRUITER", 0, 10))
                    .thenReturn(response);

            // Act & Assert
            mockMvc.perform(get(BASE_URL + "/role/RECRUITER")
                    .header("Authorization", AUTH_HEADER)
                    .header("X-User-Id", REQUESTER_ID)
                    .header("X-User-Role", ADMIN_ROLE)
                    .param("page", "0")
                    .param("size", "10"))
                    .andExpect(status().isOk());
            verify(adminUserService).getUsersByRole(AUTH_HEADER, ADMIN_ROLE, "RECRUITER", 0, 10);
        }

        @Test
        @DisplayName("Should return 403 when requester is not admin")
        void shouldReturnForbidden_WhenRequesterNotAdmin() throws Exception {
            // Arrange
            doThrow(new UnauthorizedException("Only admins can access this resource"))
                    .when(adminUserService).getUsersByRole(anyString(), eq("RECRUITER"), anyString(), anyInt(), anyInt());

            // Act & Assert
            mockMvc.perform(get(BASE_URL + "/role/USER")
                    .header("Authorization", AUTH_HEADER)
                    .header("X-User-Id", REQUESTER_ID)
                    .header("X-User-Role", "RECRUITER"))
                    .andExpect(status().isForbidden());
        }
    }

    @Nested
    @DisplayName("GetUserById Endpoint Tests")
    class GetUserByIdTests {

        @Test
        @DisplayName("Should return 200 OK with user details")
        void shouldReturnOk_WithUserDetails() throws Exception {
            // Arrange
            Long userId = 5L;
            UserResponse response = new UserResponse();
            response.setId(userId);
            when(adminUserService.getUserById(AUTH_HEADER, ADMIN_ROLE, userId))
                    .thenReturn(response);

            // Act & Assert
            mockMvc.perform(get(BASE_URL + "/" + userId)
                    .header("Authorization", AUTH_HEADER)
                    .header("X-User-Id", REQUESTER_ID)
                    .header("X-User-Role", ADMIN_ROLE))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(userId));
            verify(adminUserService).getUserById(AUTH_HEADER, ADMIN_ROLE, userId);
        }

        @Test
        @DisplayName("Should return 403 for non-admin role")
        void shouldReturnForbidden_ForNonAdminRole() throws Exception {
            // Arrange
            Long userId = 5L;
            doThrow(new UnauthorizedException("Only admins can access this resource"))
                    .when(adminUserService).getUserById(anyString(), eq("USER"), eq(userId));

            // Act & Assert
            mockMvc.perform(get(BASE_URL + "/" + userId)
                    .header("Authorization", AUTH_HEADER)
                    .header("X-User-Id", REQUESTER_ID)
                    .header("X-User-Role", "USER"))
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("Should support boundary user IDs")
        void shouldSupportBoundaryUserIds() throws Exception {
            // Arrange
            UserResponse response = new UserResponse();
            when(adminUserService.getUserById(AUTH_HEADER, ADMIN_ROLE, Long.MAX_VALUE))
                    .thenReturn(response);

            // Act & Assert
            mockMvc.perform(get(BASE_URL + "/" + Long.MAX_VALUE)
                    .header("Authorization", AUTH_HEADER)
                    .header("X-User-Id", REQUESTER_ID)
                    .header("X-User-Role", ADMIN_ROLE))
                    .andExpect(status().isOk());
        }
    }

    @Nested
    @DisplayName("GetUserByEmail Endpoint Tests")
    class GetUserByEmailTests {

        @Test
        @DisplayName("Should return 200 OK with user by email")
        void shouldReturnOk_WithUserByEmail() throws Exception {
            // Arrange
            String email = "test@example.com";
            UserResponse response = new UserResponse();
            response.setEmail(email);
            when(adminUserService.getUserByEmail(AUTH_HEADER, ADMIN_ROLE, email))
                    .thenReturn(response);

            // Act & Assert
            mockMvc.perform(get(BASE_URL + "/email/" + email)
                    .header("Authorization", AUTH_HEADER)
                    .header("X-User-Id", REQUESTER_ID)
                    .header("X-User-Role", ADMIN_ROLE))
                    .andExpect(status().isOk());
            verify(adminUserService).getUserByEmail(AUTH_HEADER, ADMIN_ROLE, email);
        }

        @Test
        @DisplayName("Should return 403 for unauthorized role")
        void shouldReturnForbidden_ForUnauthorizedRole() throws Exception {
            // Arrange
            String email = "test@example.com";
            doThrow(new UnauthorizedException("Only admins can access this resource"))
                    .when(adminUserService).getUserByEmail(anyString(), eq("RECRUITER"), anyString());

            // Act & Assert
            mockMvc.perform(get(BASE_URL + "/email/" + email)
                    .header("Authorization", AUTH_HEADER)
                    .header("X-User-Id", REQUESTER_ID)
                    .header("X-User-Role", "RECRUITER"))
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("Should handle email with special characters")
        void shouldHandleEmailWithSpecialCharacters() throws Exception {
            // Arrange
            String email = "test+alias.name@sub.example.co.uk";
            UserResponse response = new UserResponse();
            when(adminUserService.getUserByEmail(AUTH_HEADER, ADMIN_ROLE, email))
                    .thenReturn(response);

            // Act & Assert
            mockMvc.perform(get(BASE_URL + "/email/" + email)
                    .header("Authorization", AUTH_HEADER)
                    .header("X-User-Id", REQUESTER_ID)
                    .header("X-User-Role", ADMIN_ROLE))
                    .andExpect(status().isOk());
        }
    }

    @Nested
    @DisplayName("UpdateUser Endpoint Tests")
    class UpdateUserTests {

        @Test
        @DisplayName("Should return 200 OK with updated user")
        void shouldReturnOk_WithUpdatedUser() throws Exception {
            // Arrange
            Long userId = 8L;
            UserResponse response = new UserResponse();
            response.setId(userId);
            when(adminUserService.updateUser(anyString(), eq(ADMIN_ROLE), eq(userId), any(AdminUserUpdateRequest.class)))
                    .thenReturn(response);

            // Act & Assert
            mockMvc.perform(put(BASE_URL + "/" + userId)
                    .header("Authorization", AUTH_HEADER)
                    .header("X-User-Id", REQUESTER_ID)
                    .header("X-User-Role", ADMIN_ROLE)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("{\"name\": \"Updated Name\"}"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(userId));
        }

        @Test
        @DisplayName("Should return 403 for non-admin role")
        void shouldReturnForbidden_ForNonAdminRole() throws Exception {
            // Arrange
            Long userId = 8L;
            doThrow(new UnauthorizedException("Only admins can access this resource"))
                    .when(adminUserService).updateUser(anyString(), eq("USER"), eq(userId), any());

            // Act & Assert
            mockMvc.perform(put(BASE_URL + "/" + userId)
                    .header("Authorization", AUTH_HEADER)
                    .header("X-User-Id", REQUESTER_ID)
                    .header("X-User-Role", "USER")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("{}"))
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("Should return 200 OK with empty request body")
        void shouldReturnOk_WithEmptyRequestBody() throws Exception {
            // Arrange
            Long userId = 8L;
            UserResponse response = new UserResponse();
            when(adminUserService.updateUser(anyString(), eq(ADMIN_ROLE), eq(userId), any()))
                    .thenReturn(response);

            // Act & Assert
            mockMvc.perform(put(BASE_URL + "/" + userId)
                    .header("Authorization", AUTH_HEADER)
                    .header("X-User-Id", REQUESTER_ID)
                    .header("X-User-Role", ADMIN_ROLE)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("{}"))
                    .andExpect(status().isOk());
        }
    }

    @Nested
    @DisplayName("DeleteUser Endpoint Tests")
    class DeleteUserTests {

        @Test
        @DisplayName("Should return 200 OK with success message")
        void shouldReturnOk_WithDeleteMessage() throws Exception {
            // Arrange
            Long userId = 11L;
            Map<String, String> response = Map.of("message", "User deleted successfully");
            when(adminUserService.deleteUser(AUTH_HEADER, ADMIN_ROLE, userId))
                    .thenReturn(response);

            // Act & Assert
            mockMvc.perform(delete(BASE_URL + "/" + userId)
                    .header("Authorization", AUTH_HEADER)
                    .header("X-User-Id", REQUESTER_ID)
                    .header("X-User-Role", ADMIN_ROLE))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.message").value("User deleted successfully"));
            verify(adminUserService).deleteUser(AUTH_HEADER, ADMIN_ROLE, userId);
        }

        @Test
        @DisplayName("Should return 403 for non-admin role")
        void shouldReturnForbidden_ForNonAdminRole() throws Exception {
            // Arrange
            Long userId = 11L;
            doThrow(new UnauthorizedException("Only admins can access this resource"))
                    .when(adminUserService).deleteUser(anyString(), eq("RECRUITER"), eq(userId));

            // Act & Assert
            mockMvc.perform(delete(BASE_URL + "/" + userId)
                    .header("Authorization", AUTH_HEADER)
                    .header("X-User-Id", REQUESTER_ID)
                    .header("X-User-Role", "RECRUITER"))
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("Should return 503 when service unavailable")
        void shouldReturnServiceUnavailable() throws Exception {
            // Arrange
            Long userId = 11L;
            doThrow(new DownstreamServiceUnavailableException("User service unreachable"))
                    .when(adminUserService).deleteUser(anyString(), eq(ADMIN_ROLE), eq(userId));

            // Act & Assert
            mockMvc.perform(delete(BASE_URL + "/" + userId)
                    .header("Authorization", AUTH_HEADER)
                    .header("X-User-Id", REQUESTER_ID)
                    .header("X-User-Role", ADMIN_ROLE))
                    .andExpect(status().isServiceUnavailable());
        }
    }

    @Nested
    @DisplayName("Common Error Handling Tests")
    class CommonErrorHandlingTests {

        @Test
        @DisplayName("Should include error details in response")
        void shouldIncludeErrorDetailsInResponse() throws Exception {
            // Arrange
            doThrow(new UnauthorizedException("Custom unauthorized message"))
                    .when(adminUserService).getAllUsers(anyString(), eq("RECRUITER"), anyInt(), anyInt());

            // Act & Assert
            mockMvc.perform(get(BASE_URL)
                    .header("Authorization", AUTH_HEADER)
                    .header("X-User-Id", REQUESTER_ID)
                    .header("X-User-Role", "RECRUITER"))
                    .andExpect(status().isForbidden())
                    .andExpect(jsonPath("$.message").exists())
                    .andExpect(jsonPath("$.status").value(403))
                    .andExpect(jsonPath("$.timestamp").exists());
        }
    }
}
