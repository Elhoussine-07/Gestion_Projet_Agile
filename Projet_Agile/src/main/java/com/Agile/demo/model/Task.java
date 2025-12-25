package com.Agile.demo.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDate;

@Entity
@Table(name = "tasks")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Task extends AbstractWorkItem {

    @Column(name = "description", length = 2000)
    private String description;

    @Column(name = "estimated_hours", nullable = false)
    private Integer estimatedHours = 0;

    @Column(name = "actual_hours", nullable = false)
    private Integer actualHours = 0;

    // ===== NOUVEAUX CHAMPS À AJOUTER =====

    /**
     * Indique si la tâche est bloquée
     */
    @Column(name = "is_blocked", nullable = false)
    private boolean isBlocked = false;

    /**
     * Raison du blocage de la tâche
     */
    @Column(name = "block_reason", length = 500)
    private String blockReason;

    /**
     * Date de complétion de la tâche
     */
    @Column(name = "completed_date")
    private LocalDate completedDate;

    // ===== RELATIONS (INCHANGÉES) =====

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_story_id", nullable = false)
    private UserStory userStory;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assigned_user_id")
    private User assignedUser;

    @ManyToOne
    @JoinColumn(name = "sprint_backlog_id")
    private SprintBacklog sprintBacklog;



    // ===== CONSTRUCTEURS (INCHANGÉS) =====

    public Task(String title, Integer estimatedHours) {
        super(title, WorkItemStatus.TODO);
        this.estimatedHours = estimatedHours;
        this.actualHours = 0;
    }

    public Task(String title, String description, Integer estimatedHours) {
        super(title, WorkItemStatus.TODO);
        this.description = description;
        this.estimatedHours = estimatedHours;
        this.actualHours = 0;
    }

    public Task(String title, String description, Integer estimatedHours, UserStory userStory) {
        super(title, WorkItemStatus.TODO);
        this.description = description;
        this.estimatedHours = estimatedHours;
        this.actualHours = 0;
        this.userStory = userStory;
    }

    // ===== MÉTHODES DE COHÉRENCE DES RELATIONS =====

    public void assignTo(User user) {
        this.assignedUser = user;
    }

    public void unassign() {
        this.assignedUser = null;
    }

    public boolean isAssigned() {
        return this.assignedUser != null;
    }

    // ===== MÉTHODES MÉTIER =====

    public void logHours(int hours) {
        if (hours < 0) {
            throw new IllegalArgumentException("Les heures ne peuvent pas être négatives");
        }
        this.actualHours += hours;
    }

    public double calculateHoursProgress() {
        if (estimatedHours == 0) {
            return 0.0;
        }
        double progress = (actualHours * 100.0) / estimatedHours;
        return Math.min(progress, 100.0);
    }

    public boolean isOverEstimate() {
        return actualHours > estimatedHours;
    }

    public int getRemainingHours() {
        int remaining = estimatedHours - actualHours;
        return Math.max(remaining, 0);
    }

    public boolean canBeAssigned() {
        return this.status == WorkItemStatus.TODO && !isAssigned();
    }

    @Override
    public void start() {
        if (this.status == WorkItemStatus.TODO) {
            if (assignedUser == null) {
                throw new IllegalStateException("La tâche doit être assignée avant de démarrer");
            }
            updateStatus(WorkItemStatus.IN_PROGRESS);
        }
    }

    @Override
    public void complete() {
        if (this.status != WorkItemStatus.IN_PROGRESS &&
                this.status != WorkItemStatus.IN_REVIEW &&
                this.status != WorkItemStatus.TESTING) {
            throw new IllegalStateException("La tâche doit être en cours pour être terminée");
        }
        updateStatus(WorkItemStatus.DONE);
        this.completedDate = LocalDate.now(); // NOUVELLE LIGNE
    }

    public void moveToReview() {
        if (this.status == WorkItemStatus.IN_PROGRESS) {
            updateStatus(WorkItemStatus.IN_REVIEW);
        }
    }

    public void moveToTesting() {
        if (this.status != WorkItemStatus.IN_PROGRESS &&
                this.status != WorkItemStatus.IN_REVIEW) {
            throw new IllegalStateException(
                    "La tâche doit être en cours ou en revue pour passer en test"
            );
        }
        this.status = WorkItemStatus.TESTING;
    }

    @Override
    public int getProgress() {
        if (isDone()) {
            return 100;
        }
        return (int) calculateHoursProgress();
    }

    @Override
    public String toString() {
        return String.format("Task{id=%d, title='%s', estimatedHours=%d, actualHours=%d, status=%s, assigned=%s, blocked=%s}",
                getId(), getTitle(), estimatedHours, actualHours, getStatus(),
                isAssigned() ? assignedUser.getUsername() : "non assignée",
                isBlocked ? "OUI" : "NON");
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Task)) return false;
        Task task = (Task) o;
        return getId() != null && getId().equals(task.getId());
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}