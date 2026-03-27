package com.capg.authservice.service;

import com.capg.authservice.dto.response.UserDeletedEvent;
import com.capg.authservice.entity.User;
import com.capg.authservice.enums.UserRole;
import com.capg.authservice.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceSimpleTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private EventPublisher eventPublisher;

    @InjectMocks
    private UserService userService;

    @Test
    void deleteUserByIdShouldDeleteUserAndPublishDeletedEvent() {
        User user = new User();
        user.setId(7L);
        user.setEmail("test@mail.com");
        user.setRole(UserRole.RECRUITER);

        when(userRepository.findById(7L)).thenReturn(Optional.of(user));

        User deleted = userService.deleteUserById(7L);

        assertEquals(7L, deleted.getId());
        verify(userRepository).delete(user);

        ArgumentCaptor<UserDeletedEvent> captor = ArgumentCaptor.forClass(UserDeletedEvent.class);
        verify(eventPublisher).publishUserDeletedEvent(captor.capture());
        assertEquals(7L, captor.getValue().getUserId());
        assertEquals("RECRUITER", captor.getValue().getRole());
    }
}
