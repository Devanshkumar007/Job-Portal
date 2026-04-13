package com.capg.authservice.security;

import com.capg.authservice.entity.User;
import com.capg.authservice.enums.UserRole;
import com.capg.authservice.exception.UserNotFoundException;
import com.capg.authservice.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserDetailsServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserDetailsServiceImpl userDetailsService;

    @Test
    void loadUserByUsernameShouldReturnSpringSecurityUserWithRoleAuthority() {
        User user = new User();
        user.setEmail("user@mail.com");
        user.setPassword("encoded-pass");
        user.setRole(UserRole.ADMIN);

        when(userRepository.findByEmailIgnoreCase("user@mail.com")).thenReturn(Optional.of(user));

        UserDetails userDetails = userDetailsService.loadUserByUsername("user@mail.com");

        assertEquals("user@mail.com", userDetails.getUsername());
        assertEquals("encoded-pass", userDetails.getPassword());
        assertEquals("ROLE_ADMIN", userDetails.getAuthorities().iterator().next().getAuthority());
    }

    @Test
    void loadUserByUsernameShouldThrowWhenUserDoesNotExist() {
        when(userRepository.findByEmailIgnoreCase("missing@mail.com")).thenReturn(Optional.empty());

        UserNotFoundException exception = assertThrows(
                UserNotFoundException.class,
                () -> userDetailsService.loadUserByUsername("missing@mail.com"));

        assertEquals("User not found with email: missing@mail.com", exception.getMessage());
    }
}
