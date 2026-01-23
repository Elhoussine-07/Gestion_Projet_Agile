package com.Agile.demo.planning.dto.project;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AddMemberDTO {
    @NotNull(message = "User ID is required")
    private Long userId;
}