package com.Agile.demo.model;

import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import jakarta.persistence.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "sprint_backlogs")
public class SprintBacklog extends AbstractBacklog {

    @Column(nullable = false)
    private Integer sprintNumber;

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
        this.sprintNumber = sprintNumber;
        this.startDate = startDate;
        this.endDate = endDate;
        this.goal = goal;
        this.sprintStatus = SprintStatus.PLANNED;
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

    /**
     * Calcule la vélocité du sprint (somme des story points des User Stories complétées)
     */
    public int calculateVelocity() {
        return userStories.stream()
                .filter(us -> us.getStatus() == Status.DONE)
                .mapToInt(UserStory::getStoryPoints)
                .sum();
    }

    /**
     * Calcule le pourcentage de progression du sprint
     */
    public double calculateProgress() {
        if (userStories.isEmpty()) {
            return 0.0;
        }
        long completedStories = userStories.stream()
                .filter(us -> us.getStatus() == Status.DONE)
                .count();
        return (completedStories * 100.0) / userStories.size();
    }

    /**
     * Calcule le nombre total de story points du sprint
     */
    public int getTotalStoryPoints() {
        return userStories.stream()
                .mapToInt(UserStory::getStoryPoints)
                .sum();
    }

    /**
     * Calcule le nombre de story points restants
     */
    public int getRemainingStoryPoints() {
        return userStories.stream()
                .filter(us -> us.getStatus() != Status.DONE)
                .mapToInt(UserStory::getStoryPoints)
                .sum();
    }

    /**
     * Vérifie si le sprint est en cours
     */
    public boolean isActive() {
        return sprintStatus == SprintStatus.ACTIVE;
    }

    /**
     * Vérifie si le sprint est terminé
     */
    public boolean isCompleted() {
        return sprintStatus == SprintStatus.COMPLETED;
    }

    /**
     * Obtient la durée du sprint en jours
     */
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

    /**
     * Retire un item du sprint backlog
     *
     * @param item L'item à retirer
     */
    @Override
    public void removeItem(AbstractWorkItem item) {
        if (item instanceof UserStory) {
            removeUserStory((UserStory) item);
        } else if (item instanceof Task) {
            removeTask((Task) item);
        }
    }

    /**
     * Retourne tous les items (UserStories + Tasks) du sprint
     *
     * @return Liste de tous les AbstractWorkItem
     */
    @Override
    public List<AbstractWorkItem> getItems() {
        List<AbstractWorkItem> items = new ArrayList<>();
        items.addAll(userStories);
        items.addAll(tasks);
        return items;
    }
}