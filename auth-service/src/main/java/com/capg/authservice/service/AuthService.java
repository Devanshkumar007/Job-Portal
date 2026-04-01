package com.capg.authservice.service;

import com.capg.authservice.dto.request.LoginRequest;
import com.capg.authservice.dto.request.RegisterRequest;
import com.capg.authservice.dto.response.AuthResponse;
import com.capg.authservice.entity.User;
import com.capg.authservice.enums.UserRole;
import com.capg.authservice.exception.DuplicateEmailException;
import com.capg.authservice.exception.InvalidCredentialsException;
import com.capg.authservice.exception.RestrictedUser;
import com.capg.authservice.exception.UserNotFoundException;
import com.capg.authservice.repository.UserRepository;
import com.capg.authservice.security.JwtUtil;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private ModelMapper modelMapper;


    public AuthResponse register(RegisterRequest request) {
        String normalizedEmail = normalizeEmail(request.getEmail());

        // Step 1: Check if email already exists
        if (userRepository.existsByEmailIgnoreCase(normalizedEmail)) {
            throw new DuplicateEmailException(
                    "Email already registered: " + normalizedEmail);
        }
        User user = modelMapper.map(request, User.class);
        user.setEmail(normalizedEmail);
        user.setPassword(passwordEncoder.encode(request.getPassword()));

        UserRole role;
        try {
            role = UserRole.valueOf(request.getRole().toUpperCase());
        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid Role");
        }


        if (role == UserRole.ADMIN) {
            throw new RestrictedUser("Cannot assign ADMIN role");
        }
        user.setRole(role);


        User savedUser = userRepository.save(user);

        String token = jwtUtil.generateToken(savedUser);


        return buildAuthResponse(savedUser, token, "Registration successful!");
    }

    // =====================================================
    // LOGIN
    // =====================================================
    public AuthResponse login(LoginRequest request) {
        String normalizedEmail = normalizeEmail(request.getEmail());

        User user = userRepository.findByEmailIgnoreCase(normalizedEmail)
                .orElseThrow(() -> new UserNotFoundException(
                        "User not found with email: " + normalizedEmail));

        if (!passwordEncoder.matches(
                request.getPassword(), user.getPassword())) {
            throw new InvalidCredentialsException(
                    "Invalid email or password!");
        }

        String token = jwtUtil.generateToken(user);
        
        return buildAuthResponse(user, token, "Login successful!");
    }

    private AuthResponse buildAuthResponse(User user, String token,
                                           String message) {
        AuthResponse response = modelMapper.map(user, AuthResponse.class);
        response.setToken(token);
        response.setMessage(message);
        return response;
    }

    private String normalizeEmail(String email) {
        return email == null ? null : email.trim().toLowerCase();
    }
}
