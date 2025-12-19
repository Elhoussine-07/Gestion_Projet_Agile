package com.Agile.demo.execution.services;

import com.Agile.demo.execution.repositories.SprintBacklogRepository;
import com.Agile.demo.model.SprintBacklog;
import com.Agile.demo.planning.repository.ProjectRepository;
import com.Agile.demo.model.Project;
import com.Agile.demo.model.SprintStatus;
import com.Agile.demo.model.UserStory;
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
    public SprintBacklog createSprint(Long projectId, Integer SprintNumber,
                                      LocalDate startDate, LocalDate endDate, String goal) {
        // Vérifier que le projet existe
        Project project = projectRepository.findById(projectId)
                .orElseThrow(()-> new IllegalArgumentException("Projet non trouvé avec l'ID: " + projectId));

        // Vérifier qu'il n'y a pas déjà un sprint avec ce numéro
        if (sprintBacklogRepository.existsByProjectIdAndSprintNumber(projectId, SprintNumber)) {
            throw new IllegalStateException("Un sprint avec le numéro " + SprintNumber + " existe déjà pour ce projet");
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
        String sprintName = "Sprint " + SprintNumber;
        SprintBacklog sprint = new SprintBacklog(sprintName, SprintNumber, startDate, endDate, goal);
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
    public SprintBacklog getSprintById(Long SprintNumber) {
        return sprintBacklogRepository.findById(SprintNumber)
                .orElseThrow(() -> new IllegalArgumentException("Sprint non trouvé avec le Numéro: " + SprintNumber));
    }

    /**
     * Met à jour un sprint
     */
    public SprintBacklog updateSprint(Long SprintNumber, LocalDate startDate,
                                      LocalDate endDate, String goal) {
        SprintBacklog sprint = getSprintById(SprintNumber);

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

    // Méthodes à ajouter dans SprintService

    /**
     * Récupère le dernier sprint d'un projet
     */
    @Transactional(readOnly = true)
    public SprintBacklog getLastSprint(Long projectId) {
        return sprintBacklogRepository.findTopByProjectIdOrderBySprintNumberDesc(projectId)
                .orElseThrow(() -> new IllegalStateException("Aucun sprint trouvé pour ce projet"));
    }

    /**
     * Récupère les sprints par statut
     */
    @Transactional(readOnly = true)
    public List<SprintBacklog> getSprintsByStatus(Long projectId, SprintStatus status) {
        return sprintBacklogRepository.findByProjectIdAndSprintStatus(projectId, status);
    }

    /**
     * Vérifie si un sprint peut être démarré
     */
    @Transactional(readOnly = true)
    public boolean canStartSprint(Long sprintId) {
        SprintBacklog sprint = getSprintById(sprintId);

        if (sprint.getSprintStatus() != SprintStatus.PLANNED) {
            return false;
        }

        // Vérifier qu'il y a au moins une user story
        if (sprint.getUserStories().isEmpty()) {
            return false;
        }

        // Vérifier qu'il n'y a pas déjà un sprint actif
        long activeSprintsCount = sprintBacklogRepository.countByProjectIdAndSprintStatus(
                sprint.getProject().getId(), SprintStatus.ACTIVE);

        return activeSprintsCount == 0;
    }

    /**
     * Récupère les sprints entre deux dates
     */
    @Transactional(readOnly = true)
    public List<SprintBacklog> getSprintsBetweenDates(Long projectId, LocalDate startDate, LocalDate endDate) {
        return sprintBacklogRepository.findByProjectIdAndStartDateBetween(projectId, startDate, endDate);
    }

    /**
     * Déplace une user story d'un sprint à un autre
     */
    public void moveUserStoryBetweenSprints(Long fromSprintId, Long toSprintId, Long userStoryId) {
        SprintBacklog fromSprint = getSprintById(fromSprintId);
        SprintBacklog toSprint = getSprintById(toSprintId);

        UserStory userStory = fromSprint.getUserStories().stream()
                .filter(us -> us.getId().equals(userStoryId))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("User Story non trouvée dans le sprint source"));

        if (toSprint.getSprintStatus() == SprintStatus.COMPLETED ||
                toSprint.getSprintStatus() == SprintStatus.CANCELLED) {
            throw new IllegalStateException("Impossible d'ajouter des stories à un sprint terminé ou annulé");
        }

        fromSprint.removeUserStory(userStory);
        toSprint.addUserStory(userStory);

        sprintBacklogRepository.save(fromSprint);
        sprintBacklogRepository.save(toSprint);
    }

    /**
     * Récupère le burndown chart d'un sprint
     */
    @Transactional(readOnly = true)
    public SprintBurndown getSprintBurndown(Long sprintId) {
        SprintBacklog sprint = getSprintById(sprintId);

        int totalStoryPoints = sprint.getTotalStoryPoints();
        int remainingStoryPoints = sprint.getRemainingStoryPoints();
        int completedStoryPoints = totalStoryPoints - remainingStoryPoints;

        long totalDays = sprint.getSprintDuration();
        long elapsedDays = LocalDate.now().isBefore(sprint.getStartDate()) ? 0 :
                LocalDate.now().isAfter(sprint.getEndDate()) ? totalDays :
                        java.time.temporal.ChronoUnit.DAYS.between(sprint.getStartDate(), LocalDate.now());

        double idealBurnRate = totalDays > 0 ? (double) totalStoryPoints / totalDays : 0;
        int idealRemaining = (int) (totalStoryPoints - (idealBurnRate * elapsedDays));

        return new SprintBurndown(
                totalStoryPoints,
                remainingStoryPoints,
                completedStoryPoints,
                idealRemaining,
                elapsedDays,
                totalDays
        );
    }

    /**
     * Récupère les sprints avec des user stories incomplètes
     */
    @Transactional(readOnly = true)
    public List<SprintBacklog> getSprintsWithIncompleteStories(Long projectId) {
        return sprintBacklogRepository.findSprintsWithIncompleteStories(projectId);
    }

    /**
     * Clone un sprint (pour réutiliser la configuration)
     */
    public SprintBacklog cloneSprint(Long sprintId, Integer newSprintNumber,
                                     LocalDate newStartDate, LocalDate newEndDate) {
        SprintBacklog originalSprint = getSprintById(sprintId);

        if (sprintBacklogRepository.existsByProjectIdAndSprintNumber(
                originalSprint.getProject().getId(), newSprintNumber)) {
            throw new IllegalStateException("Un sprint avec ce numéro existe déjà");
        }

        SprintBacklog newSprint = new SprintBacklog(
                "Sprint " + newSprintNumber,
                newSprintNumber,
                newStartDate,
                newEndDate,
                originalSprint.getGoal()
        );
        newSprint.setProject(originalSprint.getProject());

        return sprintBacklogRepository.save(newSprint);
    }

    /**
     * Classe pour le burndown chart
     */
    public record SprintBurndown(
            int totalStoryPoints,
            int remainingStoryPoints,
            int completedStoryPoints,
            int idealRemaining,
            long elapsedDays,
            long totalDays
    ) {}

// Méthodes à ajouter dans SprintBacklogRepository :
// Optional<SprintBacklog> findTopByProjectIdOrderBySprintNumberDesc(Long projectId);
// List<SprintBacklog> findByProjectIdAndStartDateBetween(Long projectId, LocalDate startDate, LocalDate endDate);
// @Query("SELECT s FROM SprintBacklog s WHERE s.project.id = :projectId AND EXISTS (SELECT us FROM s.userStories us WHERE us.status != 'DONE')")
// List<SprintBacklog> findSprintsWithIncompleteStories(@Param("projectId") Long projectId);
}