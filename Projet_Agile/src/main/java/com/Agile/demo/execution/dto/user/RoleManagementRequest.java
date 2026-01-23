package com.Agile.demo.execution.dto.user;

import com.Agile.demo.model.Role;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.NotNull;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RoleManagementRequest {

    @NotNull(message = "Le rôle est obligatoire")
    private Role role;
}