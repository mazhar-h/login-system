package com.login.loginsystem.dto;

import com.login.loginsystem.model.Role;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.Set;

@Data
public class UserDto {
    @Size(min = 3, max = 40, message = "Username should have at least 3-40 characters")
    @Pattern(regexp = "^[a-zA-Z0-9\\-_]*$")
    @NotBlank(message = "Name is mandatory")
    private String username;
    @NotBlank(message = "Password is mandatory")
    @Size(min = 6, message = "Password should have at least 6 characters")
    private String password;
    @Email(message = "Email should be valid")
    @NotBlank(message = "Email is mandatory")
    private String email;
    private Set<Role> roles;
}
