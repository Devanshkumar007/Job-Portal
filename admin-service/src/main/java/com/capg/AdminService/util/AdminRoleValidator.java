package com.capg.AdminService.util;

import com.capg.AdminService.exception.UnauthorizedException;

public final class AdminRoleValidator {

    private AdminRoleValidator() {
    }

    public static void requireAdmin(String role) {
        if (!"ADMIN".equalsIgnoreCase(role)) {
            throw new UnauthorizedException("Only admins can access this resource");
        }
    }
}
