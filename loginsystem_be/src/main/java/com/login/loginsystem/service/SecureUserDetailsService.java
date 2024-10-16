package com.login.loginsystem.service;

import com.login.loginsystem.model.User;
import com.login.loginsystem.repository.UserRepository;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class SecureUserDetailsService implements UserDetailsService {

    private UserRepository userRepository;

    public SecureUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException, DisabledException {
        User user = userRepository.findByUsername(username);
        if (user == null) {
            throw new UsernameNotFoundException("User not found");
        }
        if (!user.isEnabled()) {
            throw new DisabledException("User is disabled");
        }
        return new SecureUserDetails(user);
    }
}