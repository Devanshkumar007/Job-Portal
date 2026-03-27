package com.capg.AdminService.service;

import com.capg.AdminService.client.UserClient;
import com.capg.AdminService.dto.AdminUserUpdateRequest;
import com.capg.AdminService.dto.PagedResponse;
import com.capg.AdminService.dto.UserResponse;
import com.capg.AdminService.exception.DownstreamServiceUnavailableException;
import com.capg.AdminService.exception.UnauthorizedException;
import com.capg.AdminService.util.AdminRoleValidator;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class AdminUserServiceImpl implements AdminUserService {

    private final UserClient userClient;

    @Override
    @CircuitBreaker(name = "authService", fallbackMethod = "getAllUsersFallback")
    public PagedResponse<UserResponse> getAllUsers(String authorization, String role, int page, int size) {
        AdminRoleValidator.requireAdmin(role);
        return userClient.getAllUsers(authorization, page, size);
    }

    @Override
    @CircuitBreaker(name = "authService", fallbackMethod = "getUserByIdFallback")
    public UserResponse getUserById(String authorization, String role, Long id) {
        AdminRoleValidator.requireAdmin(role);
        return userClient.getUserById(authorization, id);
    }

    @Override
    @CircuitBreaker(name = "authService", fallbackMethod = "getUserByEmailFallback")
    public UserResponse getUserByEmail(String authorization, String role, String email) {
        AdminRoleValidator.requireAdmin(role);
        return userClient.getUserByEmail(authorization, email);
    }

    @Override
    @CircuitBreaker(name = "authService", fallbackMethod = "updateUserFallback")
    public UserResponse updateUser(String authorization, String role, Long id, AdminUserUpdateRequest request) {
        AdminRoleValidator.requireAdmin(role);
        return userClient.updateUser(authorization, id, request);
    }

    @Override
    @CircuitBreaker(name = "authService", fallbackMethod = "deleteUserFallback")
    public Map<String, String> deleteUser(String authorization, String role, Long id) {
        AdminRoleValidator.requireAdmin(role);
        return userClient.deleteUserById(authorization, id);
    }

    private PagedResponse<UserResponse> getAllUsersFallback(
            String authorization, String role, int page, int size, Throwable ex) {
        if (ex instanceof UnauthorizedException unauthorizedException) {
            throw unauthorizedException;
        }
        log.error("Fallback triggered. service=User service, operation=getAllUsers, cause={}", ex.getMessage(), ex);
        throw new DownstreamServiceUnavailableException(
                "User service unreachable. Please try again after some time.", ex);
    }

    private UserResponse getUserByIdFallback(String authorization, String role, Long id, Throwable ex) {
        if (ex instanceof UnauthorizedException unauthorizedException) {
            throw unauthorizedException;
        }
        log.error("Fallback triggered. service=User service, operation=getUserById, cause={}", ex.getMessage(), ex);
        throw new DownstreamServiceUnavailableException(
                "User service unreachable. Please try again after some time.", ex);
    }

    private UserResponse getUserByEmailFallback(String authorization, String role, String email, Throwable ex) {
        if (ex instanceof UnauthorizedException unauthorizedException) {
            throw unauthorizedException;
        }
        log.error("Fallback triggered. service=User service, operation=getUserByEmail, cause={}",
                ex.getMessage(), ex);
        throw new DownstreamServiceUnavailableException(
                "User service unreachable. Please try again after some time.", ex);
    }

    private UserResponse updateUserFallback(
            String authorization, String role, Long id, AdminUserUpdateRequest request, Throwable ex) {
        if (ex instanceof UnauthorizedException unauthorizedException) {
            throw unauthorizedException;
        }
        log.error("Fallback triggered. service=User service, operation=updateUser, cause={}", ex.getMessage(), ex);
        throw new DownstreamServiceUnavailableException(
                "User service unreachable. Please try again after some time.", ex);
    }

    private Map<String, String> deleteUserFallback(String authorization, String role, Long id, Throwable ex) {
        if (ex instanceof UnauthorizedException unauthorizedException) {
            throw unauthorizedException;
        }
        log.error("Fallback triggered. service=User service, operation=deleteUserById, cause={}",
                ex.getMessage(), ex);
        throw new DownstreamServiceUnavailableException(
                "User service unreachable. Please try again after some time.", ex);
    }
}
