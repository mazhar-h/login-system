package com.login.loginsystem.util;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class JwtUtilTest {

    private JwtUtil jwtUtil;
    private UserDetails userDetails;

    @BeforeEach
    void setUp() {
        jwtUtil = new JwtUtil();

        // Mock UserDetails
        userDetails = Mockito.mock(UserDetails.class);
    }

    @Test
    void testGenerateAccessToken() {
        String token = jwtUtil.generateAccessToken(userDetails);
        assertNotNull(token);
        assertTrue(token.startsWith("ey")); // JWT tokens start with "ey"
    }

    @Test
    void testGenerateRefreshToken() {
        String token = jwtUtil.generateRefreshToken(userDetails);
        assertNotNull(token);
        assertTrue(token.startsWith("ey")); // JWT tokens start with "ey"
    }

    @Test
    void testExtractUsername() {
        when(userDetails.getUsername()).thenReturn("testUser");
        String token = jwtUtil.generateAccessToken(userDetails);
        String username = jwtUtil.extractUsername(token);
        assertEquals("testUser", username);
    }

    @Test
    void testExtractJwtFromRequest_WithBearerToken() {
        HttpServletRequest request = Mockito.mock(HttpServletRequest.class);
        when(request.getHeader("Authorization")).thenReturn("Bearer some.jwt.token");

        String token = jwtUtil.extractJwtFromRequest(request);
        assertEquals("some.jwt.token", token);
    }

    @Test
    void testExtractJwtFromRequest_WithCookie() {
        HttpServletRequest request = Mockito.mock(HttpServletRequest.class);
        when(request.getHeader("Authorization")).thenReturn(null);
        Cookie[] cookies = { new Cookie("refreshToken", "refresh.jwt.token") };
        when(request.getCookies()).thenReturn(cookies);

        String token = jwtUtil.extractJwtFromRequest(request);
        assertEquals("refresh.jwt.token", token);
    }

    @Test
    void testExtractJwtFromRequest_NeitherHeaderNorCookie() {
        HttpServletRequest request = Mockito.mock(HttpServletRequest.class);
        when(request.getHeader("Authorization")).thenReturn(null);
        when(request.getCookies()).thenReturn(null);

        String token = jwtUtil.extractJwtFromRequest(request);
        assertNull(token);
    }

    @Test
    void testIsTokenExpired() {
        String token = jwtUtil.generateAccessToken(userDetails);
        assertFalse(jwtUtil.isTokenExpired(token));
    }

    @Test
    void testExtractExpiration() {
        String token = jwtUtil.generateAccessToken(userDetails);
        Date expirationDate = jwtUtil.extractExpiration(token);
        assertNotNull(expirationDate);
        assertTrue(expirationDate.after(new Date()));
    }

    @Test
    void testValidateToken_ValidToken() {
        when(userDetails.getUsername()).thenReturn("testUser");
        String token = jwtUtil.generateAccessToken(userDetails);
        boolean isValid = jwtUtil.validateToken(token, userDetails);
        assertTrue(isValid);
    }

    @Test
    void testValidateToken_InvalidToken() {
        when(userDetails.getUsername()).thenReturn("testUser");
        String token = jwtUtil.generateAccessToken(userDetails);
        UserDetails differentUserDetails = Mockito.mock(UserDetails.class);
        when(differentUserDetails.getUsername()).thenReturn("differentUser");

        boolean isValid = jwtUtil.validateToken(token, differentUserDetails);
        assertFalse(isValid);
    }
}