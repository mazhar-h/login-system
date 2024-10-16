package com.login.loginsystem.service;

import com.login.loginsystem.model.User;
import com.login.loginsystem.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

public class SecureUserDetailsServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private SecureUserDetailsService secureUserDetailsService;

    private User user;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        user = new User();
    }

    @Test
    void testLoadUserByUsername_UserFound() {
        // Arrange
        String username = "testuser";
        user.setUsername(username);
        user.setEnabled(true);
        when(userRepository.findByUsername(username)).thenReturn(user);

        // Act
        UserDetails userDetails = secureUserDetailsService.loadUserByUsername(username);

        // Assert
        assertThat(userDetails.getUsername()).isEqualTo(username);
        verify(userRepository).findByUsername(username);
    }

    @Test
    void testLoadUserByUsername_UserNotFound() {
        // Arrange
        String username = "testuser";
        when(userRepository.findByUsername(username)).thenReturn(null);

        // Act and Assert
        assertThatThrownBy(() -> secureUserDetailsService.loadUserByUsername(username))
                .isInstanceOf(UsernameNotFoundException.class)
                .hasMessage("User not found");
        verify(userRepository).findByUsername(username);
    }

    @Test
    void testLoadUserByUsername_UserDisabled() {
        // Arrange
        String username = "testuser";
        user.setUsername(username);
        user.setEnabled(false);
        when(userRepository.findByUsername(username)).thenReturn(user);

        // Act and Assert
        assertThatThrownBy(() -> secureUserDetailsService.loadUserByUsername(username))
                .isInstanceOf(DisabledException.class)
                .hasMessage("User is disabled");
        verify(userRepository).findByUsername(username);
    }
}
