package com.Agile.demo.execution.dto;

import com.Agile.demo.model.SprintStatus;
import lombok.Data;

import java.time.LocalDate;

@Data
public class SprintBacklogResponseDTO {
    private Long id;
    private Long projectId;
    private Integer sprintNumber;
    private LocalDate startDate;
    private LocalDate endDate;
    private String goal;
    private SprintStatus status;
}
