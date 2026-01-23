package com.Agile.demo.planning.dto.userstory;

import com.Agile.demo.model.WorkItemStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserStoryDTO {
    private Integer id;
    private String title;
    private String formattedDescription;
    private String role;
    private String action;
    private String purpose;
    private Integer storyPoints;
    private Integer priority;
    private WorkItemStatus status;
    private Long epicId;
    private String epicTitle;
    private Long productBacklogId;
    private Long sprintBacklogId;
    private Integer sprintNumber;
    private int taskCount;
    private int completedTaskCount;
    private double progress;
    private boolean isInSprint;
    private boolean isValid;

    // Acceptance Criteria
    private List<String> givenClauses;
    private List<String> whenClauses;
    private List<String> thenClauses;
    private String gherkinFormat;

    // Custom Metrics
    private Map<String, Integer> customMetrics;
}