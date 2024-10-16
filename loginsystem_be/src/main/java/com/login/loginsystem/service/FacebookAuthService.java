package com.login.loginsystem.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.login.loginsystem.dto.AuthResponse;
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
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;

@Service
public class FacebookAuthService {

    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;
    private final UserDetailsService userDetailsService;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final RestTemplate restTemplate;
    private static final int REFRESH_TOKEN_VALIDITY = 7 * 24 * 60 * 60; // 7 days
    private final String FACEBOOK_TOKEN_VERIFY_URL = "https://graph.facebook.com/me?access_token=%s&fields=email";

    public FacebookAuthService(UserRepository userRepository, JwtUtil jwtUtil, UserDetailsService userDetailsService,
                               RoleRepository roleRepository, PasswordEncoder passwordEncoder, RestTemplate restTemplate) {
        this.userRepository = userRepository;
        this.jwtUtil = jwtUtil;
        this.userDetailsService = userDetailsService;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
        this.restTemplate = restTemplate;
    }

    public AuthResponse verifyToken(String idTokenString,
                                          HttpServletResponse response) throws JsonProcessingException {
        String url = String.format(FACEBOOK_TOKEN_VERIFY_URL, idTokenString);
        ResponseEntity<String> facebookResponse = restTemplate.getForEntity(url, String.class);
        ObjectMapper mapper = new ObjectMapper();
        JsonNode rootNode = mapper.readTree(facebookResponse.getBody());
        String email = rootNode.get("email").asText();

        User user = userRepository.findByEmail(email);

        if (user == null) {
            return new AuthResponse(null);
        } else if (user.getFacebookId() == null){
            throw new UserExistsException("User already exists");
        }

        UserDetails userDetails = userDetailsService.loadUserByUsername(user.getUsername());
        String jwtToken = jwtUtil.generateAccessToken(userDetails);
        String refreshToken = jwtUtil.generateRefreshToken(userDetails);
        createRefreshCookie(response, refreshToken);

        return new AuthResponse(jwtToken);
    }

    public AuthResponse register(String username, HttpServletRequest request,
                                 HttpServletResponse response) throws JsonProcessingException {
        final String authorizationHeader = request.getHeader("Authorization");
        String idTokenString = null;

        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer "))
            idTokenString = authorizationHeader.substring(7);

        String url = String.format(FACEBOOK_TOKEN_VERIFY_URL, idTokenString);
        ResponseEntity<String> facebookResponse = restTemplate.getForEntity(url, String.class);
        ObjectMapper mapper = new ObjectMapper();
        JsonNode rootNode = mapper.readTree(facebookResponse.getBody());
        String email = rootNode.get("email").asText();
        String userId = rootNode.get("id").asText();

        if (userRepository.existsByUsername(username)) {
            throw new UsernameExistsException("Username already exists");
        }

        User user = new User();
        user.setUsername(username);
        user.setEmail(email);
        user.setFacebookId(userId);
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

    public UsernameDto getUser(HttpServletRequest request) throws JsonProcessingException {
        final String authorizationHeader = request.getHeader("Authorization");
        String idTokenString = null;

        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer "))
            idTokenString = authorizationHeader.substring(7);

        String url = String.format(FACEBOOK_TOKEN_VERIFY_URL, idTokenString);
        ResponseEntity<String> facebookResponse = restTemplate.getForEntity(url, String.class);
        ObjectMapper mapper = new ObjectMapper();
        JsonNode rootNode = mapper.readTree(facebookResponse.getBody());
        String email = rootNode.get("email").asText();

        User user = userRepository.findByEmail(email);

        return new UsernameDto(user.getUsername());
    }

    public AuthResponse linkAccount(String password, HttpServletRequest request,
                                    HttpServletResponse response) throws JsonProcessingException, InvalidCredentialsException {
        final String authorizationHeader = request.getHeader("Authorization");
        String idTokenString = null;

        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer "))
            idTokenString = authorizationHeader.substring(7);

        String url = String.format(FACEBOOK_TOKEN_VERIFY_URL, idTokenString);
        ResponseEntity<String> facebookResponse = restTemplate.getForEntity(url, String.class);
        ObjectMapper mapper = new ObjectMapper();
        JsonNode rootNode = mapper.readTree(facebookResponse.getBody());
        String email = rootNode.get("email").asText();
        String userId = rootNode.get("id").asText();

        User user = userRepository.findByEmail(email);

        if (user.getFacebookId() != null && user.getFacebookId().equals(userId))
            throw new AccountLinkedException("Account already linked!");

        if (!passwordEncoder.matches(password, user.getPassword()))
            throw new InvalidCredentialsException("Invalid credentials");

        user.setFacebookId(userId);
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
