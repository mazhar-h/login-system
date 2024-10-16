package com.login.loginsystem.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserGetResponse {
    private String username;
    private boolean enabled;
    private Set<RoleDto> roles;
}
