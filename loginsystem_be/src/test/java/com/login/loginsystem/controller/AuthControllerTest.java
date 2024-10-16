package com.login.loginsystem.controller;

import com.login.loginsystem.dto.AuthRequest;
import com.login.loginsystem.dto.AuthResponse;
import com.login.loginsystem.dto.UserDto;
import com.login.loginsystem.service.UserService;
import com.login.loginsystem.util.JwtUtil;
import io.jsonwebtoken.MalformedJwtException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.*;

class AuthControllerTest {

    private AuthController authController;
    private AuthenticationManager authenticationManager;
    private JwtUtil jwtUtil;
    private UserDetailsService userDetailsService;
    private UserService userService;
    private HttpServletRequest request;
    private HttpServletResponse response;

    @BeforeEach
    void setUp() {
        authenticationManager = mock(AuthenticationManager.class);
        jwtUtil = mock(JwtUtil.class);
        userDetailsService = mock(UserDetailsService.class);
        userService = mock(UserService.class);
        authController = new AuthController(authenticationManager, jwtUtil, userDetailsService, userService);

        request = mock(HttpServletRequest.class);
        response = mock(HttpServletResponse.class);
    }

    @Test
    void testLogin_Success() throws Exception {
        AuthRequest authRequest = new AuthRequest("testUser", "testPassword");
        UserDetails userDetails = mock(UserDetails.class);
        String accessToken = "accessToken";
        String refreshToken = "refreshToken";

        when(userDetailsService.loadUserByUsername(authRequest.getUsername())).thenReturn(userDetails);
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class))).thenReturn(null);
        when(jwtUtil.generateAccessToken(userDetails)).thenReturn(accessToken);
        when(jwtUtil.generateRefreshToken(userDetails)).thenReturn(refreshToken);

        ResponseEntity<?> responseEntity = authController.login(authRequest, response);

        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        assertNotNull(responseEntity.getBody());
        assertEquals(accessToken, ((AuthResponse) responseEntity.getBody()).getAccessToken());

        // Verify cookie is added
        verify(response).addCookie(argThat(cookie -> {
            return "refreshToken".equals(cookie.getName())
                    && refreshToken.equals(cookie.getValue())
                    && cookie.getMaxAge() == 7 * 24 * 60 * 60 // 7 days
                    && "/".equals(cookie.getPath())
                    && cookie.isHttpOnly()
                    && cookie.getSecure();
        }));
    }

    @Test
    void testLogin_UserNotVerified() throws Exception {
        AuthRequest authRequest = new AuthRequest("testUser", "testPassword");

        when(userService.isUserCredentialsValid("testUser", "testPassword")).thenReturn(true);
        when(userDetailsService.loadUserByUsername(authRequest.getUsername())).thenThrow(new DisabledException("User is not verified"));

        ResponseEntity<?> responseEntity = authController.login(authRequest, response);

        assertEquals(HttpStatus.FORBIDDEN, responseEntity.getStatusCode());
        assertEquals("User is not verified", responseEntity.getBody());
    }

    @Test
    void testLogin_InvalidCredentials() throws Exception {
        AuthRequest authRequest = new AuthRequest("testUser", "wrongPassword");

        when(userDetailsService.loadUserByUsername(authRequest.getUsername())).thenReturn(mock(UserDetails.class));
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new BadCredentialsException("Invalid credentials"));

        ResponseEntity<?> responseEntity = authController.login(authRequest, response);

        assertEquals(HttpStatus.UNAUTHORIZED, responseEntity.getStatusCode());
        assertEquals("Invalid credentials", responseEntity.getBody());
    }

    @Test
    void testRefreshToken_Success() throws Exception {
        String refreshToken = "valid-refresh-token";
        Cookie refreshTokenCookie = new Cookie("refreshToken", refreshToken);
        when(request.getCookies()).thenReturn(new Cookie[]{refreshTokenCookie});
        when(jwtUtil.isTokenExpired(refreshToken)).thenReturn(false);
        when(jwtUtil.extractUsername(refreshToken)).thenReturn("testUser");

        UserDetails userDetails = mock(UserDetails.class);
        when(userDetailsService.loadUserByUsername("testUser")).thenReturn(userDetails);
        String newAccessToken = "newAccessToken";
        when(jwtUtil.generateAccessToken(userDetails)).thenReturn(newAccessToken);

        ResponseEntity<?> responseEntity = authController.refreshToken(request);

        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        assertEquals(newAccessToken, ((AuthResponse) responseEntity.getBody()).getAccessToken());
    }

    @Test
    void testRefreshToken_TokenExpired() throws Exception {
        String refreshToken = "expired-refresh-token";
        Cookie refreshTokenCookie = new Cookie("refreshToken", refreshToken);
        when(request.getCookies()).thenReturn(new Cookie[]{refreshTokenCookie});
        when(jwtUtil.isTokenExpired(refreshToken)).thenReturn(true);

        ResponseEntity<?> responseEntity = authController.refreshToken(request);

        assertEquals(HttpStatus.UNAUTHORIZED, responseEntity.getStatusCode());
        assertEquals("Refresh token invalid or missing", responseEntity.getBody());
    }

    @Test
    void testRefreshToken_MalformedToken() throws Exception {
        String refreshToken = "malformed-refresh-token";
        Cookie refreshTokenCookie = new Cookie("refreshToken", refreshToken);
        when(request.getCookies()).thenReturn(new Cookie[]{refreshTokenCookie});
        when(jwtUtil.isTokenExpired(refreshToken)).thenThrow(new MalformedJwtException("Malformed token"));

        ResponseEntity<?> responseEntity = authController.refreshToken(request);

        assertEquals(HttpStatus.UNAUTHORIZED, responseEntity.getStatusCode());
        assertEquals("Refresh token invalid or missing", responseEntity.getBody());
    }

    @Test
    void testLogout_Success() {
        Cookie cookie = new Cookie("refreshToken", "someToken");

        // Simulate adding a cookie
        doNothing().when(response).addCookie(any(Cookie.class));

        ResponseEntity<Void> responseEntity = authController.logout(response);

        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());

        // Verify that a cookie is added to clear the refresh token
        verify(response).addCookie(argThat(c -> {
            return "refreshToken".equals(c.getName()) && c.getValue() == null && c.getMaxAge() == 0;
        }));
    }
}
