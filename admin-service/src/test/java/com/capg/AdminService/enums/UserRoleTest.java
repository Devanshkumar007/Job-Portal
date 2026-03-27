package com.capg.AdminService.enums;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.EnumSet;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class UserRoleTest {

    @Test
    @DisplayName("enum should contain JOB_SEEKER RECRUITER and ADMIN roles")
    void enum_ShouldContainExpectedRoles() {
        EnumSet<UserRole> roles = EnumSet.allOf(UserRole.class);

        assertEquals(3, roles.size());
        assertTrue(roles.contains(UserRole.JOB_SEEKER));
        assertTrue(roles.contains(UserRole.RECRUITER));
        assertTrue(roles.contains(UserRole.ADMIN));
    }

    @Test
    @DisplayName("valueOf should resolve ADMIN role by exact enum name")
    void valueOf_ShouldResolveAdmin() {
        assertEquals(UserRole.ADMIN, UserRole.valueOf("ADMIN"));
    }
}
