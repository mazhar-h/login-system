package com.login.loginsystem.service;

import com.login.loginsystem.dto.UserCreateDto;
import com.login.loginsystem.dto.UserDto;
import com.login.loginsystem.dto.UserGetResponse;
import com.login.loginsystem.dto.UsernameEmailDto;
import com.login.loginsystem.model.PasswordResetToken;
import com.login.loginsystem.model.Role;
import com.login.loginsystem.model.User;
import com.login.loginsystem.model.VerificationToken;
import com.login.loginsystem.repository.PasswordResetTokenRepository;
import com.login.loginsystem.repository.RoleRepository;
import com.login.loginsystem.repository.UserRepository;
import com.login.loginsystem.repository.VerificationTokenRepository;
import com.login.loginsystem.util.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.crypto.password.PasswordEncoder;

import javax.management.relation.RoleNotFoundException;
import java.time.LocalDateTime;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

class UserServiceTest {

    @InjectMocks
    private UserService userService;
    @Mock
    private UserRepository userRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private JwtUtil jwtUtil;
    @Mock
    private RoleRepository roleRepository;
    @Mock
    private PasswordResetTokenRepository passwordResetTokenRepository;
    @Mock
    private EmailService emailService;
    @Mock
    private VerificationTokenRepository verificationTokenRepository;
    private User user;
    private PasswordResetToken passwordResetToken;
    private VerificationToken verificationToken;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        user = new User();
        user.setUsername("testUser");
        user.setEmail("test@example.com");
        user.setPassword("password");
        Set roles = new HashSet<Role>();
        roles.add(new Role("ROLE_USER"));
        user.setRoles(roles);
        user.setEnabled(false);
    }

    @Test
    void testRegisterUser() throws Exception {
        UserDto userDto = new UserDto();
        userDto.setUsername("testUser");
        userDto.setEmail("test@example.com");
        userDto.setPassword("password");

        Role role = new Role();
        role.setName("ROLE_USER");

        when(userRepository.existsByUsername(userDto.getUsername())).thenReturn(false);
        when(roleRepository.findByName("ROLE_USER")).thenReturn(role);
        when(passwordEncoder.encode(userDto.getPassword())).thenReturn("encodedPassword");

        userService.registerUser(userDto);

        verify(userRepository).save(any(User.class));
        verify(verificationTokenRepository).save(any(VerificationToken.class));
        verify(emailService).sendEmail(eq("test@example.com"), anyString(), anyString());
    }

    @Test
    void testRegisterUser_UsernameAlreadyExists_ThrowsException() {
        // Arrange
        UserDto userDto = new UserDto();
        userDto.setUsername("existingUser");
        userDto.setEmail("test@example.com");
        userDto.setPassword("password");

        when(userRepository.existsByUsername(userDto.getUsername())).thenReturn(true);

        // Act & Assert
        assertThatThrownBy(() -> userService.registerUser(userDto))
                .isInstanceOf(Exception.class)
                .hasMessageContaining("Username already exists");
    }

    @Test
    void testRegisterUser_RoleNotFound_ThrowsRuntimeException() {
        // Arrange
        UserDto userDto = new UserDto();
        userDto.setUsername("newUser");
        userDto.setEmail("test@example.com");
        userDto.setPassword("password");

        when(userRepository.existsByUsername(userDto.getUsername())).thenReturn(false);
        when(roleRepository.findByName("ROLE_USER")).thenReturn(null); // Role not found

        // Act & Assert
        assertThatThrownBy(() -> userService.registerUser(userDto))
                .isInstanceOf(RoleNotFoundException.class)
                .hasMessage("Role not found");
    }

    @Test
    void testGetUsername() {
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(jwtUtil.extractJwtFromRequest(request)).thenReturn("fakeToken");
        when(jwtUtil.extractUsername("fakeToken")).thenReturn("testUser");

        String username = userService.getUsername(request);
        assertThat(username).isEqualTo("testUser");
    }

    @Test
    void testGetAllUsers() {
        when(userRepository.findAll()).thenReturn(Collections.singletonList(user));

        List<UsernameEmailDto> users = userService.getAllUsers();
        assertThat(users).hasSize(1);
        assertThat(users.get(0).getUsername()).isEqualTo("testUser");
        assertThat(users.get(0).getEmail()).isEqualTo("test@example.com");
    }

    @Test
    void testGetUser() {
        when(userRepository.findByUsername("testUser")).thenReturn(user);

        UserGetResponse userResponse = userService.getUser("testUser");

        assertThat(userResponse.getUsername()).isEqualTo("testUser");
    }

    @Test
    void testCreateUser() {
        UserCreateDto userCreateDto = new UserCreateDto();
        userCreateDto.setUsername("newUser");
        userCreateDto.setEmail("new@example.com");
        userCreateDto.setRole("ROLE_USER");

        when(userRepository.findByUsername(userCreateDto.getUsername())).thenReturn(null);
        when(userRepository.findByEmail(userCreateDto.getEmail())).thenReturn(null);
        when(roleRepository.findByName(userCreateDto.getRole())).thenReturn(new Role());

        boolean result = userService.createUser(userCreateDto);

        assertThat(result).isTrue();
        verify(userRepository).save(any(User.class));
    }

    @Test
    void testCreateUser_UsernameAlreadyExists_ReturnsFalse() {
        // Arrange
        UserCreateDto userCreateDto = new UserCreateDto();
        userCreateDto.setUsername("existingUser");
        userCreateDto.setEmail("test@example.com");
        userCreateDto.setRole("ROLE_USER");

        User existingUser = new User();
        existingUser.setUsername("existingUser");

        when(userRepository.findByUsername(userCreateDto.getUsername())).thenReturn(existingUser);

        // Act
        boolean result = userService.createUser(userCreateDto);

        // Assert
        assertThat(result).isFalse();
    }

    @Test
    void testCreateUser_EmailAlreadyExists_ReturnsFalse() {
        // Arrange
        UserCreateDto userCreateDto = new UserCreateDto();
        userCreateDto.setUsername("newUser");
        userCreateDto.setEmail("existing@example.com");
        userCreateDto.setRole("ROLE_USER");

        User existingUser = new User();
        existingUser.setEmail("existing@example.com");

        when(userRepository.findByUsername(userCreateDto.getUsername())).thenReturn(null);
        when(userRepository.findByEmail(userCreateDto.getEmail())).thenReturn(existingUser);

        // Act
        boolean result = userService.createUser(userCreateDto);

        // Assert
        assertThat(result).isFalse();
    }

    @Test
    void testCreateUser_RoleDoesNotExist_ReturnsFalse() {
        // Arrange
        UserCreateDto userCreateDto = new UserCreateDto();
        userCreateDto.setUsername("newUser");
        userCreateDto.setEmail("test@example.com");
        userCreateDto.setRole("ROLE_INVALID");

        when(userRepository.findByUsername(userCreateDto.getUsername())).thenReturn(null);
        when(userRepository.findByEmail(userCreateDto.getEmail())).thenReturn(null);
        when(roleRepository.findByName(userCreateDto.getRole())).thenReturn(null); // Role not found

        // Act
        boolean result = userService.createUser(userCreateDto);

        // Assert
        assertThat(result).isFalse();
    }

    @Test
    void testUpdateUser() {
        when(userRepository.findByUsername("currentUser")).thenReturn(user);
        when(userRepository.existsByUsername("newUser")).thenReturn(false);
        when(userRepository.existsByEmail("new@example.com")).thenReturn(false);
        when(roleRepository.findByName("ROLE_USER")).thenReturn(new Role("ROLE_USER"));

        boolean result = userService.updateUser("currentUser", "newUser", "new@example.com", "ROLE_USER");

        assertThat(result).isTrue();
        assertThat(user.getUsername()).isEqualTo("newUser");
        assertThat(user.getEmail()).isEqualTo("new@example.com");
    }

    @Test
    void testUpdateUser_Branch() {
        when(userRepository.findByUsername("currentUser")).thenReturn(null);

        boolean result = userService.updateUser("currentUser", "newUser", "new@example.com", "ROLE_USER");

        assertThat(result).isFalse();
    }

    @Test
    void testUpdateUser_Branch2() {
        when(userRepository.findByUsername("currentUser")).thenReturn(user);
        when(userRepository.existsByUsername("newUser")).thenReturn(true);
        when(userRepository.existsByEmail("new@example.com")).thenReturn(true);
        when(roleRepository.findByName("ROLE_USER")).thenReturn(null);

        boolean result = userService.updateUser("currentUser", "newUser", "new@example.com", "ROLE_USER");

        assertThat(result).isTrue();
        assertThat(user.getUsername()).isEqualTo("testUser");
        assertThat(user.getEmail()).isEqualTo("test@example.com");
    }

    @Test
    void testUpdateUser_Branch3() {
        when(userRepository.findByUsername("currentUser")).thenReturn(user);
        when(userRepository.existsByUsername("newUser")).thenReturn(true);
        when(userRepository.existsByEmail("new@example.com")).thenReturn(true);
        when(roleRepository.findByName("ROLE_USER")).thenReturn(null);

        boolean result = userService.updateUser("currentUser", null, null, "ROLE_USER");

        assertThat(result).isTrue();
        assertThat(user.getUsername()).isEqualTo("testUser");
        assertThat(user.getEmail()).isEqualTo("test@example.com");
    }

    @Test
    void testDeleteUserByUsername_Success() {
        when(userRepository.existsByUsername("testUser")).thenReturn(true);

        boolean result = userService.deleteUserByUsername("testUser");

        assertThat(result).isTrue();
        verify(userRepository).deleteByUsername("testUser");
    }

    @Test
    void testDeleteUserByUsername_Fail() {
        when(userRepository.existsByUsername("testUser")).thenReturn(false);

        boolean result = userService.deleteUserByUsername("testUser");

        assertThat(result).isFalse();
    }

    @Test
    void testVerifyUserEmail() {
        String token = UUID.randomUUID().toString();
        VerificationToken verificationToken = new VerificationToken();
        verificationToken.setToken(token);
        verificationToken.setUser(user);
        verificationToken.setExpiryDate(LocalDateTime.now().plusDays(1));

        when(verificationTokenRepository.findByToken(token)).thenReturn(Optional.of(verificationToken));

        boolean result = userService.verifyUserEmail(token);

        assertThat(result).isTrue();
        assertThat(user.isEnabled()).isTrue();
        verify(verificationTokenRepository).delete(verificationToken);
    }

    @Test
    void testVerifyUserEmail_NoTokenFound_ReturnsFalse() {
        // Arrange
        String token = "invalidToken";
        when(verificationTokenRepository.findByToken(token)).thenReturn(Optional.empty());

        // Act
        boolean result = userService.verifyUserEmail(token);

        // Assert
        assertThat(result).isFalse();
    }

    @Test
    void testVerifyUserEmail_TokenExpired_ReturnsFalse() {
        // Arrange
        String token = "expiredToken";
        verificationToken = new VerificationToken();
        verificationToken.setExpiryDate(LocalDateTime.now().minusHours(1)); // Set expiry date to the past
        verificationToken.setUser(user);
        when(verificationTokenRepository.findByToken(token)).thenReturn(Optional.of(verificationToken));

        // Act
        boolean result = userService.verifyUserEmail(token);

        // Assert
        assertThat(result).isFalse();
    }

    @Test
    void testProcessForgotPassword() {
        String email = "test@example.com";
        when(userRepository.findByEmail(email)).thenReturn(user);

        boolean result = userService.processForgotPassword(email);

        assertThat(result).isTrue();
        verify(passwordResetTokenRepository).save(any(PasswordResetToken.class));
        verify(emailService).sendEmail(eq("test@example.com"), anyString(), anyString());
    }

    @Test
    void testResetPassword() {
        String token = UUID.randomUUID().toString();
        PasswordResetToken resetToken = new PasswordResetToken();
        resetToken.setToken(token);
        resetToken.setUser(user);
        resetToken.setExpiryDate(LocalDateTime.now().plusHours(1));

        when(passwordResetTokenRepository.findByToken(token)).thenReturn(Optional.of(resetToken));
        when(passwordEncoder.encode("newPassword")).thenReturn("encodedNewPassword");

        boolean result = userService.resetPassword(token, "newPassword");

        assertThat(result).isTrue();
        verify(userRepository).save(user);
        verify(passwordResetTokenRepository).delete(resetToken);
    }

    @Test
    void testResetPassword_NewPasswordIsNull_ReturnsFalse() {
        // Arrange
        String token = "validToken";
        when(passwordResetTokenRepository.findByToken(token)).thenReturn(Optional.of(new PasswordResetToken()));

        // Act
        boolean result = userService.resetPassword(token, null);

        // Assert
        assertThat(result).isFalse();
    }

    @Test
    void testResetPassword_NoTokenFound_ReturnsFalse() {
        // Arrange
        String token = "invalidToken";
        when(passwordResetTokenRepository.findByToken(token)).thenReturn(Optional.empty());

        // Act
        boolean result = userService.resetPassword(token, "newPassword123");

        // Assert
        assertThat(result).isFalse();
    }

    @Test
    void testResetPassword_TokenExpired_ReturnsFalse() {
        // Arrange
        String token = "expiredToken";
        passwordResetToken = new PasswordResetToken();
        passwordResetToken.setExpiryDate(LocalDateTime.now().minusHours(1)); // Set expiry date to the past
        when(passwordResetTokenRepository.findByToken(token)).thenReturn(Optional.of(passwordResetToken));

        // Act
        boolean result = userService.resetPassword(token, "newPassword123");

        // Assert
        assertThat(result).isFalse();
    }

    @Test
    void testGetUserByToken() {
        // Setup
        HttpServletRequest request = mock(HttpServletRequest.class);
        String token = "validToken";
        String username = "testUser";

        // Mocking the behavior of the JWT utility
        when(jwtUtil.extractJwtFromRequest(request)).thenReturn(token);
        when(jwtUtil.extractUsername(token)).thenReturn(username);
        when(userRepository.findByUsername(username)).thenReturn(user);

        // Call the method
        UserGetResponse userResponse = userService.getUserByToken(request);

        // Assertions
        assertThat(userResponse).isNotNull();
        assertThat(userResponse.getUsername()).isEqualTo(username);
        assertThat(userResponse.isEnabled()).isFalse(); // user is initially disabled
        assertThat(userResponse.getRoles()).isNotEmpty(); // Assuming user has roles
    }

    @Test
    void testGetUserByToken_UserNotFound() {
        // Setup
        HttpServletRequest request = mock(HttpServletRequest.class);
        String token = "validToken";
        String username = "unknownUser";

        // Mocking the behavior of the JWT utility
        when(jwtUtil.extractJwtFromRequest(request)).thenReturn(token);
        when(jwtUtil.extractUsername(token)).thenReturn(username);
        when(userRepository.findByUsername(username)).thenReturn(null);

        // Call the method
        UserGetResponse userResponse = userService.getUserByToken(request);

        // Assertions
        assertThat(userResponse).isNull(); // Expecting null response for unknown user
    }

    @Test
    void testGetUserByToken_InvalidToken() {
        // Setup
        HttpServletRequest request = mock(HttpServletRequest.class);
        String token = null; // Simulating invalid token

        // Mocking the behavior of the JWT utility
        when(jwtUtil.extractJwtFromRequest(request)).thenReturn(token);

        // Call the method
        UserGetResponse userResponse = userService.getUserByToken(request);

        // Assertions
        assertThat(userResponse).isNull(); // Expecting null response since token is invalid
    }

    @Test
    void testProcessForgotUsername_UserFound() {
        // Setup
        String email = "test@example.com";
        User user = new User();
        user.setUsername("testUser");
        user.setEmail(email);

        // Mocking the behavior of the user repository and email service
        when(userRepository.findByEmail(email)).thenReturn(user);
        doNothing().when(emailService).sendEmail(eq(email), eq("Forgotten Username"), contains("Your username is: testUser"));

        // Call the method
        boolean result = userService.processForgotUsername(email);

        // Assertions
        assertThat(result).isTrue(); // Expecting true since the user was found and email sent
        verify(emailService, times(1)).sendEmail(eq(email), eq("Forgotten Username"), contains("Your username is: testUser"));
    }

    @Test
    void testProcessForgotUsername_UserNotFound() {
        // Setup
        String email = "notfound@example.com";

        // Mocking the behavior of the user repository
        when(userRepository.findByEmail(email)).thenReturn(null); // No user found

        // Call the method
        boolean result = userService.processForgotUsername(email);

        // Assertions
        assertThat(result).isFalse(); // Expecting false since no user was found
        verify(emailService, never()).sendEmail(anyString(), anyString(), anyString()); // Ensure no email was sent
    }

    @Test
    void testResendVerificationEmail_UserFoundAndNotEnabled() {
        // Setup
        String username = "testUser";
        User user = new User();
        user.setId(1L);
        user.setUsername(username);
        user.setEnabled(false);
        user.setEmail("test@example.com");

        VerificationToken token = new VerificationToken();
        token.setToken("verificationToken");
        token.setUser(user);

        // Mocking the behavior of the user repository and verification token repository
        when(userRepository.findByUsername(username)).thenReturn(user);
        when(verificationTokenRepository.findByUserId(user.getId())).thenReturn(token);
        doNothing().when(emailService).sendEmail(eq(user.getEmail()), eq("Resend Email Verification"), contains("Click the link to verify your email:"));

        // Call the method
        boolean result = userService.resendVerificationEmail(username);

        // Assertions
        assertThat(result).isTrue(); // Expecting true since the user is found and not enabled
        verify(emailService, times(1)).sendEmail(eq(user.getEmail()), eq("Resend Email Verification"), contains("Click the link to verify your email:"));
    }

    @Test
    void testResendVerificationEmail_UserNotFound() {
        // Setup
        String username = "notFoundUser";

        // Mocking the behavior of the user repository
        when(userRepository.findByUsername(username)).thenReturn(null); // No user found

        // Call the method
        boolean result = userService.resendVerificationEmail(username);

        // Assertions
        assertThat(result).isFalse(); // Expecting false since no user was found
        verify(emailService, never()).sendEmail(anyString(), anyString(), anyString()); // Ensure no email was sent
    }

    @Test
    void testResendVerificationEmail_UserAlreadyEnabled() {
        // Setup
        String username = "enabledUser";
        User user = new User();
        user.setUsername(username);
        user.setEnabled(true);

        // Mocking the behavior of the user repository
        when(userRepository.findByUsername(username)).thenReturn(user); // User found but enabled

        // Call the method
        boolean result = userService.resendVerificationEmail(username);

        // Assertions
        assertThat(result).isFalse(); // Expecting false since the user is already enabled
        verify(emailService, never()).sendEmail(anyString(), anyString(), anyString()); // Ensure no email was sent
    }
}