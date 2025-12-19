package com.Agile.demo.execution.workflow;

import com.Agile.demo.execution.repositories.SprintBacklogRepository;
import com.Agile.demo.execution.repositories.TaskRepository;
import com.Agile.demo.model.SprintBacklog;
import com.Agile.demo.model.SprintStatus;
import com.Agile.demo.planning.repository.ProjectRepository;
import com.Agile.demo.planning.repository.UserStoryRepository;
import com.Agile.demo.model.Task;
import com.Agile.demo.model.UserStory;
import com.Agile.demo.model.WorkItemStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

/**
 * Service avancé pour la gestion du workflow des sprints
 * Gère le cycle de vie complet et les métriques des sprints
 */
@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class SprintWorkflowService {

    private final SprintBacklogRepository sprintBacklogRepository;
    private final UserStoryRepository userStoryRepository;
    private final TaskRepository taskRepository;
    private final ProjectRepository projectRepository;

    /**
     * Démarre un sprint avec validation complète des règles métier
     *
     * @param sprintId ID du sprint à démarrer
     * @throws IllegalStateException si le sprint ne peut pas être démarré
     */
    public void startSprint(Long sprintId) {
        log.info("Tentative de démarrage du sprint ID: {}", sprintId);

        SprintBacklog sprint = sprintBacklogRepository.findById(sprintId)
                .orElseThrow(() -> new IllegalArgumentException("Sprint non trouvé avec l'ID: " + sprintId));

        // RÈGLE MÉTIER: Le sprint doit être en statut PLANNED
        if (sprint.getSprintStatus() != SprintStatus.PLANNED) {
            throw new IllegalStateException(
                    String.format("Le sprint doit être en statut PLANNED pour démarrer. Statut actuel: %s",
                            sprint.getSprintStatus())
            );
        }

        // RÈGLE MÉTIER: Vérifier qu'il n'y a pas déjà un sprint actif pour ce projet
        long activeSprintsCount = sprintBacklogRepository.countByProjectIdAndSprintStatus(
                sprint.getProject().getId(), SprintStatus.ACTIVE);

        if (activeSprintsCount > 0) {
            throw new IllegalStateException(
                    "Un sprint est déjà actif pour ce projet. Terminez-le avant d'en démarrer un nouveau."
            );
        }

        // RÈGLE MÉTIER: Vérifier que le sprint contient au moins une User Story
        if (sprint.getUserStories().isEmpty()) {
            throw new IllegalStateException("Le sprint doit contenir au moins une User Story pour démarrer");
        }

        // RÈGLE MÉTIER: Vérifier les dépendances des User Stories
        validateUserStoriesDependencies(sprint);

        // Démarrer le sprint
        sprint.startSprint();
        sprintBacklogRepository.save(sprint);

        log.info("Sprint ID: {} démarré avec succès. {} User Stories incluses.",
                sprintId, sprint.getUserStories().size());
    }

    /**
     * Termine un sprint avec calcul automatique des métriques
     *
     * @param sprintId ID du sprint à terminer
     */
    public void completeSprint(Long sprintId) {
        log.info("Tentative de complétion du sprint ID: {}", sprintId);

        SprintBacklog sprint = sprintBacklogRepository.findById(sprintId)
                .orElseThrow(() -> new IllegalArgumentException("Sprint non trouvé avec l'ID: " + sprintId));

        // RÈGLE MÉTIER: Le sprint doit être ACTIVE pour être terminé
        if (sprint.getSprintStatus() != SprintStatus.ACTIVE) {
            throw new IllegalStateException(
                    String.format("Le sprint doit être actif pour être terminé. Statut actuel: %s",
                            sprint.getSprintStatus())
            );
        }

        // Calculer les métriques finales avant de terminer
        SprintMetrics finalMetrics = getSprintMetrics(sprintId);
        log.info("Métriques finales du sprint - Vélocité: {}, Progression: {}%, Stories complétées: {}/{}",
                finalMetrics.velocity(),
                String.format("%.2f", finalMetrics.progressPercentage()),
                finalMetrics.completedStories(),
                finalMetrics.totalStories());

        // Terminer le sprint
        sprint.completeSprint();
        sprintBacklogRepository.save(sprint);

        // Déplacer les User Stories non terminées vers le Product Backlog
        moveIncompletedUserStoriesToBacklog(sprint);

        log.info("Sprint ID: {} terminé avec succès. Vélocité finale: {}",
                sprintId, finalMetrics.velocity());
    }

    /**
     * Ajoute une User Story au sprint avec validation des dépendances
     *
     * @param sprintId ID du sprint
     * @param userStoryId ID de la User Story à ajouter
     */
    public void addUserStoryToSprint(Long sprintId, Long userStoryId) {
        log.info("Ajout de la User Story ID: {} au sprint ID: {}", userStoryId, sprintId);

        SprintBacklog sprint = sprintBacklogRepository.findById(sprintId)
                .orElseThrow(() -> new IllegalArgumentException("Sprint non trouvé avec l'ID: " + sprintId));

        UserStory userStory = userStoryRepository.findById(userStoryId)
                .orElseThrow(() -> new IllegalArgumentException("User Story non trouvée avec l'ID: " + userStoryId));

        // RÈGLE MÉTIER: Ne peut pas modifier un sprint terminé ou annulé
        if (sprint.getSprintStatus() == SprintStatus.COMPLETED ||
                sprint.getSprintStatus() == SprintStatus.CANCELLED) {
            throw new IllegalStateException(
                    "Impossible d'ajouter des User Stories à un sprint terminé ou annulé"
            );
        }

        // RÈGLE MÉTIER: La User Story doit appartenir au même projet
        if (!userStory.getProductBacklog().getProject().getId().equals(sprint.getProject().getId())) {
            throw new IllegalStateException(
                    "La User Story n'appartient pas au même projet que le sprint"
            );
        }

        // RÈGLE MÉTIER: Vérifier que les dépendances sont satisfaites
        if (!userStory.areDependenciesCompleted()) {
            List<UserStory> incompleteDependencies = userStory.getDependencies().stream()
                    .filter(dep -> dep.getStatus() != WorkItemStatus.DONE)
                    .toList();

            throw new IllegalStateException(
                    String.format("La User Story a %d dépendance(s) non complétée(s). " +
                                    "Les dépendances doivent être terminées avant d'ajouter cette story au sprint.",
                            incompleteDependencies.size())
            );
        }

        // RÈGLE MÉTIER: La User Story ne doit pas être déjà dans un autre sprint actif
        if (userStory.getSprintBacklog() != null &&
                userStory.getSprintBacklog().getSprintStatus() == SprintStatus.ACTIVE) {
            throw new IllegalStateException(
                    "La User Story est déjà assignée à un sprint actif"
            );
        }

        // Ajouter la User Story au sprint
        sprint.addUserStory(userStory);

        // Associer également les tâches de la User Story au sprint
        for (Task task : userStory.getTasks()) {
            task.setSprintBacklog(sprint);
        }

        sprintBacklogRepository.save(sprint);

        log.info("User Story '{}' ajoutée au sprint '{}' avec succès",
                userStory.getTitle(), sprint.getName());
    }

    /**
     * Retire une User Story du sprint
     *
     * @param sprintId ID du sprint
     * @param userStoryId ID de la User Story à retirer
     */
    public void removeUserStoryFromSprint(Long sprintId, Long userStoryId) {
        log.info("Retrait de la User Story ID: {} du sprint ID: {}", userStoryId, sprintId);

        SprintBacklog sprint = sprintBacklogRepository.findById(sprintId)
                .orElseThrow(() -> new IllegalArgumentException("Sprint non trouvé avec l'ID: " + sprintId));

        UserStory userStory = userStoryRepository.findById(userStoryId)
                .orElseThrow(() -> new IllegalArgumentException("User Story non trouvée avec l'ID: " + userStoryId));

        // RÈGLE MÉTIER: Ne peut pas modifier un sprint terminé
        if (sprint.getSprintStatus() == SprintStatus.COMPLETED) {
            throw new IllegalStateException(
                    "Impossible de retirer des User Stories d'un sprint terminé"
            );
        }

        // RÈGLE MÉTIER: Ne peut pas retirer une story avec des tâches en cours
        long tasksInProgress = userStory.getTasks().stream()
                .filter(task -> task.getStatus() == WorkItemStatus.IN_PROGRESS)
                .count();

        if (tasksInProgress > 0) {
            throw new IllegalStateException(
                    String.format("La User Story a %d tâche(s) en cours. " +
                                    "Terminez ou annulez ces tâches avant de retirer la story du sprint.",
                            tasksInProgress)
            );
        }

        // Retirer la User Story du sprint
        sprint.removeUserStory(userStory);

        // Dissocier les tâches du sprint
        for (Task task : userStory.getTasks()) {
            task.setSprintBacklog(null);
        }

        sprintBacklogRepository.save(sprint);

        log.info("User Story '{}' retirée du sprint '{}' avec succès",
                userStory.getTitle(), sprint.getName());
    }

    /**
     * Calcule la vélocité du sprint (story points complétés)
     *
     * @param sprintId ID du sprint
     * @return Vélocité (somme des story points des stories DONE)
     */
    @Transactional(readOnly = true)
    public int calculateVelocity(Long sprintId) {
        SprintBacklog sprint = sprintBacklogRepository.findById(sprintId)
                .orElseThrow(() -> new IllegalArgumentException("Sprint non trouvé avec l'ID: " + sprintId));

        return sprint.calculateVelocity();
    }

    /**
     * Récupère les métriques complètes du sprint
     *
     * @param sprintId ID du sprint
     * @return Objet SprintMetrics avec toutes les métriques
     */
    @Transactional(readOnly = true)
    public SprintMetrics getSprintMetrics(Long sprintId) {
        SprintBacklog sprint = sprintBacklogRepository.findById(sprintId)
                .orElseThrow(() -> new IllegalArgumentException("Sprint non trouvé avec l'ID: " + sprintId));

        // Métriques de base du sprint
        int velocity = sprint.calculateVelocity();
        double progressPercentage = sprint.calculateProgress();
        int totalStoryPoints = sprint.getTotalStoryPoints();
        int remainingStoryPoints = sprint.getRemainingStoryPoints();
        int completedStoryPoints = velocity;

        // Comptage des stories par statut
        long totalStories = sprint.getUserStories().size();
        long completedStories = sprint.getUserStories().stream()
                .filter(us -> us.getStatus() == WorkItemStatus.DONE)
                .count();
        long inProgressStories = sprint.getUserStories().stream()
                .filter(us -> us.getStatus() == WorkItemStatus.IN_PROGRESS)
                .count();
        long todoStories = sprint.getUserStories().stream()
                .filter(us -> us.getStatus() == WorkItemStatus.TODO)
                .count();

        // Métriques des tâches
        List<Task> allTasks = taskRepository.findBySprintBacklogId(sprintId);
        long totalTasks = allTasks.size();
        long completedTasks = allTasks.stream()
                .filter(task -> task.getStatus() == WorkItemStatus.DONE)
                .count();
        long inProgressTasks = allTasks.stream()
                .filter(task -> task.getStatus() == WorkItemStatus.IN_PROGRESS)
                .count();

        // Métriques temporelles
        long sprintDurationDays = sprint.getSprintDuration();
        long daysElapsed = calculateDaysElapsed(sprint);
        long daysRemaining = Math.max(0, sprintDurationDays - daysElapsed);

        // Métriques d'heures
        int totalEstimatedHours = allTasks.stream()
                .mapToInt(Task::getEstimatedHours)
                .sum();
        int totalActualHours = allTasks.stream()
                .mapToInt(Task::getActualHours)
                .sum();
        int remainingHours = allTasks.stream()
                .mapToInt(Task::getRemainingHours)
                .sum();

        // Calcul de la vélocité moyenne attendue vs réelle
        double expectedVelocityRate = sprintDurationDays > 0
                ? (double) totalStoryPoints / sprintDurationDays
                : 0.0;
        double actualVelocityRate = daysElapsed > 0
                ? (double) completedStoryPoints / daysElapsed
                : 0.0;

        // Prédiction de complétion
        boolean onTrack = actualVelocityRate >= expectedVelocityRate || daysElapsed == 0;

        return new SprintMetrics(
                velocity,
                progressPercentage,
                totalStoryPoints,
                completedStoryPoints,
                remainingStoryPoints,
                (int) totalStories,
                (int) completedStories,
                (int) inProgressStories,
                (int) todoStories,
                (int) totalTasks,
                (int) completedTasks,
                (int) inProgressTasks,
                totalEstimatedHours,
                totalActualHours,
                remainingHours,
                sprintDurationDays,
                daysElapsed,
                daysRemaining,
                expectedVelocityRate,
                actualVelocityRate,
                onTrack,
                sprint.getSprintStatus()
        );
    }

    // ===== MÉTHODES PRIVÉES D'AIDE =====

    /**
     * Valide que toutes les dépendances des User Stories du sprint sont satisfaites
     */
    private void validateUserStoriesDependencies(SprintBacklog sprint) {
        List<UserStory> storiesWithUnmetDependencies = sprint.getUserStories().stream()
                .filter(us -> !us.areDependenciesCompleted())
                .toList();

        if (!storiesWithUnmetDependencies.isEmpty()) {
            String storyTitles = storiesWithUnmetDependencies.stream()
                    .map(UserStory::getTitle)
                    .reduce((a, b) -> a + ", " + b)
                    .orElse("");

            throw new IllegalStateException(
                    String.format("Les User Stories suivantes ont des dépendances non satisfaites: %s. " +
                            "Complétez les dépendances avant de démarrer le sprint.", storyTitles)
            );
        }
    }

    /**
     * Déplace les User Stories non terminées vers le Product Backlog
     */
    private void moveIncompletedUserStoriesToBacklog(SprintBacklog sprint) {
        List<UserStory> incompletedStories = sprint.getUserStories().stream()
                .filter(us -> us.getStatus() != WorkItemStatus.DONE)
                .toList();

        if (!incompletedStories.isEmpty()) {
            log.info("Déplacement de {} User Stories non terminées vers le Product Backlog",
                    incompletedStories.size());

            for (UserStory story : incompletedStories) {
                story.setSprintBacklog(null);
                // Les tâches restent associées à la story mais ne sont plus dans le sprint
                for (Task task : story.getTasks()) {
                    task.setSprintBacklog(null);
                }
            }
        }
    }

    /**
     * Calcule le nombre de jours écoulés depuis le début du sprint
     */
    private long calculateDaysElapsed(SprintBacklog sprint) {
        if (sprint.getSprintStatus() == SprintStatus.PLANNED) {
            return 0;
        }

        LocalDate now = LocalDate.now();
        LocalDate startDate = sprint.getStartDate();
        LocalDate endDate = sprint.getEndDate();

        if (now.isBefore(startDate)) {
            return 0;
        } else if (now.isAfter(endDate)) {
            return ChronoUnit.DAYS.between(startDate, endDate);
        } else {
            return ChronoUnit.DAYS.between(startDate, now);
        }
    }

    /**
     * Record pour les métriques complètes du sprint
     */
    public record SprintMetrics(
            // Métriques de story points
            int velocity,
            double progressPercentage,
            int totalStoryPoints,
            int completedStoryPoints,
            int remainingStoryPoints,

            // Métriques de User Stories
            int totalStories,
            int completedStories,
            int inProgressStories,
            int todoStories,

            // Métriques de tâches
            int totalTasks,
            int completedTasks,
            int inProgressTasks,

            // Métriques d'heures
            int totalEstimatedHours,
            int totalActualHours,
            int remainingHours,

            // Métriques temporelles
            long sprintDurationDays,
            long daysElapsed,
            long daysRemaining,

            // Métriques de vélocité
            double expectedVelocityRate,
            double actualVelocityRate,
            boolean onTrack,

            // Statut
            SprintStatus status
    ) {}
}