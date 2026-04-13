package com.capg.authservice.security;

import com.capg.authservice.entity.User;
import com.capg.authservice.enums.UserRole;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class JwtUtilTest {

    private JwtUtil jwtUtil;

    @BeforeEach
    void setUp() {
        jwtUtil = new JwtUtil();
        // 32+ byte secret for HS256 signing.
        ReflectionTestUtils.setField(jwtUtil, "jwtSecret", "12345678901234567890123456789012");
        ReflectionTestUtils.setField(jwtUtil, "jwtExpiration", 120_000L);
    }

    @Test
    void generateTokenShouldCreateValidTokenForMatchingEmail() {
        User user = buildUser(1L, "user@mail.com", UserRole.JOB_SEEKER);

        String token = jwtUtil.generateToken(user);

        assertEquals("user@mail.com", jwtUtil.extractEmail(token));
        assertTrue(jwtUtil.validateToken(token, "user@mail.com"));
        assertFalse(jwtUtil.validateToken(token, "other@mail.com"));
    }

    @Test
    void generatePasswordResetTokenShouldBeRecognizedAsValid() {
        User user = buildUser(2L, "reset@mail.com", UserRole.RECRUITER);

        String token = jwtUtil.generatePasswordResetToken(user, 60_000L);

        assertTrue(jwtUtil.isValidPasswordResetToken(token));
        assertEquals("reset@mail.com", jwtUtil.extractEmail(token));
    }

    @Test
    void passwordResetTokenShouldBeInvalidWhenExpired() {
        User user = buildUser(3L, "expired@mail.com", UserRole.JOB_SEEKER);

        String token = jwtUtil.generatePasswordResetToken(user, -1L);

        assertFalse(jwtUtil.isValidPasswordResetToken(token));
    }

    @Test
    void invalidTokenShouldFailValidation() {
        assertFalse(jwtUtil.validateToken("not-a-token", "user@mail.com"));
        assertFalse(jwtUtil.isValidPasswordResetToken("not-a-token"));
    }

    private User buildUser(Long id, String email, UserRole role) {
        User user = new User();
        user.setId(id);
        user.setEmail(email);
        user.setRole(role);
        return user;
    }
}
