package com.Agile.demo.execution.services;

import com.Agile.demo.execution.repositories.SprintBacklogRepository;
import com.Agile.demo.execution.repositories.TaskRepository;
import com.Agile.demo.model.*;
import com.Agile.demo.planning.repository.UserStoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ValidationService {

    private final UserStoryRepository userStoryRepository;
    private final SprintBacklogRepository sprintBacklogRepository;
    private final TaskRepository taskRepository;

    public ValidationResult validateSprintCanStart(Long sprintId) {
        List<String> errors = new ArrayList<>();

        SprintBacklog sprint = sprintBacklogRepository.findById(sprintId)
                .orElseThrow(() -> new IllegalArgumentException("Sprint non trouvé"));

        if (sprint.getSprintStatus() != SprintStatus.PLANNED) {
            errors.add(String.format("Le sprint doit être en statut PLANNED (statut actuel: %s)",
                    sprint.getSprintStatus()));
        }

        long activeSprintsCount = sprintBacklogRepository.countByProjectIdAndSprintStatus(
                sprint.getProject().getId(), SprintStatus.ACTIVE);
        if (activeSprintsCount > 0) {
            errors.add("Un sprint est déjà actif pour ce projet");
        }

        if (sprint.getUserStories().isEmpty()) {
            errors.add("Le sprint doit contenir au moins une User Story");
        }

        List<UserStory> storiesWithUnmetDeps = sprint.getUserStories().stream()
                .filter(us -> !us.areDependenciesCompleted())
                .toList();

        if (!storiesWithUnmetDeps.isEmpty()) {
            String titles = storiesWithUnmetDeps.stream()
                    .map(UserStory::getTitle)
                    .collect(Collectors.joining(", "));
            errors.add(String.format("User Stories avec dépendances non satisfaites: %s", titles));
        }

        if (sprint.getEndDate().isBefore(sprint.getStartDate())) {
            errors.add("La date de fin est avant la date de début");
        }

        return new ValidationResult(errors.isEmpty(), errors);
    }

    public ValidationResult validateTaskCanStart(Long taskId, Long userId) {
        List<String> errors = new ArrayList<>();

        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new IllegalArgumentException("Tâche non trouvée"));

        if (task.getStatus() != WorkItemStatus.TODO) {
            errors.add(String.format("La tâche doit être en statut TODO (statut actuel: %s)",
                    task.getStatus()));
        }

        if (task.getAssignedUser() != null && !task.getAssignedUser().getId().equals(userId)) {
            errors.add(String.format("La tâche est déjà assignée à %s",
                    task.getAssignedUser().getUsername()));
        }

        UserStory userStory = task.getUserStory();
        if (userStory != null && !userStory.canBeStarted()) {
            if (!userStory.areDependenciesCompleted()) {
                errors.add("La User Story parente a des dépendances non complétées");
            }
        }

        if (task.getSprintBacklog() != null &&
                task.getSprintBacklog().getSprintStatus() != SprintStatus.ACTIVE) {
            errors.add("Le sprint n'est pas actif");
        }

        return new ValidationResult(errors.isEmpty(), errors);
    }

    public ValidationResult validateUserStoryCanBeAddedToSprint(Long sprintId, Long userStoryId) {
        List<String> errors = new ArrayList<>();

        SprintBacklog sprint = sprintBacklogRepository.findById(sprintId)
                .orElseThrow(() -> new IllegalArgumentException("Sprint non trouvé"));

        UserStory userStory = userStoryRepository.findById(userStoryId)
                .orElseThrow(() -> new IllegalArgumentException("User Story non trouvée"));

        if (sprint.getSprintStatus() == SprintStatus.COMPLETED ||
                sprint.getSprintStatus() == SprintStatus.CANCELLED) {
            errors.add("Impossible d'ajouter des stories à un sprint terminé ou annulé");
        }

        if (!userStory.getProductBacklog().getProject().getId().equals(sprint.getProject().getId())) {
            errors.add("La User Story n'appartient pas au même projet que le sprint");
        }

        if (!userStory.areDependenciesCompleted()) {
            List<UserStory> incompleteDeps = userStory.getDependencies().stream()
                    .filter(dep -> dep.getStatus() != WorkItemStatus.DONE)
                    .toList();

            String depTitles = incompleteDeps.stream()
                    .map(UserStory::getTitle)
                    .collect(Collectors.joining(", "));

            errors.add(String.format("Dépendances non complétées: %s", depTitles));
        }

        if (userStory.getSprintBacklog() != null &&
                userStory.getSprintBacklog().getSprintStatus() == SprintStatus.ACTIVE) {
            errors.add(String.format("La User Story est déjà dans le sprint actif '%s'",
                    userStory.getSprintBacklog().getName()));
        }

        return new ValidationResult(errors.isEmpty(), errors);
    }

    public ValidationResult validateUserStoryCanBeRemovedFromSprint(Long sprintId, Long userStoryId) {
        List<String> errors = new ArrayList<>();

        SprintBacklog sprint = sprintBacklogRepository.findById(sprintId)
                .orElseThrow(() -> new IllegalArgumentException("Sprint non trouvé"));

        UserStory userStory = userStoryRepository.findById(userStoryId)
                .orElseThrow(() -> new IllegalArgumentException("User Story non trouvée"));

        if (sprint.getSprintStatus() == SprintStatus.COMPLETED) {
            errors.add("Impossible de retirer des stories d'un sprint terminé");
        }

        long tasksInProgress = userStory.getTasks().stream()
                .filter(task -> task.getStatus() == WorkItemStatus.IN_PROGRESS)
                .count();

        if (tasksInProgress > 0) {
            errors.add(String.format("%d tâche(s) en cours. Terminez-les d'abord.", tasksInProgress));
        }

        return new ValidationResult(errors.isEmpty(), errors);
    }

    public ValidationResult validateTaskCanMoveToReview(Long taskId) {
        List<String> errors = new ArrayList<>();

        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new IllegalArgumentException("Tâche non trouvée"));

        if (task.getStatus() != WorkItemStatus.IN_PROGRESS) {
            errors.add(String.format("La tâche doit être en cours (statut actuel: %s)",
                    task.getStatus()));
        }

        if (task.getActualHours() == 0) {
            errors.add("Aucune heure enregistrée. Enregistrez du temps avant la revue.");
        }

        return new ValidationResult(errors.isEmpty(), errors);
    }

    public ValidationResult validateTaskCanComplete(Long taskId) {
        List<String> errors = new ArrayList<>();
        List<String> warnings = new ArrayList<>();

        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new IllegalArgumentException("Tâche non trouvée"));

        if (task.getStatus() != WorkItemStatus.IN_PROGRESS &&
                task.getStatus() != WorkItemStatus.IN_REVIEW &&
                task.getStatus() != WorkItemStatus.TESTING) {
            errors.add(String.format("Statut invalide pour complétion: %s", task.getStatus()));
        }

        if (task.getActualHours() == 0) {
            warnings.add("Aucune heure enregistrée sur cette tâche");
        }

        if ((double) task.getActualHours() > task.getEstimatedHours() * 1.5) {
            warnings.add(String.format("Temps réel (%.1fh) dépasse largement l'estimation (%.1fh)",
                    (float) task.getActualHours(),
                    (float) task.getEstimatedHours()));
        }

        return new ValidationResult(errors.isEmpty(), errors, warnings);
    }

    public SprintHealthReport analyzeSprintHealth(Long sprintId) {
        SprintBacklog sprint = sprintBacklogRepository.findById(sprintId)
                .orElseThrow(() -> new IllegalArgumentException("Sprint non trouvé"));

        List<String> issues = new ArrayList<>();
        List<String> warnings = new ArrayList<>();
        List<String> recommendations = new ArrayList<>();

        long blockedStories = sprint.getUserStories().stream()
                .filter(us -> !us.areDependenciesCompleted())
                .count();
        if (blockedStories > 0) {
            issues.add(String.format("%d User Story(ies) avec dépendances non satisfaites", blockedStories));
        }

        List<Task> unassignedTasks = taskRepository.findBySprintBacklogIdAndAssignedUserIsNull(sprintId);
        if (!unassignedTasks.isEmpty()) {
            warnings.add(String.format("%d tâche(s) non assignée(s)", unassignedTasks.size()));
            recommendations.add("Assignez toutes les tâches pour une meilleure visibilité");
        }

        List<Task> overEstimatedTasks = taskRepository.findOverEstimatedTasksBySprint(sprintId);
        if (!overEstimatedTasks.isEmpty()) {
            warnings.add(String.format("%d tâche(s) en dépassement d'estimation", overEstimatedTasks.size()));
        }

        double progress = sprint.calculateProgress();
        long daysElapsed = ChronoUnit.DAYS.between(sprint.getStartDate(), LocalDate.now());
        long totalDays = sprint.getSprintDuration();

        if (totalDays > 0) {
            double expectedProgress = (daysElapsed * 100.0) / totalDays;
            if (progress < expectedProgress - 20) {
                warnings.add(String.format("Sprint en retard: %.1f%% complété vs %.1f%% attendu",
                        progress, expectedProgress));
                recommendations.add("Réduire le scope ou augmenter la capacité de l'équipe");
            }
        }

        int healthScore = calculateHealthScore(issues.size(), warnings.size(), progress);

        return new SprintHealthReport(
                healthScore,
                issues,
                warnings,
                recommendations,
                progress,
                sprint.getSprintStatus()
        );
    }

    private int calculateHealthScore(int issuesCount, int warningsCount, double progress) {
        int score = 100;
        score -= issuesCount * 20;
        score -= warningsCount * 10;

        if (progress > 80) {
            score += 10;
        } else if (progress < 30) {
            score -= 10;
        }

        return Math.max(0, Math.min(100, score));
    }

    public record ValidationResult(
            boolean isValid,
            List<String> errors,
            List<String> warnings
    ) {
        public ValidationResult(boolean isValid, List<String> errors) {
            this(isValid, errors, new ArrayList<>());
        }

        public String getErrorMessage() {
            return String.join("; ", errors);
        }

        public boolean hasWarnings() {
            return !warnings.isEmpty();
        }
    }

    public record SprintHealthReport(
            int healthScore,
            List<String> issues,
            List<String> warnings,
            List<String> recommendations,
            double progressPercentage,
            SprintStatus status
    ) {
        public boolean isHealthy() {
            return healthScore >= 70;
        }

        public String getHealthLevel() {
            if (healthScore >= 80) return "Excellent";
            if (healthScore >= 60) return "Bon";
            if (healthScore >= 40) return "Moyen";
            return "Critique";
        }
    }
}