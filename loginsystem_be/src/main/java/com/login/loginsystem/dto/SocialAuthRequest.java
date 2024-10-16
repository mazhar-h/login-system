package com.login.loginsystem.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class SocialAuthRequest {
    @NotBlank(message = "Username is mandatory")
    private String username;
}
