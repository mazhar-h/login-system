package com.login.loginsystem.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Data
@NoArgsConstructor
public class EmailToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    private User user;

    private String token;
    private String newEmail;
    private LocalDateTime expiryDate;

    public EmailToken(User user, String token, String newEmail) {
        this.user = user;
        this.token = token;
        this.newEmail = newEmail;
        this.expiryDate = LocalDateTime.now().plusHours(24);
    }
}
