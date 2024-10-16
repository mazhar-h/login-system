package com.login.loginsystem.repository;

import com.login.loginsystem.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    User findByUsername(String username);
    User findByEmail(String email);
    User findByGoogleId(String googleId);
    boolean existsByUsername(String username);
    boolean existsByEmail(String email);
    void deleteByUsername(String username);
}