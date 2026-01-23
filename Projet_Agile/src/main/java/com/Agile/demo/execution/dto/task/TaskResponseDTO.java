package com.Agile.demo.execution.dto.task;

import com.Agile.demo.model.WorkItemStatus;
import lombok.Data;
import java.time.LocalDateTime;

@Data
public class TaskResponseDTO {
    private Long id;
    private String title;
    private String description;
    private Integer estimatedHours;
    private Integer actualHours;
    private Integer remainingHours; // Champ calculé souvent utile
    private WorkItemStatus status;

    // On ne renvoie pas l'objet User entier, juste les infos utiles
    private Long assignedUserId;
    private String assignedUsername;

    // Idem pour la UserStory
    private Long userStoryId;

    // Infos de blocage
    private boolean blocked;
    private String blockReason;
}