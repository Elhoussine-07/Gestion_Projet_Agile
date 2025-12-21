package com.Agile.demo.execution.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDate;

@Data
public class SprintCloneRequest {
    private Integer newSprintNumber;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate newStartDate;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate newEndDate;
}