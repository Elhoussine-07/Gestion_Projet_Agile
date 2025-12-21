package com.Agile.demo.execution.dto;

import com.Agile.demo.model.Role;
import lombok.Data;

@Data
public class UserCreateRequest {
    private String username;
    private String email;
    private String password;
    private Role role;
}