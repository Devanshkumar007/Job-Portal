package com.capg.authservice.dto.request;

import com.capg.authservice.enums.UserRole;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class AdminUserUpdateRequest {

    @Size(max = 100, message = "Name cannot be longer than 100 characters")
    private String name;

    @Email(message = "Invalid email format")
    private String email;

    @Size(min = 6, message = "Password must be at least 6 characters")
    private String password;

    @Size(max = 20, message = "Phone number cannot be longer than 20 characters")
    private String phone;

    private UserRole role;
}
