package com.capg.AdminService.service;

import com.capg.AdminService.client.UserClient;
import com.capg.AdminService.dto.AdminUserUpdateRequest;
import com.capg.AdminService.dto.PagedResponse;
import com.capg.AdminService.dto.UserResponse;
import com.capg.AdminService.exception.DownstreamServiceUnavailableException;
import com.capg.AdminService.exception.UnauthorizedException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AdminUserServiceImplTest {

    @Mock
    private UserClient userClient;

    @InjectMocks
    private AdminUserServiceImpl adminUserService;

    @Test
    @DisplayName("getAllUsers should return page data when requester role is ADMIN")
    void getAllUsers_ShouldReturnPagedUsers_WhenRoleIsAdmin() {
        String authHeader = "Bearer token";
        PagedResponse<UserResponse> expected = new PagedResponse<>();

        when(userClient.getAllUsers(authHeader, 0, 20)).thenReturn(expected);

        PagedResponse<UserResponse> actual = adminUserService.getAllUsers(authHeader, "ADMIN", 0, 20);

        assertEquals(expected, actual);
        verify(userClient).getAllUsers(authHeader, 0, 20);
    }

    @Test
    @DisplayName("getUserById should reject non-admin role and skip downstream call")
    void getUserById_ShouldThrowUnauthorized_WhenRoleIsNotAdmin() {
        String authHeader = "Bearer token";
        Long userId = 5L;

        assertThrows(UnauthorizedException.class,
                () -> adminUserService.getUserById(authHeader, "USER", userId));
        verify(userClient, never()).getUserById(authHeader, userId);
    }

    @Test
    @DisplayName("getUserById should return user when role is ADMIN")
    void getUserById_ShouldReturnUser_WhenRoleIsAdmin() {
        String authHeader = "Bearer token";
        Long userId = 5L;
        UserResponse expected = new UserResponse();
        expected.setId(userId);

        when(userClient.getUserById(authHeader, userId)).thenReturn(expected);

        UserResponse actual = adminUserService.getUserById(authHeader, "ADMIN", userId);

        assertEquals(expected, actual);
        verify(userClient).getUserById(authHeader, userId);
    }

    @Test
    @DisplayName("getUsersByRole should return paged users when requester role is ADMIN")
    void getUsersByRole_ShouldReturnPagedUsers_WhenRequesterRoleIsAdmin() {
        String authHeader = "Bearer token";
        String targetRole = "RECRUITER";
        PagedResponse<UserResponse> expected = new PagedResponse<>();

        when(userClient.getUsersByRole(authHeader, targetRole, 0, 10)).thenReturn(expected);

        PagedResponse<UserResponse> actual =
                adminUserService.getUsersByRole(authHeader, "ADMIN", targetRole, 0, 10);

        assertEquals(expected, actual);
        verify(userClient).getUsersByRole(authHeader, targetRole, 0, 10);
    }

    @Test
    @DisplayName("getUsersByRole should reject non-admin requester role and skip downstream call")
    void getUsersByRole_ShouldThrowUnauthorized_WhenRequesterRoleIsNotAdmin() {
        String authHeader = "Bearer token";
        String targetRole = "RECRUITER";

        assertThrows(UnauthorizedException.class,
                () -> adminUserService.getUsersByRole(authHeader, "USER", targetRole, 0, 10));
        verify(userClient, never()).getUsersByRole(authHeader, targetRole, 0, 10);
    }

    @Test
    @DisplayName("getUserByEmail should pass-through email lookup for case-insensitive admin role")
    void getUserByEmail_ShouldReturnUser_WhenRoleIsLowercaseAdmin() {
        String authHeader = "Bearer token";
        String email = "demo@example.com";
        UserResponse expected = new UserResponse();
        expected.setEmail(email);

        when(userClient.getUserByEmail(authHeader, email)).thenReturn(expected);

        UserResponse actual = adminUserService.getUserByEmail(authHeader, "admin", email);

        assertEquals(expected, actual);
        verify(userClient).getUserByEmail(authHeader, email);
    }

    @Test
    @DisplayName("updateUser should delegate update request object as-is for admin users")
    void updateUser_ShouldForwardRequest_WhenRoleIsAdmin() {
        String authHeader = "Bearer token";
        Long userId = 8L;
        AdminUserUpdateRequest request = new AdminUserUpdateRequest();
        request.setName("Updated Name");
        UserResponse expected = new UserResponse();
        expected.setId(userId);

        when(userClient.updateUser(authHeader, userId, request)).thenReturn(expected);

        UserResponse actual = adminUserService.updateUser(authHeader, "ADMIN", userId, request);

        assertEquals(expected, actual);
        verify(userClient).updateUser(authHeader, userId, request);
    }

    @Test
    @DisplayName("updateUser should still pass null request body to downstream client when role is ADMIN")
    void updateUser_ShouldForwardNullRequest_WhenRoleIsAdmin() {
        String authHeader = "Bearer token";
        Long userId = 8L;
        UserResponse expected = new UserResponse();
        expected.setId(userId);

        when(userClient.updateUser(authHeader, userId, null)).thenReturn(expected);

        UserResponse actual = adminUserService.updateUser(authHeader, "ADMIN", userId, null);

        assertEquals(expected, actual);
        verify(userClient).updateUser(authHeader, userId, null);
    }

    @Test
    @DisplayName("deleteUser should return downstream status message map for admin users")
    void deleteUser_ShouldReturnStatusMap_WhenRoleIsAdmin() {
        String authHeader = "Bearer token";
        Long userId = 11L;
        Map<String, String> expected = Map.of("message", "User deleted successfully");

        when(userClient.deleteUserById(authHeader, userId)).thenReturn(expected);

        Map<String, String> actual = adminUserService.deleteUser(authHeader, "ADMIN", userId);

        assertEquals(expected, actual);
        verify(userClient).deleteUserById(authHeader, userId);
    }

    @Test
    @DisplayName("deleteUser should throw UnauthorizedException when role is null")
    void deleteUser_ShouldThrowUnauthorized_WhenRoleIsNull() {
        String authHeader = "Bearer token";
        Long userId = 11L;

        assertThrows(UnauthorizedException.class, () -> adminUserService.deleteUser(authHeader, null, userId));
        verify(userClient, never()).deleteUserById(authHeader, userId);
    }

    @Test
    @DisplayName("getAllUsers fallback should rethrow UnauthorizedException")
    void getAllUsersFallback_ShouldRethrowUnauthorizedException() {
        UnauthorizedException unauthorized = new UnauthorizedException("forbidden");

        RuntimeException thrown = invokeFallbackAndCapture(
                "getAllUsersFallback",
                new Class<?>[]{String.class, String.class, int.class, int.class, Throwable.class},
                "Bearer token", "ADMIN", 0, 20, unauthorized
        );

        assertSame(unauthorized, thrown);
    }

    @Test
    @DisplayName("getUsersByRole fallback should wrap downstream errors")
    void getUsersByRoleFallback_ShouldWrapDownstreamError() {
        RuntimeException downstream = new RuntimeException("service down");

        RuntimeException thrown = invokeFallbackAndCapture(
                "getUsersByRoleFallback",
                new Class<?>[]{String.class, String.class, String.class, int.class, int.class, Throwable.class},
                "Bearer token", "ADMIN", "RECRUITER", 0, 10, downstream
        );

        DownstreamServiceUnavailableException wrapped =
                assertInstanceOf(DownstreamServiceUnavailableException.class, thrown);
        assertEquals("User service unreachable. Please try again after some time.", wrapped.getMessage());
        assertSame(downstream, wrapped.getCause());
    }

    @Test
    @DisplayName("getUserById fallback should wrap downstream errors")
    void getUserByIdFallback_ShouldWrapDownstreamError() {
        RuntimeException downstream = new RuntimeException("timeout");

        RuntimeException thrown = invokeFallbackAndCapture(
                "getUserByIdFallback",
                new Class<?>[]{String.class, String.class, Long.class, Throwable.class},
                "Bearer token", "ADMIN", 5L, downstream
        );

        DownstreamServiceUnavailableException wrapped =
                assertInstanceOf(DownstreamServiceUnavailableException.class, thrown);
        assertEquals("User service unreachable. Please try again after some time.", wrapped.getMessage());
        assertSame(downstream, wrapped.getCause());
    }

    @Test
    @DisplayName("getUserByEmail fallback should rethrow UnauthorizedException")
    void getUserByEmailFallback_ShouldRethrowUnauthorizedException() {
        UnauthorizedException unauthorized = new UnauthorizedException("forbidden");

        RuntimeException thrown = invokeFallbackAndCapture(
                "getUserByEmailFallback",
                new Class<?>[]{String.class, String.class, String.class, Throwable.class},
                "Bearer token", "ADMIN", "demo@example.com", unauthorized
        );

        assertSame(unauthorized, thrown);
    }

    @Test
    @DisplayName("updateUser fallback should wrap downstream errors")
    void updateUserFallback_ShouldWrapDownstreamError() {
        RuntimeException downstream = new RuntimeException("service down");
        AdminUserUpdateRequest request = new AdminUserUpdateRequest();
        request.setName("Updated Name");

        RuntimeException thrown = invokeFallbackAndCapture(
                "updateUserFallback",
                new Class<?>[]{String.class, String.class, Long.class, AdminUserUpdateRequest.class, Throwable.class},
                "Bearer token", "ADMIN", 8L, request, downstream
        );

        DownstreamServiceUnavailableException wrapped =
                assertInstanceOf(DownstreamServiceUnavailableException.class, thrown);
        assertEquals("User service unreachable. Please try again after some time.", wrapped.getMessage());
        assertSame(downstream, wrapped.getCause());
    }

    @Test
    @DisplayName("deleteUser fallback should wrap downstream errors")
    void deleteUserFallback_ShouldWrapDownstreamError() {
        RuntimeException downstream = new RuntimeException("timeout");

        RuntimeException thrown = invokeFallbackAndCapture(
                "deleteUserFallback",
                new Class<?>[]{String.class, String.class, Long.class, Throwable.class},
                "Bearer token", "ADMIN", 11L, downstream
        );

        DownstreamServiceUnavailableException wrapped =
                assertInstanceOf(DownstreamServiceUnavailableException.class, thrown);
        assertEquals("User service unreachable. Please try again after some time.", wrapped.getMessage());
        assertSame(downstream, wrapped.getCause());
    }

    @Test
    @DisplayName("getAllUsers fallback should wrap downstream errors")
    void getAllUsersFallback_ShouldWrapDownstreamError() {
        RuntimeException downstream = new RuntimeException("service down");

        RuntimeException thrown = invokeFallbackAndCapture(
                "getAllUsersFallback",
                new Class<?>[]{String.class, String.class, int.class, int.class, Throwable.class},
                "Bearer token", "ADMIN", 0, 20, downstream
        );

        DownstreamServiceUnavailableException wrapped =
                assertInstanceOf(DownstreamServiceUnavailableException.class, thrown);
        assertEquals("User service unreachable. Please try again after some time.", wrapped.getMessage());
        assertSame(downstream, wrapped.getCause());
    }

    @Test
    @DisplayName("getUsersByRole fallback should rethrow UnauthorizedException")
    void getUsersByRoleFallback_ShouldRethrowUnauthorizedException() {
        UnauthorizedException unauthorized = new UnauthorizedException("forbidden");

        RuntimeException thrown = invokeFallbackAndCapture(
                "getUsersByRoleFallback",
                new Class<?>[]{String.class, String.class, String.class, int.class, int.class, Throwable.class},
                "Bearer token", "ADMIN", "RECRUITER", 0, 10, unauthorized
        );

        assertSame(unauthorized, thrown);
    }

    @Test
    @DisplayName("getUserById fallback should rethrow UnauthorizedException")
    void getUserByIdFallback_ShouldRethrowUnauthorizedException() {
        UnauthorizedException unauthorized = new UnauthorizedException("forbidden");

        RuntimeException thrown = invokeFallbackAndCapture(
                "getUserByIdFallback",
                new Class<?>[]{String.class, String.class, Long.class, Throwable.class},
                "Bearer token", "ADMIN", 5L, unauthorized
        );

        assertSame(unauthorized, thrown);
    }

    @Test
    @DisplayName("getUserByEmail fallback should wrap downstream errors")
    void getUserByEmailFallback_ShouldWrapDownstreamError() {
        RuntimeException downstream = new RuntimeException("service down");

        RuntimeException thrown = invokeFallbackAndCapture(
                "getUserByEmailFallback",
                new Class<?>[]{String.class, String.class, String.class, Throwable.class},
                "Bearer token", "ADMIN", "demo@example.com", downstream
        );

        DownstreamServiceUnavailableException wrapped =
                assertInstanceOf(DownstreamServiceUnavailableException.class, thrown);
        assertEquals("User service unreachable. Please try again after some time.", wrapped.getMessage());
        assertSame(downstream, wrapped.getCause());
    }

    @Test
    @DisplayName("updateUser fallback should rethrow UnauthorizedException")
    void updateUserFallback_ShouldRethrowUnauthorizedException() {
        UnauthorizedException unauthorized = new UnauthorizedException("forbidden");
        AdminUserUpdateRequest request = new AdminUserUpdateRequest();
        request.setName("Updated Name");

        RuntimeException thrown = invokeFallbackAndCapture(
                "updateUserFallback",
                new Class<?>[]{String.class, String.class, Long.class, AdminUserUpdateRequest.class, Throwable.class},
                "Bearer token", "ADMIN", 8L, request, unauthorized
        );

        assertSame(unauthorized, thrown);
    }

    @Test
    @DisplayName("deleteUser fallback should rethrow UnauthorizedException")
    void deleteUserFallback_ShouldRethrowUnauthorizedException() {
        UnauthorizedException unauthorized = new UnauthorizedException("forbidden");

        RuntimeException thrown = invokeFallbackAndCapture(
                "deleteUserFallback",
                new Class<?>[]{String.class, String.class, Long.class, Throwable.class},
                "Bearer token", "ADMIN", 11L, unauthorized
        );

        assertSame(unauthorized, thrown);
    }

    private RuntimeException invokeFallbackAndCapture(String methodName, Class<?>[] parameterTypes, Object... args) {
        try {
            Method method = AdminUserServiceImpl.class.getDeclaredMethod(methodName, parameterTypes);
            method.setAccessible(true);
            method.invoke(adminUserService, args);
            throw new AssertionError("Expected runtime exception from fallback method");
        } catch (InvocationTargetException ex) {
            Throwable cause = ex.getCause();
            if (cause instanceof RuntimeException runtimeException) {
                return runtimeException;
            }
            throw new RuntimeException(cause);
        } catch (ReflectiveOperationException ex) {
            throw new RuntimeException(ex);
        }
    }
}
