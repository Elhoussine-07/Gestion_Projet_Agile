package com.Agile.demo.execution.dto.task;

import lombok.Data;

@Data
public class CreateTaskRequest {
    private Long userStoryId;
    private String title;
    private String description;
    private Integer estimatedHours;
    private Long assignedUserId; // Optionnel à la création
}