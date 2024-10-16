package com.login.loginsystem.service;

import com.login.loginsystem.model.Role;
import com.login.loginsystem.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class SecureUserDetailsTest {

    private User user;
    private SecureUserDetails secureUserDetails;

    @BeforeEach
    void setUp() {
        user = mock(User.class);
        secureUserDetails = new SecureUserDetails(user);
    }

    @Test
    void testGetAuthorities() {
        // Arrange
        Role role1 = mock(Role.class);
        when(role1.getName()).thenReturn("ROLE_USER");

        Role role2 = mock(Role.class);
        when(role2.getName()).thenReturn("ROLE_ADMIN");

        Set<Role> roles = new HashSet<>();
        roles.add(role1);
        roles.add(role2);
        when(user.getRoles()).thenReturn(roles); // Mock the roles of the user

        // Act
        Collection<GrantedAuthority> authorities = (Collection<GrantedAuthority>) secureUserDetails.getAuthorities();

        // Assert
        assertThat(authorities)
                .containsExactlyInAnyOrder(
                        new SimpleGrantedAuthority("ROLE_USER"),
                        new SimpleGrantedAuthority("ROLE_ADMIN")
                );
    }

    @Test
    void testGetPassword() {
        // Arrange
        String expectedPassword = "password123";
        when(user.getPassword()).thenReturn(expectedPassword);

        // Act
        String actualPassword = secureUserDetails.getPassword();

        // Assert
        assertThat(actualPassword).isEqualTo(expectedPassword);
    }

    @Test
    void testGetUsername() {
        // Arrange
        String expectedUsername = "testuser";
        when(user.getUsername()).thenReturn(expectedUsername);

        // Act
        String actualUsername = secureUserDetails.getUsername();

        // Assert
        assertThat(actualUsername).isEqualTo(expectedUsername);
    }

    @Test
    void testIsEnabled() {
        // Arrange
        when(user.isEnabled()).thenReturn(true);

        // Act
        boolean isEnabled = secureUserDetails.isEnabled();

        // Assert
        assertThat(isEnabled).isTrue();

        // Arrange for disabled case
        when(user.isEnabled()).thenReturn(false);

        // Act
        isEnabled = secureUserDetails.isEnabled();

        // Assert
        assertThat(isEnabled).isFalse();
    }
}
