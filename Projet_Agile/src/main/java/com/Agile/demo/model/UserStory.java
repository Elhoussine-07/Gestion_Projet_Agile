package com.Agile.demo.model;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.util.*;

@Entity
@Table(name = "user_stories")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class UserStory extends AbstractWorkItem {

    // ===== DESCRIPTION =====

    @Embedded
    private UserStoryDescription description;

    @Embedded
    private AcceptanceCriteria acceptanceCriteria;

    @Column(name = "story_points", nullable = false)
    @Builder.Default
    private Integer storyPoints = 0;

    @Column(name = "priority", nullable = false)
    @Builder.Default
    private Integer priority = 0;

    @Column(name = "business_value")
    private Integer businessValue;

    // ===== METRICS FLEXIBLES =====

    @ElementCollection
    @CollectionTable(
            name = "user_story_metrics",
            joinColumns = @JoinColumn(name = "user_story_id")
    )
    @MapKeyColumn(name = "metric_name")
    @Column(name = "metric_value")
    @Builder.Default
    private Map<String, Integer> customMetrics = new HashMap<>();

    // ===== METRIC HELPERS =====

    public void setMetric(String name, Integer value) {
        customMetrics.put(name, value);
    }

    public Integer getMetric(String name) {
        return customMetrics.getOrDefault(name, 0);
    }

    // ===== RELATIONS =====

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "epic_id")
    private Epic epic;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "product_backlog_id", nullable = false)
    private ProductBacklog productBacklog;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sprint_backlog_id")
    private SprintBacklog sprintBacklog;

    @OneToMany(mappedBy = "userStory", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<Task> tasks = new ArrayList<>();

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "user_story_dependencies",
            joinColumns = @JoinColumn(name = "user_story_id"),
            inverseJoinColumns = @JoinColumn(name = "depends_on_id")
    )
    @Builder.Default
    private List<UserStory> dependencies = new ArrayList<>();


    // ===== COHÉRENCE DES RELATIONS =====

    public void addTask(Task task) {
        if (!tasks.contains(task)) {
            tasks.add(task);
            task.setUserStory(this);
        }
    }

    public void removeTask(Task task) {
        tasks.remove(task);
        task.setUserStory(null);
    }

    public void setEpic(Epic epic) {
        if (this.epic != null) {
            this.epic.getUserStories().remove(this);
        }
        this.epic = epic;
        if (epic != null && !epic.getUserStories().contains(this)) {
            epic.getUserStories().add(this);
        }
    }

    public void addDependency(UserStory dependency) {
        if (!dependencies.contains(dependency)) {
            dependencies.add(dependency);
        }
    }

    public void removeDependency(UserStory dependency) {
        dependencies.remove(dependency);
    }

    // ===== MÉTIER =====

    public double calculateProgress() {
        if (tasks.isEmpty()) return 0.0;
        long done = tasks.stream()
                .filter(t -> t.getStatus() == WorkItemStatus.DONE)
                .count();
        return (done * 100.0) / tasks.size();
    }

    public boolean areAllTasksCompleted() {
        return !tasks.isEmpty() &&
                tasks.stream().allMatch(t -> t.getStatus() == WorkItemStatus.DONE);
    }

    public boolean areDependenciesCompleted() {
        return dependencies.stream()
                .allMatch(d -> d.getStatus() == WorkItemStatus.DONE);
    }

    public boolean canBeStarted() {
        return getStatus() == WorkItemStatus.TODO && areDependenciesCompleted();
    }

    public boolean isValid() {
        return description != null && description.isValid()
                && acceptanceCriteria != null && acceptanceCriteria.isValid();
    }

    public boolean isInSprint() {
        return sprintBacklog != null;
    }

    public int getTotalEstimatedHours() {
        return tasks.stream().mapToInt(Task::getEstimatedHours).sum();
    }

    public String getFormattedDescription() {
        return description != null ? description.toString() : "";
    }

    public String getFormattedAcceptanceCriteria() {
        return acceptanceCriteria != null ? acceptanceCriteria.toGherkinFormat() : "";
    }

    public String getShortDescription() {
        return description != null ? description.toShortString() : "";
    }

    // ===== STANDARD =====

    @Override
    public String toString() {
        return String.format(
                "UserStory{id=%d, title='%s', storyPoints=%d, priority=%d, status=%s}",
                id, getTitle(), storyPoints, priority, getStatus()
        );
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof UserStory)) return false;
        UserStory that = (UserStory) o;
        return id != null && id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
