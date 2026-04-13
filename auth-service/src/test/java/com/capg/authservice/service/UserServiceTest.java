package com.capg.authservice.service;

import com.capg.authservice.dto.response.UserDeletedEvent;
import com.capg.authservice.entity.User;
import com.capg.authservice.enums.UserRole;
import com.capg.authservice.exception.DuplicateEmailException;
import com.capg.authservice.exception.InvalidCredentialsException;
import com.capg.authservice.exception.UserNotFoundException;
import com.capg.authservice.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
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
class UserServiceTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private EventPublisher eventPublisher;

    @InjectMocks
    private UserService userService;

    @Test
    void deleteCurrentUserShouldDeleteAndPublishEvent() {
        User user = buildUser(5L, "user@mail.com", UserRole.RECRUITER);
        when(userRepository.findByEmailIgnoreCase("user@mail.com")).thenReturn(Optional.of(user));

        User deletedUser = userService.deleteCurrentUser(" USER@MAIL.COM ");

        assertEquals(5L, deletedUser.getId());
        verify(userRepository).delete(user);

        ArgumentCaptor<UserDeletedEvent> captor = ArgumentCaptor.forClass(UserDeletedEvent.class);
        verify(eventPublisher).publishUserDeletedEvent(captor.capture());
        assertEquals(5L, captor.getValue().getUserId());
        assertEquals("RECRUITER", captor.getValue().getRole());
    }

    @Test
    void updateUserShouldThrowWhenPayloadIsNull() {
        User user = buildUser(1L, "user@mail.com", UserRole.JOB_SEEKER);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        IllegalArgumentException exception =
                assertThrows(IllegalArgumentException.class, () -> userService.updateUser(1L, null));

        assertEquals("Updated user payload is required", exception.getMessage());
    }

    @Test
    void updateUserShouldThrowWhenNewEmailAlreadyExists() {
        User existing = buildUser(1L, "current@mail.com", UserRole.JOB_SEEKER);
        User update = new User();
        update.setEmail("new@mail.com");

        when(userRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(userRepository.existsByEmailIgnoreCase("new@mail.com")).thenReturn(true);

        assertThrows(DuplicateEmailException.class, () -> userService.updateUser(1L, update));
    }

    @Test
    void updateUserShouldApplyAdminLevelFieldsAndSave() {
        User existing = buildUser(2L, "current@mail.com", UserRole.JOB_SEEKER);
        existing.setPassword("old-encoded");

        User update = new User();
        update.setName("Updated Name");
        update.setEmail(" UPDATED@MAIL.COM ");
        update.setPhone("9999999999");
        update.setRole(UserRole.RECRUITER);
        update.setPassword("new-pass");

        when(userRepository.findById(2L)).thenReturn(Optional.of(existing));
        when(userRepository.existsByEmailIgnoreCase("updated@mail.com")).thenReturn(false);
        when(passwordEncoder.encode("new-pass")).thenReturn("new-encoded");
        when(userRepository.save(existing)).thenReturn(existing);

        User saved = userService.updateUser(2L, update);

        assertEquals("Updated Name", saved.getName());
        assertEquals("updated@mail.com", saved.getEmail());
        assertEquals("9999999999", saved.getPhone());
        assertEquals(UserRole.RECRUITER, saved.getRole());
        assertEquals("new-encoded", saved.getPassword());
    }

    @Test
    void updateCurrentUserShouldThrowWhenCurrentPasswordMissingForPasswordChange() {
        User existing = buildUser(3L, "user@mail.com", UserRole.JOB_SEEKER);
        existing.setPassword("encoded-old");

        User update = new User();
        update.setPassword("new-pass");

        when(userRepository.findByEmailIgnoreCase("user@mail.com")).thenReturn(Optional.of(existing));

        assertThrows(InvalidCredentialsException.class,
                () -> userService.updateCurrentUser("user@mail.com", update, null));
        verify(passwordEncoder, never()).encode(any());
    }

    @Test
    void updateCurrentUserShouldThrowWhenCurrentPasswordIsWrong() {
        User existing = buildUser(3L, "user@mail.com", UserRole.JOB_SEEKER);
        existing.setPassword("encoded-old");

        User update = new User();
        update.setPassword("new-pass");

        when(userRepository.findByEmailIgnoreCase("user@mail.com")).thenReturn(Optional.of(existing));
        when(passwordEncoder.matches("wrong", "encoded-old")).thenReturn(false);

        assertThrows(InvalidCredentialsException.class,
                () -> userService.updateCurrentUser("user@mail.com", update, "wrong"));
    }

    @Test
    void updateCurrentUserShouldUpdatePasswordButIgnoreRoleField() {
        User existing = buildUser(4L, "user@mail.com", UserRole.JOB_SEEKER);
        existing.setPassword("encoded-old");

        User update = new User();
        update.setName("New Name");
        update.setRole(UserRole.ADMIN); // Role updates are ignored for current user updates.
        update.setPassword("new-pass");

        when(userRepository.findByEmailIgnoreCase("user@mail.com")).thenReturn(Optional.of(existing));
        when(passwordEncoder.matches("current-pass", "encoded-old")).thenReturn(true);
        when(passwordEncoder.encode("new-pass")).thenReturn("encoded-new");
        when(userRepository.save(existing)).thenReturn(existing);

        User saved = userService.updateCurrentUser("user@mail.com", update, "current-pass");

        assertEquals("New Name", saved.getName());
        assertEquals(UserRole.JOB_SEEKER, saved.getRole());
        assertEquals("encoded-new", saved.getPassword());
    }

    @Test
    void getUserByEmailShouldThrowWhenNormalizedEmailIsNull() {
        assertThrows(UserNotFoundException.class, () -> userService.getUserByEmail("   "));
        verify(userRepository, never()).findByEmailIgnoreCase(any());
    }

    @Test
    void getAllUsersShouldClampNegativePageAndLargeSize() {
        when(userRepository.findAll(any(Pageable.class))).thenReturn(Page.empty());

        userService.getAllUsers(-5, 1000);

        ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);
        verify(userRepository).findAll(pageableCaptor.capture());
        assertEquals(0, pageableCaptor.getValue().getPageNumber());
        assertEquals(100, pageableCaptor.getValue().getPageSize());
    }

    @Test
    void getUsersByRoleShouldClampSizeAndUseUpperCaseRole() {
        when(userRepository.findByRole(eq(UserRole.ADMIN), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of()));

        Page<User> page = userService.getUsersByRole("admin", -1, 0);

        assertTrue(page.getContent().isEmpty());
        ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);
        verify(userRepository).findByRole(eq(UserRole.ADMIN), pageableCaptor.capture());
        assertEquals(0, pageableCaptor.getValue().getPageNumber());
        assertEquals(1, pageableCaptor.getValue().getPageSize());
    }

    @Test
    void getUsersByRoleShouldThrowForInvalidRole() {
        IllegalArgumentException exception =
                assertThrows(IllegalArgumentException.class,
                        () -> userService.getUsersByRole("invalid-role", 0, 10));

        assertEquals("Invalid role: invalid-role", exception.getMessage());
    }

    private User buildUser(Long id, String email, UserRole role) {
        User user = new User();
        user.setId(id);
        user.setName("Test");
        user.setEmail(email);
        user.setPassword("encoded");
        user.setRole(role);
        return user;
    }
}
