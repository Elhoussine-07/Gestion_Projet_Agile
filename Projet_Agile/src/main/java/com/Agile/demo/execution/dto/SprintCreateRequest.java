package com.Agile.demo.execution.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SprintCreateRequest {

    private Long projectId;

    private Integer sprintNumber;

    private LocalDate startDate;

    private LocalDate endDate;

    private String goal;

    // Liste optionnelle des IDs des User Stories à associer au sprint
    private List<Long> userStoryIds;
}