package com.capg.AdminService.client;

import com.capg.AdminService.dto.AdminUserUpdateRequest;
import com.capg.AdminService.dto.PagedResponse;
import com.capg.AdminService.dto.UserResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Map;

@FeignClient(name="AUTH-SERVICE")
public interface UserClient {

    @GetMapping("/api/user/getAll")
    PagedResponse<UserResponse> getAllUsers(
            @RequestHeader("Authorization") String authorization,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size);

    @GetMapping("/api/user/role/{role}")
    PagedResponse<UserResponse> getUsersByRole(
            @RequestHeader("Authorization") String authorization,
            @PathVariable("role") String role,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size);

    @GetMapping("/api/user/{id}")
    UserResponse getUserById(
            @RequestHeader("Authorization") String authorization,
            @PathVariable("id") Long id);

    @GetMapping("/api/user/email/{email}")
    UserResponse getUserByEmail(
            @RequestHeader("Authorization") String authorization,
            @PathVariable("email") String email);

    @PutMapping("/api/user/{id}")
    UserResponse updateUser(
            @RequestHeader("Authorization") String authorization,
            @PathVariable("id") Long id,
            @RequestBody AdminUserUpdateRequest request);

    @DeleteMapping("/api/user/{id}")
    Map<String, String> deleteUserById(
            @RequestHeader("Authorization") String authorization,
            @PathVariable("id") Long id);
}
