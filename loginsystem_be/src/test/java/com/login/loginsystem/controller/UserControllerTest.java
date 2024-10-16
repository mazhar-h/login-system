package com.login.loginsystem.controller;

import com.login.loginsystem.dto.*;
import com.login.loginsystem.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

class UserControllerTest {

    @Mock
    private UserService userService;

    @Mock
    private HttpServletRequest request;

    @InjectMocks
    private UserController userController;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testHelloWorld() {
        when(userService.getUsername(request)).thenReturn("John");

        String result = userController.helloWorld(request);

        assertEquals("Hello John", result);
    }

    @Test
    void testHelloWorld2() {
        when(userService.getUsername(request)).thenReturn("Admin");

        String result = userController.helloWorld2(request);

        assertEquals("Hello admin Admin", result);
    }

    @Test
    void testGetAllUsers() {
        List<UsernameEmailDto> users = new ArrayList<>();
        users.add(new UsernameEmailDto("user1", "user1@example.com"));
        users.add(new UsernameEmailDto("user2", "user2@example.com"));

        when(userService.getAllUsers()).thenReturn(users);

        ResponseEntity<List<UsernameEmailDto>> response = userController.getAllUsers();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(users, response.getBody());
    }

    @Test
    void testGetUser_Success() {
        String username = "testUser";
        UserGetResponse userResponse = new UserGetResponse();
        userResponse.setUsername(username);

        when(userService.getUser(username)).thenReturn(userResponse);

        ResponseEntity<?> response = userController.getUser(username);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(userResponse, response.getBody());
    }

    @Test
    void testGetUser_NotFound() {
        String username = "nonExistentUser";

        when(userService.getUser(username)).thenReturn(null);

        ResponseEntity<?> response = userController.getUser(username);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Could not get user", response.getBody());
    }

    @Test
    void testRegisterUser_Success() throws Exception {
        UserDto userDto = new UserDto();
        userDto.setUsername("testUser");
        userDto.setPassword("testPassword");

        // Simulate successful registration
        doNothing().when(userService).registerUser(userDto);

        ResponseEntity<?> responseEntity = userController.registerUser(userDto);

        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        assertEquals("User registered successfully", responseEntity.getBody());
    }

    @Test
    void testCreateUser_Success() {
        UserCreateDto userCreateDto = new UserCreateDto();
        userCreateDto.setUsername("newUser");
        userCreateDto.setEmail("newUser@example.com");

        when(userService.createUser(userCreateDto)).thenReturn(true);

        ResponseEntity<String> response = userController.createUser(userCreateDto);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("User created successfully", response.getBody());
    }

    @Test
    void testCreateUser_Failure() {
        UserCreateDto userCreateDto = new UserCreateDto();
        userCreateDto.setUsername("newUser");
        userCreateDto.setEmail("newUser@example.com");

        when(userService.createUser(userCreateDto)).thenReturn(false);

        ResponseEntity<String> response = userController.createUser(userCreateDto);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Could not create user", response.getBody());
    }

    @Test
    void testUpdateUser_Success() {
        UserUpdateDto userUpdateDto = new UserUpdateDto();
        userUpdateDto.setCurrentUsername("oldUser");
        userUpdateDto.setNewUsername("newUser");
        userUpdateDto.setNewEmail("newUser@example.com");
        userUpdateDto.setNewRole("USER");

        when(userService.updateUser("oldUser", "newUser", "newUser@example.com", "USER")).thenReturn(true);

        ResponseEntity<String> response = userController.updateUser(userUpdateDto);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("User updated successfully", response.getBody());
    }

    @Test
    void testUpdateUser_Failure() {
        UserUpdateDto userUpdateDto = new UserUpdateDto();
        userUpdateDto.setCurrentUsername("oldUser");
        userUpdateDto.setNewUsername("newUser");
        userUpdateDto.setNewEmail("newUser@example.com");
        userUpdateDto.setNewRole("USER");

        when(userService.updateUser("oldUser", "newUser", "newUser@example.com", "USER")).thenReturn(false);

        ResponseEntity<String> response = userController.updateUser(userUpdateDto);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Could not update user", response.getBody());
    }

    @Test
    void testDeleteUser_Success() {
        String username = "testUser";

        when(userService.deleteUserByUsername(username)).thenReturn(true);

        ResponseEntity<String> response = userController.deleteUserByUsername(username);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("User deleted successfully", response.getBody());
    }

    @Test
    void testDeleteUser_NotFound() {
        String username = "nonExistentUser";

        when(userService.deleteUserByUsername(username)).thenReturn(false);

        ResponseEntity<String> response = userController.deleteUserByUsername(username);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertEquals("User not found", response.getBody());
    }

    @Test
    void testVerifyEmail_Success() {
        String token = "validToken";

        when(userService.verifyUserEmail(token)).thenReturn(true);

        ResponseEntity<String> response = userController.verifyEmail(token);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Email verified successfully.", response.getBody());
    }

    @Test
    void testVerifyEmail_Failure() {
        String token = "invalidToken";

        when(userService.verifyUserEmail(token)).thenReturn(false);

        ResponseEntity<String> response = userController.verifyEmail(token);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Invalid or expired verification token.", response.getBody());
    }

    @Test
    void testResendVerificationEmail_Success() {
        UsernameDto usernameDto = new UsernameDto();
        usernameDto.setUsername("testUser");

        when(userService.resendVerificationEmail(usernameDto.getUsername())).thenReturn(true);

        ResponseEntity<String> response = userController.resendVerificationEmail(usernameDto);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Verification email resent.", response.getBody());
    }

    @Test
    void testResendVerificationEmail_NotFound() {
        UsernameDto usernameDto = new UsernameDto();
        usernameDto.setUsername("nonExistentUser");

        when(userService.resendVerificationEmail(usernameDto.getUsername())).thenReturn(false);

        ResponseEntity<String> response = userController.resendVerificationEmail(usernameDto);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertEquals("User not found.", response.getBody());
    }

    @Test
    void testForgotPassword_Success() {
        EmailDto emailDto = new EmailDto();
        emailDto.setEmail("user@example.com");

        when(userService.processForgotPassword(emailDto.getEmail())).thenReturn(true);

        ResponseEntity<String> response = userController.forgotPassword(emailDto);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Password reset link sent to your email.", response.getBody());
    }

    @Test
    void testForgotPassword_NotFound() {
        EmailDto emailDto = new EmailDto();
        emailDto.setEmail("nonExistentUser@example.com");

        when(userService.processForgotPassword(emailDto.getEmail())).thenReturn(false);

        ResponseEntity<String> response = userController.forgotPassword(emailDto);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertEquals("User not found", response.getBody());
    }

    @Test
    void testResetPassword_Success() {
        String token = "validToken";
        PasswordResetDto resetDto = new PasswordResetDto();
        resetDto.setNewPassword("newPassword123");
        resetDto.setToken(token);

        when(userService.resetPassword(token, resetDto.getNewPassword())).thenReturn(true);

        ResponseEntity<String> response = userController.resetPassword(resetDto);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Password reset successful.", response.getBody());
    }

    @Test
    void testResetPassword_Failure() {
        String token = "invalidToken";
        PasswordResetDto resetDto = new PasswordResetDto();
        resetDto.setNewPassword("newPassword123");
        resetDto.setToken(token);

        when(userService.resetPassword(token, resetDto.getNewPassword())).thenReturn(false);

        ResponseEntity<String> response = userController.resetPassword(resetDto);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Invalid or expired token.", response.getBody());
    }

    @Test
    void testForgotUsername_Success() {
        EmailDto emailDto = new EmailDto();
        emailDto.setEmail("user@example.com");

        when(userService.processForgotUsername(emailDto.getEmail())).thenReturn(true);

        ResponseEntity<String> response = userController.forgotUsername(emailDto);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Username sent to your email.", response.getBody());
    }

    @Test
    void testForgotUsername_NotFound() {
        EmailDto emailDto = new EmailDto();
        emailDto.setEmail("nonExistentUser@example.com");

        when(userService.processForgotUsername(emailDto.getEmail())).thenReturn(false);

        ResponseEntity<String> response = userController.forgotUsername(emailDto);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertEquals("Email not found", response.getBody());
    }

    @Test
    void testGetUser2_Success() {
        // Arrange
        UserGetResponse userResponse = new UserGetResponse(); // Initialize with required fields
        when(userService.getUserByToken(request)).thenReturn(userResponse);

        // Act
        ResponseEntity<?> response = userController.getUser(request);

        // Assert
        assertEquals(200, response.getStatusCodeValue());
        assertEquals(userResponse, response.getBody());
    }

    @Test
    void testGetUser2_NotFound2() {
        // Arrange
        when(userService.getUserByToken(request)).thenReturn(null);

        // Act
        ResponseEntity<?> response = userController.getUser(request);

        // Assert
        assertEquals(400, response.getStatusCodeValue());
        assertEquals("Could not get user", response.getBody());
    }
}
