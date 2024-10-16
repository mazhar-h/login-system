package com.login.loginsystem.controller;

import com.login.loginsystem.dto.*;
import com.login.loginsystem.exception.UserExistsException;
import com.login.loginsystem.service.UserService;
import com.login.loginsystem.util.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.management.relation.RoleNotFoundException;
import java.util.List;

@RestController
@RequestMapping("/api/v1")
public class UserController {

    private final UserService userService;
    private final JwtUtil jwtUtil;

    public UserController(UserService userService, JwtUtil jwtUtil) {
        this.userService = userService;
        this.jwtUtil = jwtUtil;
    }

    @GetMapping("/hello")
    public String helloWorld(HttpServletRequest request) {
        return "Hello " + userService.getUsername(request);
    }

    @GetMapping("/admin/hello")
    public String helloWorld2(HttpServletRequest request) {
        return "Hello admin " + userService.getUsername(request);
    }

    @GetMapping("/admin/users")
    public ResponseEntity<List<UsernameEmailDto>> getAllUsers() {
        List<UsernameEmailDto> users = userService.getAllUsers();
        return ResponseEntity.ok(users);
    }

    @GetMapping("/users/{username}")
    public ResponseEntity<?> getUser(@PathVariable String username) {
        UserGetResponse user = userService.getUser(username);
        if (user != null) {
            return ResponseEntity.ok(user);
        } else {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Could not get user");
        }
    }

    @GetMapping("/users")
    public ResponseEntity<?> getUser(HttpServletRequest request) {
        UserGetResponse user = userService.getUserByToken(request);
        if (user != null) {
            return ResponseEntity.ok(user);
        } else {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Could not get user");
        }
    }

    @GetMapping("/users/email")
    public ResponseEntity<?> getUserEmail(HttpServletRequest request) {
        UsernameEmailDto user = userService.getEmailByToken(request);
        if (user != null) {
            return ResponseEntity.ok(user);
        } else {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Could not get user");
        }
    }

    @PostMapping("/users/register")
    public ResponseEntity<?> registerUser(@Valid @RequestBody UserDto user) {
        try {
            userService.registerUser(user);
        } catch (UserExistsException e) {
            return ResponseEntity.ok("Registered");
        } catch (RoleNotFoundException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }

        return ResponseEntity.ok("User registered successfully");
    }

    @PutMapping("/users/password")
    public ResponseEntity<?> updatePassword(@RequestBody NewPasswordDto newPasswordDto, HttpServletRequest request) {
        boolean isUpdated = userService.updatePassword(newPasswordDto.getCurrentPassword(),
                newPasswordDto.getNewPassword(), request);
        if (isUpdated) {
            return ResponseEntity.ok("Password updated successfully");
        } else {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Could not get user");
        }
    }

    @PostMapping("/admin/users")
    public ResponseEntity<String> createUser(@Valid @RequestBody UserCreateDto userCreateDto) {
        boolean isCreated = userService.createUser(userCreateDto);
        if (isCreated) {
            return ResponseEntity.ok("User created successfully");
        } else {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Could not create user");
        }
    }

    @PutMapping("/admin/users")
    public ResponseEntity<String> updateUser(@RequestBody UserUpdateDto request) {
        boolean isUpdated = userService.updateUser(request.getCurrentUsername(), request.getNewUsername(),
                request.getNewEmail(), request.getNewRole());
        if (isUpdated) {
            return ResponseEntity.ok("User updated successfully");
        } else {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Could not update user");
        }
    }

    @DeleteMapping("/admin/users/{username}")
    public ResponseEntity<String> deleteUserByUsername(@PathVariable String username) {
        boolean isDeleted = userService.deleteUserByUsername(username);
        if (isDeleted) {
            return ResponseEntity.ok("User deleted successfully");
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found");
        }
    }

    @DeleteMapping("/users")
    public ResponseEntity<String> deleteUser(HttpServletRequest request) {
        boolean isDeleted = userService.deleteUser(request);
        if (isDeleted) {
            return ResponseEntity.ok("User deleted successfully");
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found");
        }
    }

    @GetMapping("/users/verify-email")
    public ResponseEntity<String> verifyEmail(@RequestParam String token) {
        boolean isVerified = userService.verifyUserEmail(token);
        if (isVerified) {
            return ResponseEntity.ok("Email verified successfully.");
        } else {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid or expired verification token.");
        }
    }

    @PostMapping("/users/resend-verification")
    public ResponseEntity<String> resendVerificationEmail(@RequestBody UsernameDto request) {
        boolean isSent = userService.resendVerificationEmail(request.getUsername());
        if (isSent) {
            return ResponseEntity.ok("Verification email resent.");
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found.");
        }
    }

    @PostMapping("/users/forgot-password")
    public ResponseEntity<String> forgotPassword(@RequestBody EmailDto request) {
        boolean isEmailSent = userService.processForgotPassword(request.getEmail());
        if (isEmailSent) {
            return ResponseEntity.ok("Password reset link sent to your email.");
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found");
        }
    }

    @PostMapping("/users/reset-password")
    public ResponseEntity<String> resetPassword(@RequestBody PasswordResetDto request) {
        boolean isReset = userService.resetPassword(request.getToken(), request.getNewPassword());
        if (isReset) {
            return ResponseEntity.ok("Password reset successful.");
        } else {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid or expired token.");
        }
    }

    @PostMapping("/users/forgot-username")
    public ResponseEntity<String> forgotUsername(@RequestBody EmailDto request) {
        boolean isEmailSent = userService.processForgotUsername(request.getEmail());
        if (isEmailSent) {
            return ResponseEntity.ok("Username sent to your email.");
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Email not found");
        }
    }

    @PutMapping("/users/update-email")
    public ResponseEntity<?> requestEmailUpdate(@RequestBody EmailDto request, HttpServletRequest httpRequest) {
        String newEmail = request.getEmail();
        String token = jwtUtil.extractJwtFromRequest(httpRequest);
        String username = jwtUtil.extractUsername(token);

        boolean isRequestInitiated = userService.initiateEmailUpdate(username, newEmail);

        if (isRequestInitiated) {
            return ResponseEntity.ok("Confirmation email sent to the new address.");
        } else {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Email update failed.");
        }
    }

    @PostMapping("/users/confirm-email")
    public ResponseEntity<String> confirmEmail(@RequestBody TokenDto request) {
        String token = request.getToken();
        boolean isConfirmed = userService.confirmEmailUpdate(token);

        if (isConfirmed) {
            return ResponseEntity.ok("Email updated successfully.");
        } else {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid or expired token.");
        }
    }
}
