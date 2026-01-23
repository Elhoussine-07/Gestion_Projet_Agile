package com.Agile.demo.execution.dto.task;

import lombok.Data;

@Data
public class TaskCreateRequest {
    private Long userStoryId;
    private String title;
    private String description;
    private Integer estimatedHours;
}
