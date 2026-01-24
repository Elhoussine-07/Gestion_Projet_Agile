package com.Agile.demo.planning.dto.productbacklog;

import com.Agile.demo.model.PrioritizationMethod;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductBacklogDTO {
    private Long id;
    private String name;
    private Long projectId;
    private String projectName;
    private Integer totalBusinessValue;
    private int epicCount;
    private int userStoryCount;
    private int unassignedStoryCount;
    private PrioritizationMethod selectedMethod;
}