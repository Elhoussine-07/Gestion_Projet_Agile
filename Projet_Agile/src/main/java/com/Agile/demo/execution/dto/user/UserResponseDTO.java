package com.Agile.demo.execution.dto.user;

import com.Agile.demo.model.Role;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserResponseDTO {
    private Long id;
    private String username;
    private String email;
    private Set<Role> roles;
    private String firstName;
    private String lastName;
    private String phoneNumber;
    private Boolean isActive;
    private Boolean passwordResetRequired;
}