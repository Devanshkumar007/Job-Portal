package com.capg.authservice.controller;

import com.capg.authservice.dto.request.AdminUserUpdateRequest;
import com.capg.authservice.dto.request.UserUpdateRequest;
import com.capg.authservice.dto.response.UserResponse;
import com.capg.authservice.entity.User;
import com.capg.authservice.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/user")
@CrossOrigin(origins = "*")
@Tag(name = "User Management", description = "Current user and admin user management APIs")
public class UserController {

    private static final Logger log = LoggerFactory.getLogger(UserController.class);

    @Autowired
    private UserService userService;

    @GetMapping("/me")
    @Operation(summary = "Get current authenticated user")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "User returned successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    public ResponseEntity<UserResponse> getCurrentUser(Authentication authentication) {
        log.info("Get current user request received");
        User user = userService.getUserByEmail(authentication.getName());
        log.info("Current user fetched successfully");
        return ResponseEntity.ok(toUserResponse(user));
    }

    @PutMapping("/me")
    @Operation(summary = "Update current authenticated user")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "User updated successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request"),
            @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    public ResponseEntity<UserResponse> updateCurrentUser(
            @Valid @RequestBody UserUpdateRequest request,
            Authentication authentication) {
        log.info("Update current user request received");
        User updatedUser = userService.updateCurrentUser(
                authentication.getName(),
                toUser(request),
                request.getCurrentPassword()
        );
        log.info("Current user updated successfully");
        return ResponseEntity.ok(toUserResponse(updatedUser));
    }

    @DeleteMapping("/me")
    @Operation(summary = "Delete current authenticated user")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "User deleted successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    public ResponseEntity<Map<String, String>> deleteCurrentUser(Authentication authentication) {
        log.info("Delete current user request received");
        User deletedUser = userService.deleteCurrentUser(authentication.getName());
        log.info("Current user deleted successfully");
        return ResponseEntity.status(HttpStatus.OK).body(
                Map.of(
                        "message", "Your account has been deleted successfully.",
                        "deletedUserId", String.valueOf(deletedUser.getId())
                )
        );
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/{id}")
    @Operation(summary = "Get user by id (admin)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "User returned successfully"),
            @ApiResponse(responseCode = "403", description = "Forbidden"),
            @ApiResponse(responseCode = "404", description = "User not found")
    })
    public ResponseEntity<UserResponse> getUserById(@PathVariable Long id) {
        log.info("Admin get user by id request received for id={}", id);
        User user = userService.getUserById(id);
        log.info("Admin fetched user by id successfully for id={}", id);
        return ResponseEntity.ok(toUserResponse(user));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/email/{email}")
    @Operation(summary = "Get user by email (admin)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "User returned successfully"),
            @ApiResponse(responseCode = "403", description = "Forbidden"),
            @ApiResponse(responseCode = "404", description = "User not found")
    })
    public ResponseEntity<UserResponse> getUserByEmail(@PathVariable String email) {
        log.info("Admin get user by email request received");
        User user = userService.getUserByEmail(email);
        log.info("Admin fetched user by email successfully");
        return ResponseEntity.ok(toUserResponse(user));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/getAll")
    @Operation(summary = "Get all users with pagination (admin)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Users returned successfully"),
            @ApiResponse(responseCode = "403", description = "Forbidden")
    })
    public ResponseEntity<Page<UserResponse>> getAllUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        log.info("Admin get all users request received page={} size={}", page, size);
        Page<UserResponse> response = userService.getAllUsers(page, size)
                .map(this::toUserResponse);
        log.info("Admin fetched users page successfully");
        return ResponseEntity.ok(response);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}")
    @Operation(summary = "Update user by id (admin)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "User updated successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request"),
            @ApiResponse(responseCode = "403", description = "Forbidden"),
            @ApiResponse(responseCode = "404", description = "User not found")
    })
    public ResponseEntity<UserResponse> updateUser(
            @PathVariable Long id,
            @Valid @RequestBody AdminUserUpdateRequest request) {
        log.info("Admin update user request received for id={}", id);
        User updatedUser = userService.updateUser(id, toUser(request));
        log.info("Admin updated user successfully for id={}", id);
        return ResponseEntity.ok(toUserResponse(updatedUser));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    @Operation(summary = "Delete user by id (admin)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "User deleted successfully"),
            @ApiResponse(responseCode = "403", description = "Forbidden"),
            @ApiResponse(responseCode = "404", description = "User not found")
    })
    public ResponseEntity<Map<String, String>> deleteUserById(@PathVariable Long id) {
        log.info("Admin delete user request received for id={}", id);
        User deletedUser = userService.deleteUserById(id);
        log.info("Admin deleted user successfully for id={}", id);
        return ResponseEntity.status(HttpStatus.OK).body(
                Map.of(
                        "message", "User deleted successfully.",
                        "deletedUserId", String.valueOf(deletedUser.getId())
                )
        );
    }

    private UserResponse toUserResponse(User user) {
        return new UserResponse(
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getPhone(),
                user.getRole(),
                user.getCreatedAt()
        );
    }

    private User toUser(UserUpdateRequest request) {
        User user = new User();
        user.setName(request.getName());
        user.setEmail(request.getEmail());
        user.setPassword(request.getPassword());
        user.setPhone(request.getPhone());
        return user;
    }

    private User toUser(AdminUserUpdateRequest request) {
        User user = new User();
        user.setName(request.getName());
        user.setEmail(request.getEmail());
        user.setPassword(request.getPassword());
        user.setPhone(request.getPhone());
        user.setRole(request.getRole());
        return user;
    }
}
