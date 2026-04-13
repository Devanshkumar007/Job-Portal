package com.capg.authservice.service;

import com.capg.authservice.dto.request.ChangePasswordRequest;
import com.capg.authservice.dto.request.ForgotPasswordRequest;
import com.capg.authservice.dto.request.LoginRequest;
import com.capg.authservice.dto.request.RegisterRequest;
import com.capg.authservice.dto.request.ResetPasswordRequest;
import com.capg.authservice.dto.response.AuthResponse;
import com.capg.authservice.dto.response.ForgotPasswordEvent;
import com.capg.authservice.entity.User;
import com.capg.authservice.enums.UserRole;
import com.capg.authservice.exception.DuplicateEmailException;
import com.capg.authservice.exception.InvalidCredentialsException;
import com.capg.authservice.exception.RestrictedUser;
import com.capg.authservice.exception.UserNotFoundException;
import com.capg.authservice.repository.UserRepository;
import com.capg.authservice.security.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private JwtUtil jwtUtil;
    @Mock
    private ModelMapper modelMapper;
    @Mock
    private EventPublisher eventPublisher;

    @InjectMocks
    private AuthService authService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(authService, "passwordResetExpirationMs", 900_000L);
        ReflectionTestUtils.setField(authService, "passwordResetBaseUrl", "http://frontend/reset-password");
    }

    @Test
    void registerShouldCreateUserAndReturnAuthResponse() {
        RegisterRequest request = new RegisterRequest();
        request.setName("Alex");
        request.setEmail("  ALEX@MAIL.COM ");
        request.setPassword("secret123");
        request.setRole("job_seeker");

        User mappedUser = new User();
        mappedUser.setName("Alex");
        mappedUser.setEmail("temp@mail.com");
        mappedUser.setRole(UserRole.ADMIN);

        User savedUser = new User();
        savedUser.setId(11L);
        savedUser.setName("Alex");
        savedUser.setEmail("alex@mail.com");
        savedUser.setRole(UserRole.JOB_SEEKER);

        AuthResponse mappedResponse = new AuthResponse();
        mappedResponse.setId(11L);
        mappedResponse.setEmail("alex@mail.com");

        when(userRepository.existsByEmailIgnoreCase("alex@mail.com")).thenReturn(false);
        when(modelMapper.map(request, User.class)).thenReturn(mappedUser);
        when(passwordEncoder.encode("secret123")).thenReturn("encoded-pass");
        when(userRepository.save(mappedUser)).thenReturn(savedUser);
        when(jwtUtil.generateToken(savedUser)).thenReturn("jwt-token");
        when(modelMapper.map(savedUser, AuthResponse.class)).thenReturn(mappedResponse);

        AuthResponse response = authService.register(request);

        assertEquals("Registration successful!", response.getMessage());
        assertEquals("jwt-token", response.getToken());
        assertEquals("alex@mail.com", mappedUser.getEmail());
        assertEquals("encoded-pass", mappedUser.getPassword());
        assertEquals(UserRole.JOB_SEEKER, mappedUser.getRole());
    }

    @Test
    void registerShouldThrowWhenEmailAlreadyExists() {
        RegisterRequest request = new RegisterRequest();
        request.setEmail("duplicate@mail.com");

        when(userRepository.existsByEmailIgnoreCase("duplicate@mail.com")).thenReturn(true);

        assertThrows(DuplicateEmailException.class, () -> authService.register(request));
    }

    @Test
    void registerShouldThrowForInvalidRole() {
        RegisterRequest request = new RegisterRequest();
        request.setEmail("user@mail.com");
        request.setRole("not-a-role");

        User mappedUser = new User();
        when(userRepository.existsByEmailIgnoreCase("user@mail.com")).thenReturn(false);
        when(modelMapper.map(request, User.class)).thenReturn(mappedUser);
        when(passwordEncoder.encode(any())).thenReturn("encoded");

        IllegalArgumentException exception =
                assertThrows(IllegalArgumentException.class, () -> authService.register(request));

        assertEquals("Invalid Role", exception.getMessage());
    }

    @Test
    void registerShouldThrowWhenRoleIsAdmin() {
        RegisterRequest request = new RegisterRequest();
        request.setEmail("user@mail.com");
        request.setRole("ADMIN");

        User mappedUser = new User();
        when(userRepository.existsByEmailIgnoreCase("user@mail.com")).thenReturn(false);
        when(modelMapper.map(request, User.class)).thenReturn(mappedUser);
        when(passwordEncoder.encode(any())).thenReturn("encoded");

        assertThrows(RestrictedUser.class, () -> authService.register(request));
    }

    @Test
    void loginShouldReturnAuthResponseWhenCredentialsAreValid() {
        LoginRequest request = new LoginRequest();
        request.setEmail(" USER@MAIL.COM ");
        request.setPassword("raw-pass");

        User user = new User();
        user.setId(15L);
        user.setEmail("user@mail.com");
        user.setPassword("encoded-pass");
        user.setRole(UserRole.RECRUITER);

        AuthResponse mappedResponse = new AuthResponse();
        mappedResponse.setId(15L);
        mappedResponse.setRole(UserRole.RECRUITER);

        when(userRepository.findByEmailIgnoreCase("user@mail.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("raw-pass", "encoded-pass")).thenReturn(true);
        when(jwtUtil.generateToken(user)).thenReturn("login-token");
        when(modelMapper.map(user, AuthResponse.class)).thenReturn(mappedResponse);

        AuthResponse response = authService.login(request);

        assertEquals("Login successful!", response.getMessage());
        assertEquals("login-token", response.getToken());
        verify(userRepository).findByEmailIgnoreCase("user@mail.com");
    }

    @Test
    void loginShouldThrowWhenUserIsMissing() {
        LoginRequest request = new LoginRequest();
        request.setEmail("missing@mail.com");
        request.setPassword("pass");

        when(userRepository.findByEmailIgnoreCase("missing@mail.com")).thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class, () -> authService.login(request));
    }

    @Test
    void loginShouldThrowWhenPasswordDoesNotMatch() {
        LoginRequest request = new LoginRequest();
        request.setEmail("user@mail.com");
        request.setPassword("wrong");

        User user = new User();
        user.setEmail("user@mail.com");
        user.setPassword("encoded");

        when(userRepository.findByEmailIgnoreCase("user@mail.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("wrong", "encoded")).thenReturn(false);

        assertThrows(InvalidCredentialsException.class, () -> authService.login(request));
    }

    @Test
    void forgotPasswordShouldPublishEventWhenUserExists() {
        ForgotPasswordRequest request = new ForgotPasswordRequest();
        request.setEmail("  user@mail.com ");

        User user = new User();
        user.setEmail("user@mail.com");
        user.setName("John");

        when(userRepository.findByEmailIgnoreCase("user@mail.com")).thenReturn(Optional.of(user));
        when(jwtUtil.generatePasswordResetToken(user, 900_000L)).thenReturn("reset-token");

        String message = authService.forgotPassword(request);

        ArgumentCaptor<ForgotPasswordEvent> captor = ArgumentCaptor.forClass(ForgotPasswordEvent.class);
        verify(eventPublisher).publishForgotPasswordEvent(captor.capture());
        assertEquals("user@mail.com", captor.getValue().getEmail());
        assertTrue(captor.getValue().getResetLink().contains("?token=reset-token"));
        assertEquals("If the email is registered, a reset link has been sent.", message);
    }

    @Test
    void forgotPasswordShouldAppendTokenWithAmpersandWhenBaseUrlHasQueryString() {
        ReflectionTestUtils.setField(authService, "passwordResetBaseUrl", "http://frontend/reset-password?source=app");

        ForgotPasswordRequest request = new ForgotPasswordRequest();
        request.setEmail("user@mail.com");

        User user = new User();
        user.setEmail("user@mail.com");
        user.setName("Jane");

        when(userRepository.findByEmailIgnoreCase("user@mail.com")).thenReturn(Optional.of(user));
        when(jwtUtil.generatePasswordResetToken(user, 900_000L)).thenReturn("token-2");

        authService.forgotPassword(request);

        ArgumentCaptor<ForgotPasswordEvent> captor = ArgumentCaptor.forClass(ForgotPasswordEvent.class);
        verify(eventPublisher).publishForgotPasswordEvent(captor.capture());
        assertTrue(captor.getValue().getResetLink().contains("&token=token-2"));
    }

    @Test
    void forgotPasswordShouldNotPublishEventWhenUserIsMissing() {
        ForgotPasswordRequest request = new ForgotPasswordRequest();
        request.setEmail("unknown@mail.com");

        when(userRepository.findByEmailIgnoreCase("unknown@mail.com")).thenReturn(Optional.empty());

        String message = authService.forgotPassword(request);

        verify(eventPublisher, never()).publishForgotPasswordEvent(any());
        assertEquals("If the email is registered, a reset link has been sent.", message);
    }

    @Test
    void resetPasswordShouldThrowWhenPasswordsDoNotMatch() {
        ResetPasswordRequest request = new ResetPasswordRequest();
        request.setToken("token");
        request.setNewPassword("new1234");
        request.setConfirmPassword("different");

        assertThrows(IllegalArgumentException.class, () -> authService.resetPassword(request));
    }

    @Test
    void resetPasswordShouldThrowWhenTokenIsInvalid() {
        ResetPasswordRequest request = new ResetPasswordRequest();
        request.setToken("invalid-token");
        request.setNewPassword("new1234");
        request.setConfirmPassword("new1234");

        when(jwtUtil.isValidPasswordResetToken("invalid-token")).thenReturn(false);

        assertThrows(IllegalArgumentException.class, () -> authService.resetPassword(request));
    }

    @Test
    void resetPasswordShouldThrowWhenUserIsMissing() {
        ResetPasswordRequest request = new ResetPasswordRequest();
        request.setToken("valid-token");
        request.setNewPassword("new1234");
        request.setConfirmPassword("new1234");

        when(jwtUtil.isValidPasswordResetToken("valid-token")).thenReturn(true);
        when(jwtUtil.extractEmail("valid-token")).thenReturn("missing@mail.com");
        when(userRepository.findByEmailIgnoreCase("missing@mail.com")).thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class, () -> authService.resetPassword(request));
    }

    @Test
    void resetPasswordShouldUpdatePasswordWhenInputIsValid() {
        ResetPasswordRequest request = new ResetPasswordRequest();
        request.setToken("valid-token");
        request.setNewPassword("new1234");
        request.setConfirmPassword("new1234");

        User user = new User();
        user.setEmail("user@mail.com");
        user.setPassword("old");

        when(jwtUtil.isValidPasswordResetToken("valid-token")).thenReturn(true);
        when(jwtUtil.extractEmail("valid-token")).thenReturn(" USER@MAIL.COM ");
        when(userRepository.findByEmailIgnoreCase("user@mail.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.encode("new1234")).thenReturn("encoded-new");

        String message = authService.resetPassword(request);

        assertEquals("Password reset successful.", message);
        assertEquals("encoded-new", user.getPassword());
        verify(userRepository).save(user);
    }

    @Test
    void changePasswordShouldThrowWhenPasswordsDoNotMatch() {
        ChangePasswordRequest request = new ChangePasswordRequest();
        request.setCurrentPassword("old");
        request.setNewPassword("new1234");
        request.setConfirmPassword("different");

        assertThrows(IllegalArgumentException.class,
                () -> authService.changePassword("user@mail.com", request));
    }

    @Test
    void changePasswordShouldThrowWhenUserIsMissing() {
        ChangePasswordRequest request = new ChangePasswordRequest();
        request.setCurrentPassword("old");
        request.setNewPassword("new1234");
        request.setConfirmPassword("new1234");

        when(userRepository.findByEmailIgnoreCase("user@mail.com")).thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class,
                () -> authService.changePassword(" user@mail.com ", request));
    }

    @Test
    void changePasswordShouldThrowWhenCurrentPasswordIsWrong() {
        ChangePasswordRequest request = new ChangePasswordRequest();
        request.setCurrentPassword("wrong");
        request.setNewPassword("new1234");
        request.setConfirmPassword("new1234");

        User user = new User();
        user.setEmail("user@mail.com");
        user.setPassword("encoded-old");

        when(userRepository.findByEmailIgnoreCase("user@mail.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("wrong", "encoded-old")).thenReturn(false);

        assertThrows(InvalidCredentialsException.class,
                () -> authService.changePassword("user@mail.com", request));
    }

    @Test
    void changePasswordShouldUpdatePasswordWhenCurrentPasswordMatches() {
        ChangePasswordRequest request = new ChangePasswordRequest();
        request.setCurrentPassword("old-pass");
        request.setNewPassword("new1234");
        request.setConfirmPassword("new1234");

        User user = new User();
        user.setEmail("user@mail.com");
        user.setPassword("encoded-old");

        when(userRepository.findByEmailIgnoreCase("user@mail.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("old-pass", "encoded-old")).thenReturn(true);
        when(passwordEncoder.encode("new1234")).thenReturn("encoded-new");

        String message = authService.changePassword("user@mail.com", request);

        assertEquals("Password changed successfully.", message);
        assertEquals("encoded-new", user.getPassword());
        verify(userRepository).save(user);
        verify(userRepository).findByEmailIgnoreCase(eq("user@mail.com"));
    }
}
