package com.Agile.demo.execution.dto;

import com.Agile.demo.model.Role;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserUpdateRequest {
    private String email;
    private Role role;

    private String firstName;
    private String lastName;
    private String phoneNumber;
    private Boolean isActive;
}