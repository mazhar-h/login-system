package com.login.loginsystem.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.login.loginsystem.dto.AuthResponse;
import com.login.loginsystem.dto.GoogleLinkRequest;
import com.login.loginsystem.dto.SocialAuthRequest;
import com.login.loginsystem.exception.AccountLinkedException;
import com.login.loginsystem.exception.UserExistsException;
import com.login.loginsystem.exception.UsernameExistsException;
import com.login.loginsystem.service.FacebookAuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.apache.http.auth.InvalidCredentialsException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
public class FacebookAuthController {

    private final FacebookAuthService facebookAuthService;

    public FacebookAuthController(FacebookAuthService facebookAuthService) {
        this.facebookAuthService = facebookAuthService;
    }

    @PostMapping("/facebook")
    public ResponseEntity<?> facebookLogin(@RequestBody String idTokenString, HttpServletResponse response) {
        try {
            AuthResponse facebookAuthResponse = facebookAuthService.verifyToken(idTokenString, response);
            return ResponseEntity.ok(facebookAuthResponse);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid token");
        } catch (UserExistsException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body("User already exists");
        } catch (JsonProcessingException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PostMapping("/facebook/register")
    public ResponseEntity<?> facebookRegister(@Valid @RequestBody SocialAuthRequest socialAuthRequest,
                                            HttpServletRequest request,
                                            HttpServletResponse response) {
        try {
            AuthResponse authResponse =  facebookAuthService.register(socialAuthRequest.getUsername(),
                    request, response);
            return ResponseEntity.ok(authResponse);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid token");
        } catch (UsernameExistsException | JsonProcessingException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/facebook/user")
    public ResponseEntity<?> facebookGetUser(HttpServletRequest request) {
        try {
            return ResponseEntity.ok(facebookAuthService.getUser(request));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid token");
        } catch (JsonProcessingException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PostMapping("/facebook/link")
    public ResponseEntity<?> facebookLinkUser(@RequestBody GoogleLinkRequest googleLinkRequest,
                                            HttpServletRequest request,
                                            HttpServletResponse response) {
        try {
            return ResponseEntity.ok(facebookAuthService.linkAccount(googleLinkRequest.getPassword(),
                    request, response));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid token");
        } catch (InvalidCredentialsException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid credentials");
        } catch (AccountLinkedException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Account already linked");
        } catch (JsonProcessingException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
