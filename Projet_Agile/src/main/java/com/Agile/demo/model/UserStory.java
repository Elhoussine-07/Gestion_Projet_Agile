package com.Agile.demo.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "user_stories")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UserStory extends AbstractWorkItem {

    /**
     * Description structurée de la User Story (Value Object)
     * Format: "En tant que [role], je veux [action] afin de [purpose]"
     */
    @Embedded
    private UserStoryDescription description;

    /**
     * Critères d'acceptation pour valider la complétion
     */
    @Column(name = "acceptance_criteria", length = 2000)
    private String acceptanceCriteria;

    /**
     * Estimation en Story Points (complexité relative)
     */
    @Column(name = "story_points", nullable = false)
    private Integer storyPoints = 0;

    /**
     * Score de priorité (1 = le plus prioritaire)
     * Calculé par la stratégie de priorisation du Product Backlog
     */
    @Column(name = "priority", nullable = false)
    private Integer priority = 0;

    // ===== DONNÉES POUR LE CALCUL DE PRIORISATION =====

    /**
     * Valeur métier pour l'utilisateur/entreprise (1-10)
     * Utilisé par toutes les stratégies de priorisation
     */
    @Column(name = "business_value", nullable = false)
    private Integer businessValue = 5;

    /**
     * Urgence/criticité temporelle (1-10)
     * Utilisé par MoSCoW et WSJF
     */
    @Column(name = "urgency", nullable = false)
    private Integer urgency = 5;

    /**
     * Criticité temporelle (1-10)
     * Utilisé par WSJF (SAFe)
     */
    @Column(name = "time_criticality", nullable = false)
    private Integer timeCriticality = 5;

    /**
     * Réduction du risque (1-10)
     * Utilisé par WSJF (SAFe)
     */
    @Column(name = "risk_reduction", nullable = false)
    private Integer riskReduction = 5;

    // ===== RELATIONS =====

    /**
     * Epic auquel appartient cette User Story (optionnel)
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "epic_id")
    private Epic epic;

    /**
     * Product Backlog auquel appartient cette User Story (obligatoire)
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_backlog_id", nullable = false)
    private ProductBacklog productBacklog;

    /**
     * Sprint Backlog auquel est assignée cette User Story (optionnel)
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sprint_backlog_id")
    private SprintBacklog sprintBacklog;

    /**
     * Tâches techniques associées à cette User Story
     */
    @OneToMany(mappedBy = "userStory", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Task> tasks = new ArrayList<>();

    /**
     * Dépendances vers d'autres User Stories
     * (stories qui doivent être complétées avant celle-ci)
     */
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "user_story_dependencies",
            joinColumns = @JoinColumn(name = "user_story_id"),
            inverseJoinColumns = @JoinColumn(name = "depends_on_id")
    )
    private List<UserStory> dependencies = new ArrayList<>();

    // ===== CONSTRUCTEURS =====

    /**
     * Constructeur avec UserStoryDescription
     */
    public UserStory(String title, UserStoryDescription description, Integer storyPoints) {
        super(title, WorkItemStatus.TODO);
        this.description = description;
        this.storyPoints = storyPoints;
        this.priority = 0;
        this.tasks = new ArrayList<>();
        this.dependencies = new ArrayList<>();
    }

    /**
     * Constructeur avec paramètres individuels
     */
    public UserStory(String title, String role, String action, String purpose, Integer storyPoints) {
        super(title, WorkItemStatus.TODO);
        this.description = new UserStoryDescription(role, action, purpose);
        this.storyPoints = storyPoints;
        this.priority = 0;
        this.tasks = new ArrayList<>();
        this.dependencies = new ArrayList<>();
    }

    /**
     * Constructeur complet pour les tests ou migrations
     */
    public UserStory(String title, String role, String action, String purpose,
                     Integer storyPoints, Integer businessValue, Integer urgency,
                     Integer timeCriticality, Integer riskReduction) {
        super(title, WorkItemStatus.TODO);
        this.description = new UserStoryDescription(role, action, purpose);
        this.storyPoints = storyPoints;
        this.businessValue = businessValue;
        this.urgency = urgency;
        this.timeCriticality = timeCriticality;
        this.riskReduction = riskReduction;
        this.priority = 0;
        this.tasks = new ArrayList<>();
        this.dependencies = new ArrayList<>();
    }

    // ===== MÉTHODES DE COHÉRENCE DES RELATIONS =====

    /**
     * Ajoute une tâche à la User Story
     */
    public void addTask(Task task) {
        if (!tasks.contains(task)) {
            tasks.add(task);
            task.setUserStory(this);
        }
    }

    /**
     * Retire une tâche de la User Story
     */
    public void removeTask(Task task) {
        tasks.remove(task);
        task.setUserStory(null);
    }

    /**
     * Définit l'Epic de la User Story
     */
    public void setEpic(Epic epic) {
        if (this.epic != null) {
            this.epic.getUserStories().remove(this);
        }
        this.epic = epic;
        if (epic != null && !epic.getUserStories().contains(this)) {
            epic.getUserStories().add(this);
        }
    }

    /**
     * Ajoute une dépendance (cette story dépend d'une autre)
     */
    public void addDependency(UserStory dependency) {
        if (!dependencies.contains(dependency)) {
            dependencies.add(dependency);
        }
    }

    /**
     * Retire une dépendance
     */
    public void removeDependency(UserStory dependency) {
        dependencies.remove(dependency);
    }

    // ===== MÉTHODES MÉTIER =====

    /**
     * Calcule le pourcentage de complétion basé sur les tâches
     */
    public double calculateProgress() {
        if (tasks.isEmpty()) {
            return 0.0;
        }
        long completedTasks = tasks.stream()
                .filter(task -> task.getStatus() == WorkItemStatus.DONE)
                .count();
        return (completedTasks * 100.0) / tasks.size();
    }

    /**
     * Vérifie si toutes les tâches sont complétées
     */
    public boolean areAllTasksCompleted() {
        return !tasks.isEmpty() && tasks.stream()
                .allMatch(task -> task.getStatus() == WorkItemStatus.DONE);
    }

    /**
     * Obtient la description formatée complète
     */
    public String getFormattedDescription() {
        return description != null ? description.toString() : "";
    }

    /**
     * Obtient la description courte (sans le "afin de")
     */
    public String getShortDescription() {
        return description != null ? description.toShortString() : "";
    }

    /**
     * Vérifie si la User Story est valide (description complète)
     */
    public boolean isValid() {
        return description != null && description.isValid();
    }

    /**
     * Vérifie si la User Story est assignée à un sprint
     */
    public boolean isInSprint() {
        return sprintBacklog != null;
    }

    /**
     * Obtient le nombre total d'heures estimées (somme des tâches)
     */
    public int getTotalEstimatedHours() {
        return tasks.stream()
                .mapToInt(Task::getEstimatedHours)
                .sum();
    }

    /**
     * Vérifie si toutes les dépendances sont complétées
     */
    public boolean areDependenciesCompleted() {
        return dependencies.stream()
                .allMatch(dep -> dep.getStatus() == WorkItemStatus.DONE);
    }

    /**
     * Vérifie si la User Story peut être démarrée
     */
    public boolean canBeStarted() {
        return getStatus() == WorkItemStatus.TODO && areDependenciesCompleted();
    }

    // ===== MÉTHODES STANDARD =====

    @Override
    public String toString() {
        return String.format("UserStory{id=%d, title='%s', storyPoints=%d, priority=%d, status=%s}",
                id, getTitle(), storyPoints, priority, getStatus());
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