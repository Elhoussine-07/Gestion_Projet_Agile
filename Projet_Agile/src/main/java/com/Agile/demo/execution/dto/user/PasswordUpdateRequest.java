package com.Agile.demo.execution.dto.user;

import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class PasswordUpdateRequest {
    private String currentPassword;
    private String newPassword;
}