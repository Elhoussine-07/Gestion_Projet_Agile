package com.Agile.demo.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import jakarta.persistence.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Entity
@Getter
@Setter
@Table(name = "sprint_backlogs")
public class SprintBacklog extends AbstractBacklog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // CORRECTION: Retirer le @Id en double ici
    @Column(nullable = false)
    private Integer SprintNumber;

    @Column(nullable = false)
    private LocalDate startDate;

    @Column(nullable = false)
    private LocalDate endDate;

    @Column(length = 500)
    private String goal;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SprintStatus sprintStatus = SprintStatus.PLANNED;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id", nullable = false)
    private Project project;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "sprint_user_stories",
            joinColumns = @JoinColumn(name = "sprint_backlog_id"),
            inverseJoinColumns = @JoinColumn(name = "user_story_id")
    )
    private List<UserStory> userStories = new ArrayList<>();

    @OneToMany(mappedBy = "sprintBacklog", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    private List<Task> tasks = new ArrayList<>();

    @Transient
    private IPrioritizationStrategy prioritizationStrategy;

    // Constructeur personnalisé
    public SprintBacklog(String name, Integer sprintNumber, LocalDate startDate, LocalDate endDate, String goal) {
        super(name, "Sprint Backlog for " + name);
        this.SprintNumber = sprintNumber;
        this.startDate = startDate;
        this.endDate = endDate;
        this.goal = goal;
        this.userStories = new ArrayList<>();
        this.tasks = new ArrayList<>();
    }

    // Méthodes utilitaires pour maintenir la cohérence des relations
    public void addUserStory(UserStory userStory) {
        if (!userStories.contains(userStory)) {
            userStories.add(userStory);
            userStory.setSprintBacklog(this);
        }
    }

    public void removeUserStory(UserStory userStory) {
        userStories.remove(userStory);
        userStory.setSprintBacklog(null);
    }

    public void addTask(Task task) {
        if (!tasks.contains(task)) {
            tasks.add(task);
            task.setSprintBacklog(this);
        }
    }

    public void removeTask(Task task) {
        tasks.remove(task);
        task.setSprintBacklog(null);
    }

    // Méthodes métier
    public void startSprint() {
        if (sprintStatus == SprintStatus.PLANNED) {
            this.sprintStatus = SprintStatus.ACTIVE;
        }
    }

    public void completeSprint() {
        if (sprintStatus == SprintStatus.ACTIVE) {
            this.sprintStatus = SprintStatus.COMPLETED;
        }
    }

    public void cancelSprint() {
        this.sprintStatus = SprintStatus.CANCELLED;
    }

    public int calculateVelocity() {
        return userStories.stream()
                .filter(us -> us.getStatus() == WorkItemStatus.DONE)
                .mapToInt(UserStory::getStoryPoints)
                .sum();
    }

    public double calculateProgress() {
        if (userStories.isEmpty()) {
            return 0.0;
        }
        long completedStories = userStories.stream()
                .filter(us -> us.getStatus() == WorkItemStatus.DONE)
                .count();
        return (completedStories * 100.0) / userStories.size();
    }

    public int getTotalStoryPoints() {
        return userStories.stream()
                .mapToInt(UserStory::getStoryPoints)
                .sum();
    }

    public int getRemainingStoryPoints() {
        return userStories.stream()
                .filter(us -> us.getStatus() != WorkItemStatus.DONE)
                .mapToInt(UserStory::getStoryPoints)
                .sum();
    }

    public boolean isActive() {
        return sprintStatus == SprintStatus.ACTIVE;
    }

    public boolean isCompleted() {
        return sprintStatus == SprintStatus.COMPLETED;
    }

    public long getSprintDuration() {
        return java.time.temporal.ChronoUnit.DAYS.between(startDate, endDate);
    }

    @Override
    public void addItem(AbstractWorkItem item) {
        if (item instanceof UserStory) {
            addUserStory((UserStory) item);
        } else if (item instanceof Task) {
            addTask((Task) item);
        } else {
            throw new IllegalArgumentException("Type non supporté: " + item.getClass().getSimpleName());
        }
    }

    @Override
    public void removeItem(AbstractWorkItem item) {
        if (item instanceof UserStory) {
            removeUserStory((UserStory) item);
        } else if (item instanceof Task) {
            removeTask((Task) item);
        }
    }

    @Override
    public List<AbstractWorkItem> getItems() {
        List<AbstractWorkItem> items = new ArrayList<>();
        items.addAll(userStories);
        items.addAll(tasks);
        return items;
    }

    public void setSprintNumber(Integer sprintNumber) {
        this.SprintNumber = sprintNumber;
    }

    @Override
    public String toString() {
        return String.format("SprintBacklog{id=%d, sprintNumber=%d, name='%s', status=%s, startDate=%s, endDate=%s}",
                id, SprintNumber, getName(), sprintStatus, startDate, endDate);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof SprintBacklog)) return false;
        SprintBacklog that = (SprintBacklog) o;
        return id != null && id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}