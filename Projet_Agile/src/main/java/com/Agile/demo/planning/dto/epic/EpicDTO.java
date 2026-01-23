package com.Agile.demo.planning.dto.epic;

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
}