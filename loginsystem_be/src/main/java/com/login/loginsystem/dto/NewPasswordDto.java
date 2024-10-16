package com.login.loginsystem.dto;

import lombok.Data;

@Data
public class NewPasswordDto {
    private String currentPassword;
    private String newPassword;
}
