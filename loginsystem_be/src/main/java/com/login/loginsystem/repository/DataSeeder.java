package com.login.loginsystem.repository;

import com.login.loginsystem.model.Role;
import com.login.loginsystem.model.User;
import jakarta.annotation.PostConstruct;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Collections;

@Component
public class DataSeeder {

    private RoleRepository roleRepository;
    private UserRepository userRepository;
    private PasswordEncoder passwordEncoder;

    public DataSeeder(RoleRepository roleRepository, UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.roleRepository = roleRepository;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @PostConstruct
    public void seedData() {
        if (roleRepository.count() == 0) {
            Role userRole = new Role("ROLE_USER");
            Role adminRole = new Role("ROLE_ADMIN");

            roleRepository.saveAll(Arrays.asList(userRole, adminRole));
        }
        if (userRepository.count() == 0) {
            User user = new User();
            user.setUsername("bob");
            user.setEmail("test@gmail.com");
            user.setPassword(passwordEncoder.encode("1234567"));
            user.setEnabled(true);
            Role userRole = roleRepository.findByName("ROLE_ADMIN");
            user.setRoles(Collections.singleton(userRole));

            User user2 = new User();
            user2.setUsername("sarah");
            user2.setEmail("test2@gmail.com");
            user2.setPassword(passwordEncoder.encode("1234567"));
            user2.setEnabled(true);
            Role userRole2 = roleRepository.findByName("ROLE_USER");
            user2.setRoles(Collections.singleton(userRole2));

            User user3 = new User();
            user3.setUsername("wow");
            user3.setEmail("test3@gmail.com");
            user3.setPassword(passwordEncoder.encode("1234567"));
            user3.setEnabled(true);
            Role userRole3 = roleRepository.findByName("ROLE_USER");
            user2.setRoles(Collections.singleton(userRole3));
            userRepository.save(user);
            userRepository.save(user2);
            userRepository.save(user3);
        }
    }
}