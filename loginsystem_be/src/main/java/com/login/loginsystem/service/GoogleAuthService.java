package com.login.loginsystem.service;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.login.loginsystem.dto.AuthResponse;
import com.login.loginsystem.dto.GoogleAuthResponse;
import com.login.loginsystem.dto.UsernameDto;
import com.login.loginsystem.exception.AccountLinkedException;
import com.login.loginsystem.exception.UserExistsException;
import com.login.loginsystem.exception.UsernameExistsException;
import com.login.loginsystem.model.Role;
import com.login.loginsystem.model.User;
import com.login.loginsystem.repository.RoleRepository;
import com.login.loginsystem.repository.UserRepository;
import com.login.loginsystem.util.JwtUtil;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.http.auth.InvalidCredentialsException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Collections;

@Service
public class GoogleAuthService {
    private final GoogleIdTokenVerifier verifier;
    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;
    private final UserDetailsService userDetailsService;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private static final int REFRESH_TOKEN_VALIDITY = 7 * 24 * 60 * 60; // 7 days

    public GoogleAuthService(UserRepository userRepository, JwtUtil jwtUtil,
                             UserDetailsService userDetailsService,
                             RoleRepository roleRepository, PasswordEncoder passwordEncoder,
                             @Value("${google.oauth.client-id}") String googleOauthClientId) {
        verifier = new GoogleIdTokenVerifier.Builder(new NetHttpTransport(), new GsonFactory())
                .setAudience(Collections.singletonList(googleOauthClientId))
                .build();
        this. userRepository = userRepository;
        this.jwtUtil = jwtUtil;
        this.userDetailsService = userDetailsService;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public GoogleAuthResponse verifyToken(String idTokenString,
                                          HttpServletResponse response) throws GeneralSecurityException, IOException {
        GoogleIdToken idToken = verifier.verify(idTokenString);

        if (idToken == null)
            throw new IllegalArgumentException("Invalid ID token.");

        GoogleIdToken.Payload payload = idToken.getPayload();
        String email = payload.getEmail();

        User user = userRepository.findByEmail(email);

        if (user == null) {
            return new GoogleAuthResponse(null);
        } else if (user.getGoogleId() == null){
            throw new UserExistsException("User already exists");
        }

        UserDetails userDetails = userDetailsService.loadUserByUsername(user.getUsername());
        String jwtToken = jwtUtil.generateAccessToken(userDetails);
        String refreshToken = jwtUtil.generateRefreshToken(userDetails);
        createRefreshCookie(response, refreshToken);

        return new GoogleAuthResponse(jwtToken);
    }

    public AuthResponse register(String username, HttpServletRequest request,
                                 HttpServletResponse response) throws GeneralSecurityException, IOException {
        final String authorizationHeader = request.getHeader("Authorization");
        String idTokenString = null;

        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer "))
            idTokenString = authorizationHeader.substring(7);

        GoogleIdToken idToken = verifier.verify(idTokenString);

        if (idToken == null)
            throw new IllegalArgumentException("Invalid ID token.");

        GoogleIdToken.Payload payload = idToken.getPayload();

        String userId = payload.getSubject();
        String email = payload.getEmail();

        if (userRepository.existsByUsername(username)) {
            throw new UsernameExistsException("Username already exists");
        }

        User user = new User();
        user.setUsername(username);
        user.setEmail(email);
        user.setGoogleId(userId);
        user.setEnabled(true);
        Role userRole = roleRepository.findByName("ROLE_USER");

        if (userRole == null)
            throw new RuntimeException("Role not found");

        user.setRoles(Collections.singleton(userRole));
        userRepository.save(user);
        UserDetails userDetails = userDetailsService.loadUserByUsername(user.getUsername());
        String jwtToken = jwtUtil.generateAccessToken(userDetails);
        String refreshToken = jwtUtil.generateRefreshToken(userDetails);
        createRefreshCookie(response, refreshToken);

        return new AuthResponse(jwtToken);
    }

    public UsernameDto getUser(HttpServletRequest request) throws GeneralSecurityException, IOException {
        final String authorizationHeader = request.getHeader("Authorization");
        String idTokenString = null;

        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer "))
            idTokenString = authorizationHeader.substring(7);

        GoogleIdToken idToken = verifier.verify(idTokenString);

        if (idToken == null)
            throw new IllegalArgumentException("Invalid ID token.");

        GoogleIdToken.Payload payload = idToken.getPayload();

        String email = payload.getEmail();

        User user = userRepository.findByEmail(email);

        return new UsernameDto(user.getUsername());
    }

    public AuthResponse linkAccount(String password, HttpServletRequest request,
                                    HttpServletResponse response) throws GeneralSecurityException, IOException, InvalidCredentialsException {
        final String authorizationHeader = request.getHeader("Authorization");
        String idTokenString = null;

        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer "))
            idTokenString = authorizationHeader.substring(7);

        GoogleIdToken idToken = verifier.verify(idTokenString);

        if (idToken == null)
            throw new IllegalArgumentException("Invalid ID token.");

        GoogleIdToken.Payload payload = idToken.getPayload();

        String userId = payload.getSubject();
        String email = payload.getEmail();

        User user = userRepository.findByEmail(email);

        if (user.getGoogleId() != null && user.getGoogleId().equals(userId))
            throw new AccountLinkedException("Account already linked!");

        if (!passwordEncoder.matches(password, user.getPassword()))
            throw new InvalidCredentialsException("Invalid credentials");

        user.setGoogleId(userId);
        userRepository.save(user);
        UserDetails userDetails = userDetailsService.loadUserByUsername(user.getUsername());
        String jwtToken = jwtUtil.generateAccessToken(userDetails);
        String refreshToken = jwtUtil.generateRefreshToken(userDetails);
        createRefreshCookie(response, refreshToken);
        return new AuthResponse(jwtToken);
    }

    private void createRefreshCookie(HttpServletResponse response, String refreshToken) {
        Cookie refreshTokenCookie = new Cookie("refreshToken", refreshToken);
        refreshTokenCookie.setHttpOnly(true);
        refreshTokenCookie.setSecure(true);
        refreshTokenCookie.setPath("/");
        refreshTokenCookie.setMaxAge(REFRESH_TOKEN_VALIDITY);
        response.addCookie(refreshTokenCookie);
    }
}
