package com.Agile.demo.execution.dto;

import com.Agile.demo.model.Role;
import jakarta.validation.constraints.Email;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserUpdateRequest {

    @Email(message = "Format d'email invalide")
    private String email;

    private Set<Role> roles;
    private String firstName;
    private String lastName;
    private String phoneNumber;
    private Boolean isActive;
}