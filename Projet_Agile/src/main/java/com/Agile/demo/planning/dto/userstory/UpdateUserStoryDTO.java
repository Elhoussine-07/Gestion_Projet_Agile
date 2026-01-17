package com.Agile.demo.planning.dto.userstory;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateUserStoryDTO {
    private String title;
    private String role;
    private String action;
    private String purpose;
    private Integer storyPoints;
}