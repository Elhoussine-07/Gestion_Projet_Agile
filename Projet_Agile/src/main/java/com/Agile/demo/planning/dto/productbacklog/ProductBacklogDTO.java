package com.Agile.demo.planning.dto.productbacklog;

import com.Agile.demo.model.ProductBacklog;
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
    private int epicCount;
    private int userStoryCount;
    private int unassignedStoryCount;
    private PrioritizationMethod selectedMethod;

    public static ProductBacklogDTO fromEntity(ProductBacklog backlog) {
        long unassignedCount = backlog.getStories() != null ?
                backlog.getStories().stream()
                        .filter(us -> us.getSprintBacklog() == null)
                        .count() : 0;

        return ProductBacklogDTO.builder()
                .id(backlog.getId())
                .name(backlog.getName())
                .projectId(backlog.getProject() != null ? backlog.getProject().getId() : null)
                .projectName(backlog.getProject() != null ? backlog.getProject().getName() : null)
                .epicCount(backlog.getEpics() != null ? backlog.getEpics().size() : 0)
                .userStoryCount(backlog.getStories() != null ? backlog.getStories().size() : 0)
                .unassignedStoryCount((int)unassignedCount)
                .selectedMethod(backlog.getSelectedMethod())
                .build();
    }
}