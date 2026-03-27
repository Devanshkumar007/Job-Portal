package com.capg.AdminService.service;

import com.capg.AdminService.dto.AdminUserUpdateRequest;
import com.capg.AdminService.dto.PagedResponse;
import com.capg.AdminService.dto.UserResponse;

import java.util.Map;

public interface AdminUserService {
    PagedResponse<UserResponse> getAllUsers(String authorization, String role, int page, int size);

    UserResponse getUserById(String authorization, String role, Long id);

    UserResponse getUserByEmail(String authorization, String role, String email);

    UserResponse updateUser(String authorization, String role, Long id, AdminUserUpdateRequest request);

    Map<String, String> deleteUser(String authorization, String role, Long id);
}
