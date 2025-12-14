package com.Agile.demo.execution.services;

import com.Agile.demo.model.*;
import com.Agile.demo.execution.repositories.SprintBacklogRepository;
import com.Agile.demo.execution.repositories.ProjectRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class SprintService {

    private final SprintBacklogRepository sprintBacklogRepository;
    private final ProjectRepository projectRepository;

    /**
     * Crée un nouveau sprint pour un projet
     */
    public SprintBacklog createSprint(Long projectId, Integer sprintNumber,
                                      LocalDate startDate, LocalDate endDate, String goal) {
        // Vérifier que le projet existe
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new IllegalArgumentException("Projet non trouvé avec l'ID: " + projectId));

        // Vérifier qu'il n'y a pas déjà un sprint avec ce numéro
        if (sprintBacklogRepository.existsByProjectIdAndSprintNumber(projectId, sprintNumber)) {
            throw new IllegalStateException("Un sprint avec le numéro " + sprintNumber + " existe déjà pour ce projet");
        }

        // Vérifier qu'il n'y a pas déjà un sprint actif
        long activeSprintsCount = sprintBacklogRepository.countByProjectIdAndSprintStatus(
                projectId, SprintStatus.ACTIVE);
        if (activeSprintsCount > 0) {
            throw new IllegalStateException("Un sprint est déjà actif pour ce projet");
        }

        // Valider les dates
        if (endDate.isBefore(startDate)) {
            throw new IllegalArgumentException("La date de fin doit être après la date de début");
        }

        // Créer le sprint
        String sprintName = "Sprint " + sprintNumber;
        SprintBacklog sprint = new SprintBacklog(sprintName, sprintNumber, startDate, endDate, goal);
        sprint.setProject(project);

        return sprintBacklogRepository.save(sprint);
    }

    /**
     * Récupère tous les sprints d'un projet
     */
    @Transactional(readOnly = true)
    public List<SprintBacklog> getSprintsByProject(Long projectId) {
        return sprintBacklogRepository.findByProjectSprintNumber(projectId);
    }

    /**
     * Récupère le sprint actif d'un projet
     */
    @Transactional(readOnly = true)
    public SprintBacklog getActiveSprint(Long projectId) {
        List<SprintBacklog> active = sprintBacklogRepository.findByProjectIdAndSprintStatus(projectId, SprintStatus.ACTIVE);
        return active.stream()
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("Aucun sprint actif pour ce projet"));
    }

    /**
     * Récupère un sprint par son ID
     */
    @Transactional(readOnly = true)
    public SprintBacklog getSprintById(Long sprintId) {
        return sprintBacklogRepository.findById(sprintId)
                .orElseThrow(() -> new IllegalArgumentException("Sprint non trouvé avec l'ID: " + sprintId));
    }

    /**
     * Met à jour un sprint
     */
    public SprintBacklog updateSprint(Long sprintId, LocalDate startDate,
                                      LocalDate endDate, String goal) {
        SprintBacklog sprint = getSprintById(sprintId);

        // Ne peut pas modifier un sprint terminé ou annulé
        if (sprint.getSprintStatus() == SprintStatus.COMPLETED ||
                sprint.getSprintStatus() == SprintStatus.CANCELLED) {
            throw new IllegalStateException("Impossible de modifier un sprint terminé ou annulé");
        }

        if (startDate != null && endDate != null && endDate.isBefore(startDate)) {
            throw new IllegalArgumentException("La date de fin doit être après la date de début");
        }

        if (startDate != null) sprint.setStartDate(startDate);
        if (endDate != null) sprint.setEndDate(endDate);
        if (goal != null) sprint.setGoal(goal);

        return sprintBacklogRepository.save(sprint);
    }

    /**
     * Démarre un sprint
     */
    public SprintBacklog startSprint(Long sprintId) {
        SprintBacklog sprint = getSprintById(sprintId);

        // Vérifier qu'il n'y a pas déjà un sprint actif
        long activeSprintsCount = sprintBacklogRepository.countByProjectIdAndSprintStatus(
                sprint.getProject().getId(), SprintStatus.ACTIVE);
        if (activeSprintsCount > 0) {
            throw new IllegalStateException("Un sprint est déjà actif pour ce projet");
        }

        sprint.startSprint();
        return sprintBacklogRepository.save(sprint);
    }

    /**
     * Termine un sprint
     */
    public SprintBacklog completeSprint(Long sprintId) {
        SprintBacklog sprint = getSprintById(sprintId);
        sprint.completeSprint();
        return sprintBacklogRepository.save(sprint);
    }

    /**
     * Annule un sprint
     */
    public SprintBacklog cancelSprint(Long sprintId) {
        SprintBacklog sprint = getSprintById(sprintId);
        sprint.cancelSprint();
        return sprintBacklogRepository.save(sprint);
    }

    /**
     * Supprime un sprint (seulement si planifié)
     */
    public void deleteSprint(Long sprintId) {
        SprintBacklog sprint = getSprintById(sprintId);

        if (sprint.getSprintStatus() != SprintStatus.PLANNED) {
            throw new IllegalStateException("Seuls les sprints planifiés peuvent être supprimés");
        }

        sprintBacklogRepository.delete(sprint);
    }

    /**
     * Ajoute une User Story au sprint
     */
    public SprintBacklog addUserStoryToSprint(Long sprintId, UserStory userStory) {
        SprintBacklog sprint = getSprintById(sprintId);

        if (sprint.getSprintStatus() == SprintStatus.COMPLETED ||
                sprint.getSprintStatus() == SprintStatus.CANCELLED) {
            throw new IllegalStateException("Impossible d'ajouter des stories à un sprint terminé ou annulé");
        }

        sprint.addUserStory(userStory);
        return sprintBacklogRepository.save(sprint);
    }

    /**
     * Retire une User Story du sprint
     */
    public SprintBacklog removeUserStoryFromSprint(Long sprintId, UserStory userStory) {
        SprintBacklog sprint = getSprintById(sprintId);
        sprint.removeUserStory(userStory);
        return sprintBacklogRepository.save(sprint);
    }

    /**
     * Calcule les métriques du sprint
     */
    @Transactional(readOnly = true)
    public SprintMetrics getSprintMetrics(Long sprintId) {
        SprintBacklog sprint = getSprintById(sprintId);

        int velocity = sprint.calculateVelocity();
        double progress = sprint.calculateProgress();
        int totalStoryPoints = sprint.getTotalStoryPoints();
        int remainingStoryPoints = sprint.getRemainingStoryPoints();
        long duration = sprint.getSprintDuration();

        return new SprintMetrics(velocity, progress, totalStoryPoints,
                remainingStoryPoints, duration);
    }

    /**
     * Classe interne pour les métriques du sprint
     */
    public record SprintMetrics(
            int velocity,
            double progress,
            int totalStoryPoints,
            int remainingStoryPoints,
            long durationInDays
    ) {}
}