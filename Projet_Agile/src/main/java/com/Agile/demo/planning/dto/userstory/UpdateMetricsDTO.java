package com.Agile.demo.planning.dto.userstory;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateMetricsDTO {
    private Map<String, Integer> metrics;
}