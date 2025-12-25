package com.Agile.demo.execution.dto;

import com.Agile.demo.model.Role;
import lombok.Data;

@Data
public class UserUpdateRequest {
    private String email;
    private Role role;
}