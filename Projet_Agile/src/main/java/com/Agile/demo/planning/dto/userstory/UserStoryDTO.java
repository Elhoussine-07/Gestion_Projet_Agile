package com.Agile.demo.planning.dto.userstory;

import com.Agile.demo.model.UserStory;
import com.Agile.demo.model.WorkItemStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserStoryDTO {
    private Long id;
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

    public static UserStoryDTO fromEntity(UserStory story) {
        UserStoryDTOBuilder builder = UserStoryDTO.builder()
                .id(story.getId())
                .title(story.getTitle())
                .formattedDescription(story.getFormattedDescription())
                .storyPoints(story.getStoryPoints())
                .priority(story.getPriority())
                .status(story.getStatus())
                .productBacklogId(story.getProductBacklog() != null ?
                        story.getProductBacklog().getId() : null)
                .sprintBacklogId(story.getSprintBacklog() != null ?
                        story.getSprintBacklog().getId() : null)
                .taskCount(story.getTasks() != null ? story.getTasks().size() : 0)
                .progress(story.calculateProgress())
                .isInSprint(story.isInSprint())
                .isValid(story.isValid());

        // Description
        if (story.getDescription() != null) {
            builder.role(story.getDescription().getRole())
                    .action(story.getDescription().getAction())
                    .purpose(story.getDescription().getPurpose());
        }

        // Epic
        if (story.getEpic() != null) {
            builder.epicId(story.getEpic().getId())
                    .epicTitle(story.getEpic().getTitle());
        }

        // Sprint
        if (story.getSprintBacklog() != null) {
            builder.sprintNumber(story.getSprintBacklog().getSprintNumber());
        }

        // Completed tasks
        if (story.getTasks() != null) {
            long completed = story.getTasks().stream()
                    .filter(t -> t.getStatus() == WorkItemStatus.DONE)
                    .count();
            builder.completedTaskCount((int)completed);
        }

        // Acceptance Criteria
        if (story.getAcceptanceCriteria() != null) {
            builder.givenClauses(story.getAcceptanceCriteria().getGivenClauses())
                    .whenClauses(story.getAcceptanceCriteria().getWhenClauses())
                    .thenClauses(story.getAcceptanceCriteria().getThenClauses())
                    .gherkinFormat(story.getAcceptanceCriteria().toGherkinFormat());
        }

        // Custom Metrics
        if (story.getCustomMetrics() != null) {
            builder.customMetrics(story.getCustomMetrics());
        }

        return builder.build();
    }
}