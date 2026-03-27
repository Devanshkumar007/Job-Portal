package com.capg.AdminService.dto;

import com.capg.AdminService.enums.UserRole;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AdminUserUpdateRequestValidationTest {

    private final Validator validator;

    AdminUserUpdateRequestValidationTest() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        this.validator = factory.getValidator();
    }

    @Test
    @DisplayName("should have zero validation errors when all fields are valid")
    void shouldHaveNoViolations_WhenRequestIsValid() {
        AdminUserUpdateRequest request = new AdminUserUpdateRequest();
        request.setName("Devansh Kumar");
        request.setEmail("devansh@example.com");
        request.setPassword("secret123");
        request.setPhone("9876543210");
        request.setRole(UserRole.ADMIN);

        Set<ConstraintViolation<AdminUserUpdateRequest>> violations = validator.validate(request);

        assertTrue(violations.isEmpty());
    }

    @Test
    @DisplayName("should fail validation when email format is invalid")
    void shouldFailValidation_WhenEmailIsInvalid() {
        AdminUserUpdateRequest request = new AdminUserUpdateRequest();
        request.setEmail("invalid-email");

        Set<ConstraintViolation<AdminUserUpdateRequest>> violations = validator.validate(request);

        assertEquals(1, violations.size());
        assertEquals("email", violations.iterator().next().getPropertyPath().toString());
    }

    @Test
    @DisplayName("should fail validation when password length is less than six")
    void shouldFailValidation_WhenPasswordIsTooShort() {
        AdminUserUpdateRequest request = new AdminUserUpdateRequest();
        request.setPassword("12345");

        Set<ConstraintViolation<AdminUserUpdateRequest>> violations = validator.validate(request);

        assertEquals(1, violations.size());
        assertEquals("password", violations.iterator().next().getPropertyPath().toString());
    }

    @Test
    @DisplayName("should fail validation when name exceeds max allowed length")
    void shouldFailValidation_WhenNameIsTooLong() {
        AdminUserUpdateRequest request = new AdminUserUpdateRequest();
        request.setName("a".repeat(101));

        Set<ConstraintViolation<AdminUserUpdateRequest>> violations = validator.validate(request);

        assertEquals(1, violations.size());
        assertEquals("name", violations.iterator().next().getPropertyPath().toString());
    }

    @Test
    @DisplayName("should fail validation when phone exceeds max allowed length")
    void shouldFailValidation_WhenPhoneIsTooLong() {
        AdminUserUpdateRequest request = new AdminUserUpdateRequest();
        request.setPhone("1".repeat(21));

        Set<ConstraintViolation<AdminUserUpdateRequest>> violations = validator.validate(request);

        assertEquals(1, violations.size());
        assertEquals("phone", violations.iterator().next().getPropertyPath().toString());
    }
}
