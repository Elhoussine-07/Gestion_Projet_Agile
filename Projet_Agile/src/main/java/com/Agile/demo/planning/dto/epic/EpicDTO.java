package com.Agile.demo.planning.dto.epic;

import com.Agile.demo.model.Epic;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EpicDTO {
    private Long id;
    private String title;
    private String description;
    private Long productBacklogId;
    private int userStoryCount;
    private int completedStoryCount;
    private int progress;

    public static EpicDTO fromEntity(Epic epic) {
        int totalStories = epic.getUserStories() != null ? epic.getUserStories().size() : 0;
        long completedStories = epic.getUserStories() != null ?
                epic.getUserStories().stream()
                        .filter(us -> us.getStatus() == com.Agile.demo.model.WorkItemStatus.DONE)
                        .count() : 0;

        int progress = totalStories > 0 ? (int)((completedStories * 100.0) / totalStories) : 0;

        return EpicDTO.builder()
                .id(epic.getId())
                .title(epic.getTitle())
                .description(epic.getDescription())
                .productBacklogId(epic.getProductBacklog() != null ?
                        epic.getProductBacklog().getId() : null)
                .userStoryCount(totalStories)
                .completedStoryCount((int)completedStories)
                .progress(progress)
                .build();
    }
}