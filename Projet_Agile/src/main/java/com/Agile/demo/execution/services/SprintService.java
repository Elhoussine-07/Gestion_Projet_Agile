package com.Agile.demo.execution.services;

import com.Agile.demo.execution.repositories.SprintBacklogRepository;
import com.Agile.demo.execution.repositories.TaskRepository;
import com.Agile.demo.planning.repository.ProjectRepository;
import com.Agile.demo.planning.repository.UserStoryRepository;
import com.Agile.demo.model.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class SprintService {

    private final SprintBacklogRepository sprintBacklogRepository;
    private final ProjectRepository projectRepository;
    private final UserStoryRepository userStoryRepository;
    private final TaskRepository taskRepository;

    public SprintBacklog createSprint(Long projectId, Integer SprintNumber,
                                      LocalDate startDate, LocalDate endDate, String goal,
                                      List<Long> userStoryIds) {

        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new IllegalArgumentException("Projet non trouvé avec l'ID: " + projectId));

        if (sprintBacklogRepository.existsByProjectIdAndSprintNumber(projectId, SprintNumber)) {
            throw new IllegalStateException("Un sprint avec le numéro " + SprintNumber + " existe déjà pour ce projet");
        }

        long activeSprintsCount = sprintBacklogRepository.countByProjectIdAndSprintStatus(
                projectId, SprintStatus.ACTIVE);
        if (activeSprintsCount > 0) {
            throw new IllegalStateException("Un sprint est déjà actif pour ce projet");
        }

        if (endDate.isBefore(startDate)) {
            throw new IllegalArgumentException("La date de fin doit être après la date de début");
        }

        String sprintName = "Sprint " + SprintNumber;
        SprintBacklog sprint = new SprintBacklog(sprintName, SprintNumber, startDate, endDate, goal);
        sprint.setProject(project);

        if (userStoryIds != null && !userStoryIds.isEmpty()) {
            List<UserStory> userStories = userStoryRepository.findAllById(userStoryIds);

            if (userStories.size() != userStoryIds.size()) {
                throw new IllegalArgumentException("Une ou plusieurs User Stories n'ont pas été trouvées");
            }

            userStories.forEach(us -> {
                if (!us.areDependenciesCompleted()) {
                    throw new IllegalStateException(
                            "La User Story '" + us.getTitle() + "' a des dépendances non complétées");
                }
                sprint.addUserStory(us);
            });
        }

        return sprintBacklogRepository.save(sprint);
    }

    @Transactional(readOnly = true)
    public List<SprintBacklog> getSprintsByProject(Long projectId) {
        return sprintBacklogRepository.findByProjectId(projectId);
    }

    @Transactional(readOnly = true)
    public SprintBacklog getActiveSprint(Long projectId) {
        List<SprintBacklog> active = sprintBacklogRepository.findByProjectIdAndSprintStatus(projectId, SprintStatus.ACTIVE);
        return active.stream()
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("Aucun sprint actif pour ce projet"));
    }

    @Transactional(readOnly = true)
    public SprintBacklog getSprintById(Long SprintNumber) {
        return sprintBacklogRepository.findById(SprintNumber)
                .orElseThrow(() -> new IllegalArgumentException("Sprint non trouvé avec le Numéro: " + SprintNumber));
    }

    public SprintBacklog updateSprint(Long SprintNumber, LocalDate startDate,
                                      LocalDate endDate, String goal) {
        SprintBacklog sprint = getSprintById(SprintNumber);

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

    public SprintBacklog startSprint(Long sprintId) {
        SprintBacklog sprint = getSprintById(sprintId);

        if (sprint.getSprintStatus() != SprintStatus.PLANNED) {
            throw new IllegalStateException(
                    String.format("Le sprint doit être en statut PLANNED pour démarrer. Statut actuel: %s",
                            sprint.getSprintStatus())
            );
        }

        long activeSprintsCount = sprintBacklogRepository.countByProjectIdAndSprintStatus(
                sprint.getProject().getId(), SprintStatus.ACTIVE);
        if (activeSprintsCount > 0) {
            throw new IllegalStateException(
                    "Un sprint est déjà actif pour ce projet. Terminez-le avant d'en démarrer un nouveau."
            );
        }

        if (sprint.getUserStories().isEmpty()) {
            throw new IllegalStateException("Le sprint doit contenir au moins une User Story pour démarrer");
        }

        validateUserStoriesDependencies(sprint);

        sprint.startSprint();
        return sprintBacklogRepository.save(sprint);
    }

    public SprintBacklog completeSprint(Long sprintId) {
        SprintBacklog sprint = getSprintById(sprintId);

        if (sprint.getSprintStatus() != SprintStatus.ACTIVE) {
            throw new IllegalStateException(
                    String.format("Le sprint doit être actif pour être terminé. Statut actuel: %s",
                            sprint.getSprintStatus())
            );
        }

        sprint.completeSprint();
        sprintBacklogRepository.save(sprint);

        moveIncompletedUserStoriesToBacklog(sprint);

        return sprint;
    }

    public SprintBacklog cancelSprint(Long sprintId) {
        SprintBacklog sprint = getSprintById(sprintId);
        sprint.cancelSprint();
        return sprintBacklogRepository.save(sprint);
    }

    public void deleteSprint(Long sprintId) {
        SprintBacklog sprint = getSprintById(sprintId);

        if (sprint.getSprintStatus() != SprintStatus.PLANNED) {
            throw new IllegalStateException("Seuls les sprints planifiés peuvent être supprimés");
        }

        sprintBacklogRepository.delete(sprint);
    }

    public SprintBacklog addUserStoryToSprint(Long sprintId, Long userStoryId) {
        SprintBacklog sprint = getSprintById(sprintId);

        UserStory userStory = userStoryRepository.findById(userStoryId)
                .orElseThrow(() -> new IllegalArgumentException("User Story non trouvée avec l'ID: " + userStoryId));

        if (sprint.getSprintStatus() == SprintStatus.COMPLETED ||
                sprint.getSprintStatus() == SprintStatus.CANCELLED) {
            throw new IllegalStateException("Impossible d'ajouter des stories à un sprint terminé ou annulé");
        }

        if (!userStory.getProductBacklog().getProject().getId().equals(sprint.getProject().getId())) {
            throw new IllegalStateException("La User Story n'appartient pas au même projet que le sprint");
        }

        if (!userStory.areDependenciesCompleted()) {
            long incompleteDependenciesCount = userStory.getDependencies().stream()
                    .filter(dep -> dep.getStatus() != WorkItemStatus.DONE)
                    .count();

            throw new IllegalStateException(
                    String.format("La User Story a %d dépendance(s) non complétée(s). " +
                                    "Les dépendances doivent être terminées avant d'ajouter cette story au sprint.",
                            incompleteDependenciesCount)
            );
        }

        if (userStory.getSprintBacklog() != null &&
                userStory.getSprintBacklog().getSprintStatus() == SprintStatus.ACTIVE) {
            throw new IllegalStateException("La User Story est déjà assignée à un sprint actif");
        }

        sprint.addUserStory(userStory);

        for (Task task : userStory.getTasks()) {
            task.setSprintBacklog(sprint);
        }

        return sprintBacklogRepository.save(sprint);
    }

    public SprintBacklog addMultipleUserStoriesToSprint(Long sprintId, List<Long> userStoryIds) {
        SprintBacklog sprint = getSprintById(sprintId);

        if (sprint.getSprintStatus() == SprintStatus.COMPLETED ||
                sprint.getSprintStatus() == SprintStatus.CANCELLED) {
            throw new IllegalStateException("Impossible d'ajouter des stories à un sprint terminé ou annulé");
        }

        if (userStoryIds == null || userStoryIds.isEmpty()) {
            throw new IllegalArgumentException("La liste des User Stories ne peut pas être vide");
        }

        List<UserStory> userStories = userStoryRepository.findAllById(userStoryIds);

        if (userStories.size() != userStoryIds.size()) {
            throw new IllegalArgumentException("Une ou plusieurs User Stories n'ont pas été trouvées");
        }

        for (UserStory userStory : userStories) {
            if (!userStory.getProductBacklog().getProject().getId().equals(sprint.getProject().getId())) {
                throw new IllegalStateException(
                        "La User Story '" + userStory.getTitle() + "' n'appartient pas au même projet que le sprint");
            }

            if (!userStory.areDependenciesCompleted()) {
                throw new IllegalStateException(
                        "La User Story '" + userStory.getTitle() + "' a des dépendances non complétées");
            }

            if (userStory.getSprintBacklog() != null &&
                    userStory.getSprintBacklog().getSprintStatus() == SprintStatus.ACTIVE) {
                throw new IllegalStateException(
                        "La User Story '" + userStory.getTitle() + "' est déjà assignée à un sprint actif");
            }

            sprint.addUserStory(userStory);

            for (Task task : userStory.getTasks()) {
                task.setSprintBacklog(sprint);
            }
        }

        return sprintBacklogRepository.save(sprint);
    }

    public SprintBacklog removeUserStoryFromSprint(Long sprintId, Long userStoryId) {
        SprintBacklog sprint = getSprintById(sprintId);

        UserStory userStory = userStoryRepository.findById(userStoryId)
                .orElseThrow(() -> new IllegalArgumentException("User Story non trouvée avec l'ID: " + userStoryId));

        if (sprint.getSprintStatus() == SprintStatus.COMPLETED) {
            throw new IllegalStateException("Impossible de retirer des User Stories d'un sprint terminé");
        }

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

        sprint.removeUserStory(userStory);

        for (Task task : userStory.getTasks()) {
            task.setSprintBacklog(null);
        }

        return sprintBacklogRepository.save(sprint);
    }

    @Transactional(readOnly = true)
    public SprintMetrics getSprintMetrics(Long sprintId) {
        SprintBacklog sprint = getSprintById(sprintId);

        int velocity = sprint.calculateVelocity();
        double progressPercentage = sprint.calculateProgress();
        int totalStoryPoints = sprint.getTotalStoryPoints();
        int remainingStoryPoints = sprint.getRemainingStoryPoints();
        int completedStoryPoints = velocity;

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

        List<Task> allTasks = taskRepository.findBySprintBacklogId(Math.toIntExact(sprintId));
        long totalTasks = allTasks.size();
        long completedTasks = allTasks.stream()
                .filter(task -> task.getStatus() == WorkItemStatus.DONE)
                .count();
        long inProgressTasks = allTasks.stream()
                .filter(task -> task.getStatus() == WorkItemStatus.IN_PROGRESS)
                .count();

        long sprintDurationDays = sprint.getSprintDuration();
        long daysElapsed = calculateDaysElapsed(sprint);
        long daysRemaining = Math.max(0, sprintDurationDays - daysElapsed);

        int totalEstimatedHours = allTasks.stream()
                .mapToInt(Task::getEstimatedHours)
                .sum();
        int totalActualHours = allTasks.stream()
                .mapToInt(Task::getActualHours)
                .sum();
        int remainingHours = allTasks.stream()
                .mapToInt(Task::getRemainingHours)
                .sum();

        double expectedVelocityRate = sprintDurationDays > 0
                ? (double) totalStoryPoints / sprintDurationDays
                : 0.0;
        double actualVelocityRate = daysElapsed > 0
                ? (double) completedStoryPoints / daysElapsed
                : 0.0;

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

    @Transactional(readOnly = true)
    public SprintBacklog getLastSprint(Long projectId) {
        return sprintBacklogRepository.findTopByProjectIdOrderBySprintNumberDesc(projectId)
                .orElseThrow(() -> new IllegalStateException("Aucun sprint trouvé pour ce projet"));
    }

    @Transactional(readOnly = true)
    public List<SprintBacklog> getSprintsByStatus(Long projectId, SprintStatus status) {
        return sprintBacklogRepository.findByProjectIdAndSprintStatus(projectId, status);
    }

    @Transactional(readOnly = true)
    public boolean canStartSprint(Long sprintId) {
        SprintBacklog sprint = getSprintById(sprintId);

        if (sprint.getSprintStatus() != SprintStatus.PLANNED) {
            return false;
        }

        if (sprint.getUserStories().isEmpty()) {
            return false;
        }

        long activeSprintsCount = sprintBacklogRepository.countByProjectIdAndSprintStatus(
                sprint.getProject().getId(), SprintStatus.ACTIVE);

        return activeSprintsCount == 0;
    }

    @Transactional(readOnly = true)
    public List<SprintBacklog> getSprintsBetweenDates(Long projectId, LocalDate startDate, LocalDate endDate) {
        return sprintBacklogRepository.findByProjectIdAndStartDateBetween(projectId, startDate, endDate);
    }

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

    @Transactional(readOnly = true)
    public SprintBurndown getSprintBurndown(Long sprintId) {
        SprintBacklog sprint = getSprintById(sprintId);

        int totalStoryPoints = sprint.getTotalStoryPoints();
        int remainingStoryPoints = sprint.getRemainingStoryPoints();
        int completedStoryPoints = totalStoryPoints - remainingStoryPoints;

        long totalDays = sprint.getSprintDuration();
        long elapsedDays = LocalDate.now().isBefore(sprint.getStartDate()) ? 0 :
                LocalDate.now().isAfter(sprint.getEndDate()) ? totalDays :
                        ChronoUnit.DAYS.between(sprint.getStartDate(), LocalDate.now());

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

    @Transactional(readOnly = true)
    public List<SprintBacklog> getSprintsWithIncompleteStories(Long projectId) {
        return sprintBacklogRepository.findSprintsWithIncompleteStories(projectId);
    }

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

    private void moveIncompletedUserStoriesToBacklog(SprintBacklog sprint) {
        List<UserStory> incompletedStories = sprint.getUserStories().stream()
                .filter(us -> us.getStatus() != WorkItemStatus.DONE)
                .toList();

        for (UserStory story : incompletedStories) {
            story.setSprintBacklog(null);
            for (Task task : story.getTasks()) {
                task.setSprintBacklog(null);
            }
        }
    }

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

    public record SprintMetrics(
            int velocity,
            double progressPercentage,
            int totalStoryPoints,
            int completedStoryPoints,
            int remainingStoryPoints,
            int totalStories,
            int completedStories,
            int inProgressStories,
            int todoStories,
            int totalTasks,
            int completedTasks,
            int inProgressTasks,
            int totalEstimatedHours,
            int totalActualHours,
            int remainingHours,
            long sprintDurationDays,
            long daysElapsed,
            long daysRemaining,
            double expectedVelocityRate,
            double actualVelocityRate,
            boolean onTrack,
            SprintStatus status
    ) {}

    public record SprintBurndown(
            int totalStoryPoints,
            int remainingStoryPoints,
            int completedStoryPoints,
            int idealRemaining,
            long elapsedDays,
            long totalDays
    ) {}
}