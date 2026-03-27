package com.capg.AdminService.controller;

import com.capg.AdminService.dto.AdminUserUpdateRequest;
import com.capg.AdminService.dto.PagedResponse;
import com.capg.AdminService.dto.UserResponse;
import com.capg.AdminService.service.AdminUserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/admin/users")
@RequiredArgsConstructor
@Tag(name = "Admin Users", description = "Admin operations for users")
@Slf4j
public class AdminUserController {

    private final AdminUserService adminUserService;

    @GetMapping
    @Operation(summary = "Get all users", description = "Fetch paginated users for admin users")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Users fetched successfully"),
            @ApiResponse(responseCode = "403", description = "Only admins can access this resource"),
            @ApiResponse(responseCode = "503", description = "User service unreachable")
    })
    public ResponseEntity<PagedResponse<UserResponse>> getAllUsers(
            @Parameter(hidden = true) @RequestHeader(HttpHeaders.AUTHORIZATION) String authorization,
            @RequestHeader("X-User-Id") Long requesterId,
            @RequestHeader("X-User-Role") String role,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        log.info("Admin users request: action=getAllUsers, requesterId={}, role={}, page={}, size={}",
                requesterId, role, page, size);
        return ResponseEntity.ok(adminUserService.getAllUsers(authorization, role, page, size));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get user by id", description = "Fetch user details by id")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "User fetched successfully"),
            @ApiResponse(responseCode = "403", description = "Only admins can access this resource"),
            @ApiResponse(responseCode = "503", description = "User service unreachable")
    })
    public ResponseEntity<UserResponse> getUserById(
            @Parameter(hidden = true) @RequestHeader(HttpHeaders.AUTHORIZATION) String authorization,
            @RequestHeader("X-User-Id") Long requesterId,
            @RequestHeader("X-User-Role") String role,
            @PathVariable Long id) {
        log.info("Admin users request: action=getUserById, userId={}, requesterId={}, role={}",
                id, requesterId, role);
        return ResponseEntity.ok(adminUserService.getUserById(authorization, role, id));
    }

    @GetMapping("/email/{email}")
    @Operation(summary = "Get user by email", description = "Fetch user details by email")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "User fetched successfully"),
            @ApiResponse(responseCode = "403", description = "Only admins can access this resource"),
            @ApiResponse(responseCode = "503", description = "User service unreachable")
    })
    public ResponseEntity<UserResponse> getUserByEmail(
            @Parameter(hidden = true) @RequestHeader(HttpHeaders.AUTHORIZATION) String authorization,
            @RequestHeader("X-User-Id") Long requesterId,
            @RequestHeader("X-User-Role") String role,
            @PathVariable String email) {
        log.info("Admin users request: action=getUserByEmail, email={}, requesterId={}, role={}",
                email, requesterId, role);
        return ResponseEntity.ok(adminUserService.getUserByEmail(authorization, role, email));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update user", description = "Update user details by id")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "User updated successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid user update request"),
            @ApiResponse(responseCode = "403", description = "Only admins can access this resource"),
            @ApiResponse(responseCode = "503", description = "User service unreachable")
    })
    public ResponseEntity<UserResponse> updateUser(
            @Parameter(hidden = true) @RequestHeader(HttpHeaders.AUTHORIZATION) String authorization,
            @RequestHeader("X-User-Id") Long requesterId,
            @RequestHeader("X-User-Role") String role,
            @PathVariable Long id,
            @Valid @RequestBody AdminUserUpdateRequest request) {
        log.info("Admin users request: action=updateUser, userId={}, requesterId={}, role={}",
                id, requesterId, role);
        return ResponseEntity.ok(adminUserService.updateUser(authorization, role, id, request));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete user by id", description = "Delete user by id")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "User deleted successfully"),
            @ApiResponse(responseCode = "403", description = "Only admins can access this resource"),
            @ApiResponse(responseCode = "503", description = "User service unreachable")
    })
    public ResponseEntity<Map<String, String>> deleteUser(
            @Parameter(hidden = true) @RequestHeader(HttpHeaders.AUTHORIZATION) String authorization,
            @RequestHeader("X-User-Id") Long requesterId,
            @RequestHeader("X-User-Role") String role,
            @PathVariable Long id) {
        log.info("Admin users request: action=deleteUser, userId={}, requesterId={}, role={}",
                id, requesterId, role);
        return ResponseEntity.ok(adminUserService.deleteUser(authorization, role, id));
    }
}
