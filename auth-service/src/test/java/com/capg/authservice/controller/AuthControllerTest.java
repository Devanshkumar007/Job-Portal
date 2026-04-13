package com.capg.authservice.controller;

import com.capg.authservice.dto.request.ChangePasswordRequest;
import com.capg.authservice.dto.request.ForgotPasswordRequest;
import com.capg.authservice.dto.request.LoginRequest;
import com.capg.authservice.dto.request.RegisterRequest;
import com.capg.authservice.dto.request.ResetPasswordRequest;
import com.capg.authservice.dto.response.AuthResponse;
import com.capg.authservice.service.AuthService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthControllerTest {

    @Mock
    private AuthService authService;
    @Mock
    private Authentication authentication;

    @InjectMocks
    private AuthController authController;

    @Test
    void registerShouldReturnCreatedResponse() {
        RegisterRequest request = new RegisterRequest();
        AuthResponse authResponse = new AuthResponse();
        authResponse.setMessage("Registration successful!");

        when(authService.register(request)).thenReturn(authResponse);

        ResponseEntity<AuthResponse> response = authController.register(request);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals(authResponse, response.getBody());
        verify(authService).register(request);
    }

    @Test
    void loginShouldReturnOkResponse() {
        LoginRequest request = new LoginRequest();
        AuthResponse authResponse = new AuthResponse();
        authResponse.setMessage("Login successful!");

        when(authService.login(request)).thenReturn(authResponse);

        ResponseEntity<AuthResponse> response = authController.login(request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(authResponse, response.getBody());
    }

    @Test
    void forgotPasswordShouldReturnMessagePayload() {
        ForgotPasswordRequest request = new ForgotPasswordRequest();
        when(authService.forgotPassword(request)).thenReturn("Reset link sent");

        ResponseEntity<Map<String, String>> response = authController.forgotPassword(request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Reset link sent", response.getBody().get("message"));
    }

    @Test
    void resetPasswordShouldReturnMessagePayload() {
        ResetPasswordRequest request = new ResetPasswordRequest();
        when(authService.resetPassword(request)).thenReturn("Password reset successful.");

        ResponseEntity<Map<String, String>> response = authController.resetPassword(request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Password reset successful.", response.getBody().get("message"));
    }

    @Test
    void changePasswordShouldUseAuthenticatedUserEmail() {
        ChangePasswordRequest request = new ChangePasswordRequest();
        when(authentication.getName()).thenReturn("user@mail.com");
        when(authService.changePassword("user@mail.com", request))
                .thenReturn("Password changed successfully.");

        ResponseEntity<Map<String, String>> response = authController.changePassword(request, authentication);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Password changed successfully.", response.getBody().get("message"));
        verify(authService).changePassword("user@mail.com", request);
    }
}
