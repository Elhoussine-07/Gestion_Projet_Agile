package com.Agile.demo.execution.dto.task;

import lombok.Data;

@Data
public class UpdateTaskRequest {
    private String title;
    private String description;
    private Integer estimatedHours;
    // On ne met pas le statut ici car il suit un workflow spécifique
}