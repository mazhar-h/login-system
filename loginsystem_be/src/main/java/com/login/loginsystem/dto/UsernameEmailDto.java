package com.login.loginsystem.dto;

import lombok.Data;

@Data
public class UsernameEmailDto {
    private String username;
    private String email;

    public UsernameEmailDto(String username, String email) {
        this.username = username;
        this.email = email;
    }
}