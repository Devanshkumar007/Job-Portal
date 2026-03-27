package com.capg.AdminService.util;

import com.capg.AdminService.exception.UnauthorizedException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

class AdminRoleValidatorTest {

    @Test
    @DisplayName("requireAdmin should allow exact ADMIN role")
    void requireAdmin_ShouldAllowExactAdminRole() {
        assertDoesNotThrow(() -> AdminRoleValidator.requireAdmin("ADMIN"));
    }

    @Test
    @DisplayName("requireAdmin should allow lowercase admin role")
    void requireAdmin_ShouldAllowLowercaseAdminRole() {
        assertDoesNotThrow(() -> AdminRoleValidator.requireAdmin("admin"));
    }

    @Test
    @DisplayName("requireAdmin should throw UnauthorizedException for non-admin role")
    void requireAdmin_ShouldThrowUnauthorized_WhenRoleIsNotAdmin() {
        assertThrows(UnauthorizedException.class, () -> AdminRoleValidator.requireAdmin("USER"));
    }

    @Test
    @DisplayName("requireAdmin should throw UnauthorizedException when role is null")
    void requireAdmin_ShouldThrowUnauthorized_WhenRoleIsNull() {
        assertThrows(UnauthorizedException.class, () -> AdminRoleValidator.requireAdmin(null));
    }
}
