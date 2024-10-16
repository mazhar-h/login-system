package com.login.loginsystem.service;

import com.login.loginsystem.exception.UserExistsException;
import com.login.loginsystem.model.*;
import com.login.loginsystem.repository.*;
import com.login.loginsystem.util.JwtUtil;
import com.login.loginsystem.dto.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import javax.management.relation.RoleNotFoundException;
import javax.swing.text.html.Option;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final RoleRepository roleRepository;
    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final EmailService emailService;
    private final VerificationTokenRepository verificationTokenRepository;
    private final EmailTokenRepository emailTokenRepository;

    @Value("${loginsystem.frontend.domain}")
    private String DOMAIN;
    private static final String PATH_RESET_PASSWORD = "/reset-password/";
    private static final String PATH_VERIFY_EMAIL = "/verify-email/";

    public void registerUser(UserDto userDto) throws RoleNotFoundException {
        User user = new User();
        if (userRepository.existsByUsername(userDto.getUsername())) {
            throw new UserExistsException("Username already exists");
        }
        user.setUsername(userDto.getUsername());
        user.setEmail(userDto.getEmail());
        user.setPassword(passwordEncoder.encode(userDto.getPassword()));
        Role userRole = roleRepository.findByName("ROLE_USER");

        if (userRole == null)
            throw new RoleNotFoundException("Role not found");

        user.setRoles(Collections.singleton(userRole));
        userRepository.save(user);

        String token = UUID.randomUUID().toString();
        VerificationToken verificationToken = new VerificationToken();
        verificationToken.setToken(token);
        verificationToken.setUser(user);
        verificationToken.setExpiryDate(LocalDateTime.now().plusDays(1)); // Token valid for 24 hours

        verificationTokenRepository.save(verificationToken);

        String verificationUrl = DOMAIN + PATH_VERIFY_EMAIL + token;
        emailService.sendEmail(user.getEmail(), "Email Verification", "Click the link to verify your email: " + verificationUrl);
    }

    public boolean isUserCredentialsValid(String username, String password) {
        User user = userRepository.findByUsername(username);

        if (user.getUsername().equals(username) && passwordEncoder.matches(password, user.getPassword()))
            return true;

        return false;
    }

    public String getUsername(HttpServletRequest request) {
        String token = jwtUtil.extractJwtFromRequest(request);
        return jwtUtil.extractUsername(token);
    }

    public List<UsernameEmailDto> getAllUsers() {
        return userRepository.findAll().stream()
                .map(user -> new UsernameEmailDto(user.getUsername(), user.getEmail()))
                .collect(Collectors.toList());
    }

    public UserGetResponse getUser(String username) {
        User user = userRepository.findByUsername(username);
        return new UserGetResponse(user.getUsername(), user.isEnabled(),
                user.getRoles().stream().map(this::convertToDto).collect(Collectors.toSet()));
    }

    public UsernameEmailDto getEmailByToken(HttpServletRequest request) {
        String token = jwtUtil.extractJwtFromRequest(request);
        String username = jwtUtil.extractUsername(token);
        User user = userRepository.findByUsername(username);
        if (user == null)
            return null;
        return new UsernameEmailDto(username, user.getEmail());
    }

    public UserGetResponse getUserByToken(HttpServletRequest request) {
        String token = jwtUtil.extractJwtFromRequest(request);
        String username = jwtUtil.extractUsername(token);
        User user = userRepository.findByUsername(username);
        if (user == null)
            return null;
        return new UserGetResponse(user.getUsername(), user.isEnabled(),
                user.getRoles().stream().map(this::convertToDto).collect(Collectors.toSet()));
    }

    private RoleDto convertToDto(Role role) {
        return new RoleDto(role.getName());
    }

    public boolean createUser(UserCreateDto userCreateDto) {
        User user = userRepository.findByUsername(userCreateDto.getUsername());
        if (user != null)
            return false;

        user = userRepository.findByEmail(userCreateDto.getEmail());

        if (user != null)
            return false;

        User newUser = new User();

        newUser.setEnabled(true);

        newUser.setUsername(userCreateDto.getUsername());
        newUser.setEmail(userCreateDto.getEmail());

        Role userRole = roleRepository.findByName(userCreateDto.getRole());
        if (userRole != null)
            newUser.setRoles(Collections.singleton(userRole));
        else
            return false;

        userRepository.save(newUser);

        processForgotPassword(newUser.getEmail());

        return true;
    }

    public boolean updatePassword(String currentPassword, String newPassword, HttpServletRequest request) {
        String token = jwtUtil.extractJwtFromRequest(request);
        String username = jwtUtil.extractUsername(token);
        User user = userRepository.findByUsername(username);

        if (user == null)
            return false;

        if (!passwordEncoder.matches(currentPassword, user.getPassword()))
            return false;

        user.setPassword(passwordEncoder.encode(newPassword));

        userRepository.save(user);

        return true;
    }

    public boolean updateUser(String currentUsername, String newUsername, String newEmail, String newRole) {
        User user = userRepository.findByUsername(currentUsername);
        if (user == null)
            return false;

        if (newUsername != null && !userRepository.existsByUsername(newUsername))
            user.setUsername(newUsername);

        if (newEmail != null && !userRepository.existsByEmail(newEmail)) {
            user.setEmail(newEmail);
            user.setEnabled(true);
        }

        Role userRole = roleRepository.findByName(newRole);
        if (userRole != null) {
            user.getRoles().add(userRole);
            user.setRoles(user.getRoles());
        }

        userRepository.save(user);
        return true;
    }

    @Transactional
    public boolean deleteUserByUsername(String username) {
        if (userRepository.existsByUsername(username)) {
            userRepository.deleteByUsername(username);
            return true;
        }
        return false;
    }

    @Transactional
    public boolean deleteUser(HttpServletRequest request) {
        String token = jwtUtil.extractJwtFromRequest(request);
        String username = jwtUtil.extractUsername(token);
        if (username.equalsIgnoreCase("bob"))
            return false;
        if (userRepository.existsByUsername(username)) {
            userRepository.deleteByUsername(username);
            return true;
        }
        return false;
    }

    public boolean verifyUserEmail(String token) {
        Optional<VerificationToken> verificationTokenOpt = verificationTokenRepository.findByToken(token);
        if (verificationTokenOpt.isEmpty()) {
            return false;
        }

        VerificationToken verificationToken = verificationTokenOpt.get();
        if (verificationToken.getExpiryDate().isBefore(LocalDateTime.now())) {
            return false;
        }

        User user = verificationToken.getUser();
        user.setEnabled(true);
        userRepository.save(user);

        verificationTokenRepository.delete(verificationToken);

        return true;
    }

    public boolean resendVerificationEmail(String username) {
        User user = userRepository.findByUsername(username);
        if (user == null || user.isEnabled()) {
            return false;
        }
        VerificationToken token = verificationTokenRepository.findByUserId(user.getId());

        String verificationUrl = DOMAIN + PATH_VERIFY_EMAIL + token.getToken();
        emailService.sendEmail(user.getEmail(), "Resend Email Verification", "Click the link to verify your email: " + verificationUrl);

        return true;
    }

    public boolean processForgotPassword(String email) {
        User user = userRepository.findByEmail(email);
        if (user == null) {
            return false;
        }

        Optional<PasswordResetToken> passwordResetToken = passwordResetTokenRepository.findByUserId(user.getId());

        passwordResetToken.ifPresent(passwordResetTokenRepository::delete);

        String token = UUID.randomUUID().toString();
        PasswordResetToken passwordResetToken2 = new PasswordResetToken();
        passwordResetToken2.setToken(token);
        passwordResetToken2.setUser(user);
        passwordResetToken2.setExpiryDate(LocalDateTime.now().plusHours(24));  // Token valid for 24 hours

        passwordResetTokenRepository.save(passwordResetToken2);

        String resetLink = DOMAIN + PATH_RESET_PASSWORD + token;
        emailService.sendEmail(user.getEmail(), "Password Reset", "Click the link to reset your password: " + resetLink);

        return true;
    }

    public boolean processForgotUsername(String email) {
        User user = userRepository.findByEmail(email);
        if (user == null) {
            return false;
        }

        emailService.sendEmail(user.getEmail(), "Forgotten Username", "Your username is: " + user.getUsername());

        return true;
    }

    public boolean resetPassword(String token, String newPassword) {
        Optional<PasswordResetToken> passwordResetTokenOpt = passwordResetTokenRepository.findByToken(token);
        if (newPassword == null || passwordResetTokenOpt.isEmpty()) {
            return false;
        }

        PasswordResetToken passwordResetToken = passwordResetTokenOpt.get();
        if (passwordResetToken.getExpiryDate().isBefore(LocalDateTime.now())) {
            return false;
        }

        User user = passwordResetToken.getUser();
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        passwordResetTokenRepository.delete(passwordResetToken);

        return true;
    }

    public boolean initiateEmailUpdate(String username, String newEmail) {
        User user = userRepository.findByUsername(username);

        if (userRepository.existsByEmail(newEmail))
            return false;

        Optional<EmailToken> emailToken = emailTokenRepository.findByUserId(user.getId());

        emailToken.ifPresent(emailTokenRepository::delete);

        String token = UUID.randomUUID().toString();
        EmailToken emailToken2 = new EmailToken(user, token, newEmail);
        emailTokenRepository.save(emailToken2);

        String confirmationUrl = DOMAIN + "/confirm-email/" + token;
        emailService.sendEmail(newEmail, "Confirm your email",
                "Please click the link to confirm your new email: " + confirmationUrl);

        return true;
    }


    public boolean confirmEmailUpdate(String token) {
        Optional<EmailToken> emailTokenOpt = emailTokenRepository.findByToken(token);

        if (emailTokenOpt.isEmpty()) {
            return false;
        }

        EmailToken emailToken = emailTokenOpt.get();

        if (emailToken.getExpiryDate().isBefore(LocalDateTime.now())) {
            return false;
        }

        User user = emailToken.getUser();
        user.setEmail(emailToken.getNewEmail());
        userRepository.save(user);

        emailTokenRepository.delete(emailToken);

        return true;
    }
}
