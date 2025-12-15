package com.agile.demo.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Entity
@Table(name = "tasks")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Task extends AbstractWorkItem {



    @Getter
    @Column(name = "description", length = 2000)
    private String description;

    /**
     * Nombre d'heures estimées pour réaliser la tâche
     */
    @Column(name = "estimated_hours", nullable = false)
    private Integer estimatedHours = 0;

    /**
     * Nombre d'heures réellement passées sur la tâche
     */
    @Column(name = "actual_hours", nullable = false)
    private Integer actualHours = 0;

    // ===== RELATIONS =====

    /**
     * User Story à laquelle appartient cette tâche (obligatoire)
     * -- SETTER --
     *  Définit la User Story parente

     */
    @Setter
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_story_id", nullable = false)
    private UserStory userStory;

    /**
     * Utilisateur assigné à cette tâche (optionnel)
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assigned_user_id")
    private User assignedUser;

    /**
     * Sprint Backlog qui suit cette tâche (optionnel)
     * -- SETTER --
     *  Définit le Sprint Backlog

     */
    @Setter
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sprint_backlog_id")
    private SprintBacklog sprintBacklog;

    // ===== CONSTRUCTEURS =====

    /**
     * Constructeur avec titre et estimation
     */
    public Task(String title, Integer estimatedHours) {
        super(title, WorkItemStatus.TODO);
        this.estimatedHours = estimatedHours;
        this.actualHours = 0;
    }

    /**
     * Constructeur avec titre, description et estimation
     */
    public Task(String title, String description, Integer estimatedHours) {
        super(title, WorkItemStatus.TODO);
        this.description = description;
        this.estimatedHours = estimatedHours;
        this.actualHours = 0;
    }

    /**
     * Constructeur complet
     */
    public Task(String title, String description, Integer estimatedHours, UserStory userStory) {
        super(title, WorkItemStatus.TODO);
        this.description = description;
        this.estimatedHours = estimatedHours;
        this.actualHours = 0;
        this.userStory = userStory;
    }

    // ===== MÉTHODES DE COHÉRENCE DES RELATIONS =====

    /**
     * Assigne la tâche à un utilisateur
     */
    public void assignTo(User user) {
        this.assignedUser = user;
    }

    /**
     * Désassigne la tâche
     */
    public void unassign() {
        this.assignedUser = null;
    }

    /**
     * Vérifie si la tâche est assignée
     */
    public boolean isAssigned() {
        return this.assignedUser != null;
    }

    // ===== MÉTHODES MÉTIER =====

    /**
     * Enregistre les heures travaillées sur la tâche
     */
    public void logHours(int hours) {
        if (hours < 0) {
            throw new IllegalArgumentException("Les heures ne peuvent pas être négatives");
        }
        this.actualHours += hours;
    }

    /**
     * Calcule le pourcentage de complétion basé sur les heures
     */
    public double calculateHoursProgress() {
        if (estimatedHours == 0) {
            return 0.0;
        }
        double progress = (actualHours * 100.0) / estimatedHours;
        return Math.min(progress, 100.0); // Plafonné à 100%
    }

    /**
     * Vérifie si la tâche est en retard (heures réelles > heures estimées)
     */
    public boolean isOverEstimate() {
        return actualHours > estimatedHours;
    }

    /**
     * Calcule les heures restantes estimées
     */
    public int getRemainingHours() {
        int remaining = estimatedHours - actualHours;
        return Math.max(remaining, 0);
    }

    /**
     * Vérifie si la tâche peut être assignée
     */
    public boolean canBeAssigned() {
        return this.status == WorkItemStatus.TODO && !isAssigned();
    }

    /**
     * Démarre la tâche et l'assigne automatiquement si un utilisateur est défini
     */
    @Override
    public void start() {
        if (this.status == WorkItemStatus.TODO) {
            if (assignedUser == null) {
                throw new IllegalStateException("La tâche doit être assignée avant de démarrer");
            }
            updateStatus(WorkItemStatus.IN_PROGRESS);
        }
    }

    /**
     * Termine la tâche
     */
    @Override
    public void complete() {
        if (this.status != WorkItemStatus.IN_PROGRESS && this.status != WorkItemStatus.IN_REVIEW && this.status != WorkItemStatus.TESTING) {
            throw new IllegalStateException("La tâche doit être en cours pour être terminée");
        }
        updateStatus(WorkItemStatus.DONE);
    }

    /**
     * Passe la tâche en revue
     */
    public void moveToReview() {
        if (this.status == WorkItemStatus.IN_PROGRESS) {
            updateStatus(WorkItemStatus.IN_REVIEW);
        }
    }

    /**
     * Passe la tâche en test
     */
    public void moveToTesting() {
        if (this.status != WorkItemStatus.IN_PROGRESS &&
                this.status != WorkItemStatus.IN_REVIEW) {
            throw new IllegalStateException(
                    "La tâche doit être en cours ou en revue pour passer en test"
            );
        }

        this.status = WorkItemStatus.TESTING;
    }


    // ===== IMPLÉMENTATION DES MÉTHODES ABSTRAITES =====

    @Override
    public int getProgress() {
        // Surcharge pour utiliser le calcul basé sur les heures
        if (isDone()) {
            return 100;
        }
        return (int) calculateHoursProgress();
    }

    // ===== MÉTHODES STANDARD =====

    @Override
    public String toString() {
        return String.format("Task{id=%d, title='%s', estimatedHours=%d, actualHours=%d, status=%s, assigned=%s}",
                getId(), getTitle(), estimatedHours, actualHours, getStatus(),
                isAssigned() ? assignedUser.getUsername() : "non assignée");
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