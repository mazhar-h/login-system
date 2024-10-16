package com.login.loginsystem.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UserCreateDto {
    @Size(min = 3, max = 40, message = "Username should have at least 3-40 characters")
    @Pattern(regexp = "^[a-zA-Z0-9\\-_]*$")
    @NotBlank(message = "Name is mandatory")
    private String username;
    @Size(max = 254, message = "Email should not exceed 254 characters")
    @Email(message = "Email should be valid")
    @NotBlank(message = "Email is mandatory")
    private String email;
    private String role;
}
