package com.login.loginsystem.controller;

import com.login.loginsystem.util.JwtUtil;
import com.login.loginsystem.dto.AuthRequest;
import com.login.loginsystem.dto.AuthResponse;
import com.login.loginsystem.service.UserService;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.security.SignatureException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1")
public class AuthController {

    private AuthenticationManager authenticationManager;
    private JwtUtil jwtUtil;
    private UserDetailsService userDetailsService;
    private UserService userService;
    private static final int REFRESH_TOKEN_VALIDITY = 7 * 24 * 60 * 60; // 7 days


    public AuthController(AuthenticationManager authenticationManager, JwtUtil jwtUtil, UserDetailsService userDetailsService,
                          UserService userService) {
        this. authenticationManager = authenticationManager;
        this.jwtUtil = jwtUtil;
        this.userDetailsService = userDetailsService;
        this.userService = userService;
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody AuthRequest authRequest, HttpServletResponse response) throws Exception {
        try {
            final UserDetails userDetails = userDetailsService.loadUserByUsername(authRequest.getUsername());

            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(authRequest.getUsername(), authRequest.getPassword())
            );

            final String accessToken = jwtUtil.generateAccessToken(userDetails);
            final String refreshToken = jwtUtil.generateRefreshToken(userDetails);

            Cookie refreshTokenCookie = new Cookie("refreshToken", refreshToken);
            refreshTokenCookie.setHttpOnly(true);
            refreshTokenCookie.setSecure(true);
            refreshTokenCookie.setPath("/");
            refreshTokenCookie.setMaxAge(REFRESH_TOKEN_VALIDITY);
            response.addCookie(refreshTokenCookie);

            return ResponseEntity.ok(new AuthResponse(accessToken));
        } catch (DisabledException e) {
            boolean isValid = userService.isUserCredentialsValid(authRequest.getUsername(), authRequest.getPassword());
            if (!isValid)
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid credentials");
            else
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body("User is not verified");
        } catch (BadCredentialsException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid credentials");
        }
    }

    @PostMapping("/refresh-token")
    public ResponseEntity<?> refreshToken(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        String refreshToken = null;

        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if (cookie.getName().equals("refreshToken")) {
                    refreshToken = cookie.getValue();
                }
            }
        }

        try {
            if (refreshToken == null || jwtUtil.isTokenExpired(refreshToken))
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Refresh token invalid or missing");
        } catch (MalformedJwtException | SignatureException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Refresh token invalid or missing");
        }

        String username = jwtUtil.extractUsername(refreshToken);
        UserDetails userDetails = userDetailsService.loadUserByUsername(username);

        String newAccessToken = jwtUtil.generateAccessToken(userDetails);

        return ResponseEntity.ok(new AuthResponse(newAccessToken));
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(HttpServletResponse response) {
        Cookie cookie = new Cookie("refreshToken", null);
        cookie.setHttpOnly(true);
        cookie.setSecure(true);
        cookie.setPath("/");
        cookie.setMaxAge(0);
        response.addCookie(cookie);

        return ResponseEntity.ok().build();
    }
}