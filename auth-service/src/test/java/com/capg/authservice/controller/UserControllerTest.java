package com.capg.authservice.controller;

import com.capg.authservice.dto.request.AdminUserUpdateRequest;
import com.capg.authservice.dto.request.UserUpdateRequest;
import com.capg.authservice.dto.response.UserResponse;
import com.capg.authservice.entity.User;
import com.capg.authservice.enums.UserRole;
import com.capg.authservice.service.UserService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserControllerTest {

    @Mock
    private UserService userService;
    @Mock
    private Authentication authentication;

    @InjectMocks
    private UserController userController;

    @Test
    void getCurrentUserShouldReturnMappedResponse() {
        User user = buildUser(1L, "User One", "user1@mail.com", UserRole.JOB_SEEKER);
        when(authentication.getName()).thenReturn("user1@mail.com");
        when(userService.getUserByEmail("user1@mail.com")).thenReturn(user);

        ResponseEntity<UserResponse> response = userController.getCurrentUser(authentication);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("User One", response.getBody().getName());
        assertEquals("user1@mail.com", response.getBody().getEmail());
    }

    @Test
    void updateCurrentUserShouldMapRequestAndReturnUpdatedUser() {
        UserUpdateRequest request = new UserUpdateRequest();
        request.setName("Updated Name");
        request.setEmail("updated@mail.com");
        request.setPhone("9999");
        request.setPassword("new-pass");
        request.setCurrentPassword("old-pass");

        User updated = buildUser(2L, "Updated Name", "updated@mail.com", UserRole.RECRUITER);

        when(authentication.getName()).thenReturn("user@mail.com");
        when(userService.updateCurrentUser(any(), any(User.class), any())).thenReturn(updated);

        ResponseEntity<UserResponse> response = userController.updateCurrentUser(request, authentication);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Updated Name", response.getBody().getName());
        assertEquals(UserRole.RECRUITER, response.getBody().getRole());

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userService).updateCurrentUser(
                org.mockito.ArgumentMatchers.eq("user@mail.com"),
                userCaptor.capture(),
                org.mockito.ArgumentMatchers.eq("old-pass"));
        assertEquals("updated@mail.com", userCaptor.getValue().getEmail());
    }

    @Test
    void deleteCurrentUserShouldReturnMessageAndDeletedId() {
        User deleted = buildUser(3L, "Delete Me", "delete@mail.com", UserRole.JOB_SEEKER);
        when(authentication.getName()).thenReturn("delete@mail.com");
        when(userService.deleteCurrentUser("delete@mail.com")).thenReturn(deleted);

        ResponseEntity<Map<String, String>> response = userController.deleteCurrentUser(authentication);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Your account has been deleted successfully.", response.getBody().get("message"));
        assertEquals("3", response.getBody().get("deletedUserId"));
    }

    @Test
    void getUserByIdShouldReturnMappedResponse() {
        User user = buildUser(4L, "Admin Target", "target@mail.com", UserRole.RECRUITER);
        when(userService.getUserById(4L)).thenReturn(user);

        ResponseEntity<UserResponse> response = userController.getUserById(4L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(4L, response.getBody().getId());
        assertEquals("target@mail.com", response.getBody().getEmail());
    }

    @Test
    void getAllUsersShouldMapEachEntityInPage() {
        User first = buildUser(1L, "One", "one@mail.com", UserRole.JOB_SEEKER);
        User second = buildUser(2L, "Two", "two@mail.com", UserRole.RECRUITER);
        Page<User> users = new PageImpl<>(List.of(first, second));

        when(userService.getAllUsers(0, 5)).thenReturn(users);

        ResponseEntity<Page<UserResponse>> response = userController.getAllUsers(0, 5);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(2, response.getBody().getContent().size());
        assertEquals("one@mail.com", response.getBody().getContent().get(0).getEmail());
        assertEquals(UserRole.RECRUITER, response.getBody().getContent().get(1).getRole());
    }

    @Test
    void getUsersByRoleShouldForwardRoleAndMapPage() {
        User admin = buildUser(8L, "Admin", "admin@mail.com", UserRole.ADMIN);
        when(userService.getUsersByRole("ADMIN", 1, 10))
                .thenReturn(new PageImpl<>(List.of(admin)));

        ResponseEntity<Page<UserResponse>> response = userController.getUsersByRole("ADMIN", 1, 10);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(1, response.getBody().getContent().size());
        assertEquals(UserRole.ADMIN, response.getBody().getContent().get(0).getRole());
    }

    @Test
    void updateUserShouldMapAdminRequestIncludingRole() {
        AdminUserUpdateRequest request = new AdminUserUpdateRequest();
        request.setName("Edited");
        request.setEmail("edited@mail.com");
        request.setPhone("11111");
        request.setPassword("new-pass");
        request.setRole(UserRole.RECRUITER);

        User updated = buildUser(9L, "Edited", "edited@mail.com", UserRole.RECRUITER);
        when(userService.updateUser(org.mockito.ArgumentMatchers.eq(9L), any(User.class))).thenReturn(updated);

        ResponseEntity<UserResponse> response = userController.updateUser(9L, request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(UserRole.RECRUITER, response.getBody().getRole());

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userService).updateUser(org.mockito.ArgumentMatchers.eq(9L), userCaptor.capture());
        assertEquals("edited@mail.com", userCaptor.getValue().getEmail());
        assertEquals(UserRole.RECRUITER, userCaptor.getValue().getRole());
    }

    @Test
    void deleteUserByIdShouldReturnMessageAndDeletedUserId() {
        User deleted = buildUser(10L, "Removed", "removed@mail.com", UserRole.JOB_SEEKER);
        when(userService.deleteUserById(10L)).thenReturn(deleted);

        ResponseEntity<Map<String, String>> response = userController.deleteUserById(10L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("User deleted successfully.", response.getBody().get("message"));
        assertEquals("10", response.getBody().get("deletedUserId"));
    }

    @Test
    void getCurrentUserShouldAllowNullCreatedAtInResponse() {
        User user = buildUser(11L, "No Date", "nodate@mail.com", UserRole.JOB_SEEKER);
        user.setCreatedAt(null);
        when(authentication.getName()).thenReturn("nodate@mail.com");
        when(userService.getUserByEmail("nodate@mail.com")).thenReturn(user);

        ResponseEntity<UserResponse> response = userController.getCurrentUser(authentication);

        assertNull(response.getBody().getCreatedAt());
    }

    private User buildUser(Long id, String name, String email, UserRole role) {
        User user = new User();
        user.setId(id);
        user.setName(name);
        user.setEmail(email);
        user.setPassword("encoded");
        user.setPhone("9876543210");
        user.setRole(role);
        user.setCreatedAt(LocalDateTime.of(2025, 1, 1, 10, 0));
        return user;
    }
}
