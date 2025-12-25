package com.Agile.demo.planning.dto.project;

import lombok.Data;

import java.time.LocalDate;

@Data
public class UpdateProjectDTO {
    private String name;
    private String description;
    private LocalDate startDate;
    private LocalDate endDate;
}
