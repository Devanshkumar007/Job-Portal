package com.capg.authservice.service;

import com.capg.authservice.dto.response.UserDeletedEvent;
import com.capg.authservice.entity.User;
import com.capg.authservice.exception.DuplicateEmailException;
import com.capg.authservice.exception.InvalidCredentialsException;
import com.capg.authservice.exception.UserNotFoundException;
import com.capg.authservice.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

@Service
public class UserService {
    private static final int MAX_PAGE_SIZE = 100;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private EventPublisher eventPublisher;

    @Transactional
    public User deleteUserById(Long userId) {
        User user = getUserById(userId);
        userRepository.delete(user);

        UserDeletedEvent event = new UserDeletedEvent(
                userId,
                user.getRole().name()
        );

        publishUserDeletedEventAfterCommit(event);

        return user;
    }

    @Transactional
    public User deleteCurrentUser(String email) {
        User user = getUserByEmail(email);
        userRepository.delete(user);

        UserDeletedEvent event = new UserDeletedEvent(
                user.getId(),
                user.getRole().name()
        );

        publishUserDeletedEventAfterCommit(event);
        return user;
    }

    public User updateUser(Long userId, User updatedUser) {
        User existingUser = getUserById(userId);
        return applyUpdates(existingUser, updatedUser, true);
    }

    public User updateCurrentUser(String email, User updatedUser, String currentPassword) {
        User existingUser = getUserByEmail(email);
        return applyUpdates(existingUser, updatedUser, false, currentPassword);
    }

    private User applyUpdates(User existingUser, User updatedUser, boolean allowRoleUpdate) {
        return applyUpdates(existingUser, updatedUser, allowRoleUpdate, null);
    }

    private User applyUpdates(
            User existingUser,
            User updatedUser,
            boolean allowRoleUpdate,
            String currentPassword) {
        if (updatedUser == null) {
            throw new IllegalArgumentException("Updated user payload is required");
        }

        String newEmail = normalizeEmail(updatedUser.getEmail());
        if (newEmail != null
                && !newEmail.equalsIgnoreCase(existingUser.getEmail())
                && userRepository.existsByEmailIgnoreCase(newEmail)) {
            throw new DuplicateEmailException("Email already registered: " + newEmail);
        }

        if (updatedUser.getName() != null) {
            existingUser.setName(updatedUser.getName());
        }
        if (newEmail != null) {
            existingUser.setEmail(newEmail);
        }
        if (updatedUser.getPhone() != null) {
            existingUser.setPhone(updatedUser.getPhone());
        }
        if (allowRoleUpdate && updatedUser.getRole() != null) {
            existingUser.setRole(updatedUser.getRole());
        }
        if (updatedUser.getPassword() != null
                && !updatedUser.getPassword().isBlank()) {
            if (!allowRoleUpdate && (currentPassword == null
                    || !passwordEncoder.matches(currentPassword, existingUser.getPassword()))) {
                throw new InvalidCredentialsException("Current password is incorrect");
            }
            existingUser.setPassword(passwordEncoder.encode(updatedUser.getPassword()));
        }

        return userRepository.save(existingUser);
    }

    public User getUserById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(
                        "User not found with id: " + userId));
    }

    public User getUserByEmail(String email) {
        String normalizedEmail = normalizeEmail(email);
        if (normalizedEmail == null) {
            throw new UserNotFoundException("User not found with email: " + email);
        }
        return userRepository.findByEmailIgnoreCase(normalizedEmail)
                .orElseThrow(() -> new UserNotFoundException(
                        "User not found with email: " + normalizedEmail));
    }

    public Page<User> getAllUsers(int page, int size) {
        int safePage = Math.max(page, 0);
        int safeSize = Math.min(Math.max(size, 1), MAX_PAGE_SIZE);

        PageRequest pageRequest = PageRequest.of(
                safePage,
                safeSize,
                Sort.by(Sort.Direction.DESC, "id")
        );
        return userRepository.findAll(pageRequest);
    }

    public Page<User> getUsersByRole(String role, int page, int size) {
        int safePage = Math.max(page, 0);
        int safeSize = Math.min(Math.max(size, 1), MAX_PAGE_SIZE);

        try {
            com.capg.authservice.enums.UserRole userRole =
                    com.capg.authservice.enums.UserRole.valueOf(role.toUpperCase());

            PageRequest pageRequest = PageRequest.of(
                    safePage,
                    safeSize,
                    Sort.by(Sort.Direction.DESC, "id")
            );
            return userRepository.findByRole(userRole, pageRequest);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid role: " + role);
        }
    }

    private String normalizeEmail(String email) {
        if (email == null) {
            return null;
        }
        String trimmedEmail = email.trim().toLowerCase();
        return trimmedEmail.isEmpty() ? null : trimmedEmail;
    }

    private void publishUserDeletedEventAfterCommit(UserDeletedEvent event) {
        if (!TransactionSynchronizationManager.isActualTransactionActive()) {
            eventPublisher.publishUserDeletedEvent(event);
            return;
        }

        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                eventPublisher.publishUserDeletedEvent(event);
            }
        });
    }
}
