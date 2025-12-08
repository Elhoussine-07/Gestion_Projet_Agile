package com.Agile.demo.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;

/**
 * Classe abstraite représentant un élément de travail (Work Item)
 * Parente commune de UserStory et Task
 */
@MappedSuperclass
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public abstract class AbstractWorkItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    protected Long id;

    /**
     * Titre/nom de l'élément de travail
     */
    @Column(nullable = false, length = 255)
    protected String title;

    /**
     * Statut actuel de l'élément
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    protected WorkItemStatus status = WorkItemStatus.TODO;

    /**
     * Date de création
     */
    @Column(name = "created_date", nullable = false, updatable = false)
    protected LocalDateTime createdDate;

    /**
     * Date de dernière mise à jour
     */
    @Column(name = "updated_date")
    protected LocalDateTime updatedDate;

    /**
     * Constructeur avec titre et statut
     */
    public AbstractWorkItem(String title, WorkItemStatus status) {
        this.title = title;
        this.status = status;
        this.createdDate = LocalDateTime.now();
        this.updatedDate = LocalDateTime.now();
    }

    /**
     * Constructeur avec titre seulement (statut par défaut TODO)
     */
    public AbstractWorkItem(String title) {
        this(title, WorkItemStatus.TODO);
    }

    // ===== MÉTHODES LIFECYCLE JPA =====

    @PrePersist
    protected void onCreate() {
        this.createdDate = LocalDateTime.now();
        this.updatedDate = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedDate = LocalDateTime.now();
    }

    // ===== MÉTHODES MÉTIER =====

    /**
     * Met à jour le statut de l'élément
     */
    public void updateStatus(WorkItemStatus newStatus) {
        this.status = newStatus;
        this.updatedDate = LocalDateTime.now();
    }

    /**
     * Vérifie si l'élément est terminé
     */
    public boolean isDone() {
        return this.status == WorkItemStatus.DONE;
    }

    /**
     * Vérifie si l'élément est en cours
     */
    public boolean isInProgress() {
        return this.status == WorkItemStatus.IN_PROGRESS;
    }

    /**
     * Vérifie si l'élément est bloqué
     */
    public boolean isBlocked() {
        return this.status == WorkItemStatus.BLOCKED;
    }

    /**
     * Démarre l'élément (passe en IN_PROGRESS)
     */
    public void start() {
        if (this.status == WorkItemStatus.TODO) {
            updateStatus(WorkItemStatus.IN_PROGRESS);
        }
    }

    /**
     * Termine l'élément (passe en DONE)
     */
    public void complete() {
        updateStatus(WorkItemStatus.DONE);
    }

    /**
     * Bloque l'élément
     */
    public void block() {
        updateStatus(WorkItemStatus.BLOCKED);
    }

    /**
     * Calcule le pourcentage de progression
     * Par défaut, basé sur le statut
     * Peut être surchargé par les classes filles
     */
    public int getProgress() {
        switch (this.status) {
            case TODO:
                return 0;
            case IN_PROGRESS:
            case IN_REVIEW:
            case TESTING:
                return 50;
            case DONE:
                return 100;
            case BLOCKED:
                return 0;
            default:
                return 0;
        }
    }

    // ===== MÉTHODES ABSTRAITES (à implémenter par les classes filles) =====

    public abstract String getDescription();

    // ===== MÉTHODES STANDARD =====

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof AbstractWorkItem)) return false;
        AbstractWorkItem that = (AbstractWorkItem) o;
        return id != null && id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }

    @Override
    public String toString() {
        return String.format("%s{id=%d, title='%s', status=%s}",
                getClass().getSimpleName(), id, title, status);
    }
}