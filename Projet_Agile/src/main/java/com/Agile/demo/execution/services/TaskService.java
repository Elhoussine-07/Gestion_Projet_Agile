package com.Agile.demo.execution.services;

import com.Agile.demo.model.*;
import com.Agile.demo.execution.repositories.TaskRepository;
import com.Agile.demo.execution.repositories.UserRepository;
import com.Agile.demo.planning.repository.UserStoryRepository;
import com.Agile.demo.execution.repositories.SprintBacklogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class TaskService {


    private final TaskRepository taskRepository;
    private final UserStoryRepository userStoryRepository;
    private final UserRepository userRepository;
    private final SprintBacklogRepository SprintBacklogRepository;

    /**
     * Crée une nouvelle tâche pour une User Story
     */
    public Task createTask(Long userStoryId, String title, Integer estimatedHours) {
        UserStory userStory = userStoryRepository.findById(userStoryId)
                .orElseThrow(() -> new IllegalArgumentException("User Story non trouvée avec l'ID: " + userStoryId));

        if (estimatedHours < 0) {
            throw new IllegalArgumentException("Les heures estimées ne peuvent pas être négatives");
        }

        Task task = new Task(title, estimatedHours);
        task.setUserStory(userStory);

        // Si la User Story est dans un sprint, associer la tâche au sprint
        if (userStory.getSprintBacklog() != null) {
            task.setSprintBacklog(userStory.getSprintBacklog());
        }

        return taskRepository.save(task);
    }

    /**
     * Crée une tâche avec description
     */
    public Task createTask(Long userStoryId, String title, String description, Integer estimatedHours) {
        UserStory userStory = userStoryRepository.findById(userStoryId)
                .orElseThrow(() -> new IllegalArgumentException("User Story non trouvée avec l'ID: " + userStoryId));

        Task task = new Task(title, description, estimatedHours);
        task.setUserStory(userStory);

        if (userStory.getSprintBacklog() != null) {
            task.setSprintBacklog(userStory.getSprintBacklog());
        }

        return taskRepository.save(task);
    }

    /**
     * Récupère une tâche par son ID
     */
    @Transactional(readOnly = true)
    public Task getTaskById(Long taskId) {
        return taskRepository.findById(taskId)
                .orElseThrow(() -> new IllegalArgumentException("Tâche non trouvée avec l'ID: " + taskId));
    }

    /**
     * Récupère toutes les tâches d'une User Story
     */
    @Transactional(readOnly = true)
    public List<Task> getTasksByUserStory(Long userStoryId) {
        return taskRepository.findByUserStoryId(userStoryId);
    }

    /**
     * Récupère toutes les tâches d'un sprint
     */
    @Transactional(readOnly = true)
    public List<Task> getTasksBySprint(Integer sprintBacklogId) {
        return taskRepository.findBySprintBacklogId(sprintBacklogId);
    }

    /**
     * Récupère toutes les tâches assignées à un utilisateur
     */
    @Transactional(readOnly = true)
    public List<Task> getTasksByUser(Long userId) {
        return taskRepository.findByAssignedUserId(userId);
    }

    /**
     * Assigne une tâche à un utilisateur
     */
    public Task assignTask(Long taskId, Long userId) {
        Task task = getTaskById(taskId);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Utilisateur non trouvé avec l'ID: " + userId));

        if (!task.canBeAssigned()) {
            throw new IllegalStateException("La tâche ne peut pas être assignée (déjà assignée ou pas en statut TODO)");
        }

        task.assignTo(user);
        return taskRepository.save(task);
    }

    /**
     * Désassigne une tâche
     */
    public Task unassignTask(Long taskId) {
        Task task = getTaskById(taskId);

        if (task.getStatus() == WorkItemStatus.IN_PROGRESS) {
            throw new IllegalStateException("Impossible de désassigner une tâche en cours");
        }

        task.unassign();
        return taskRepository.save(task);
    }

    /**
     * Enregistre des heures travaillées sur une tâche
     */
    public Task logHours(Long taskId, Integer hours) {
        Task task = getTaskById(taskId);

        if (hours <= 0) {
            throw new IllegalArgumentException("Le nombre d'heures doit être positif");
        }

        task.logHours(hours);
        return taskRepository.save(task);
    }

    /**
     * Met à jour les heures estimées d'une tâche
     */
    public Task updateEstimatedHours(Long taskId, Integer estimatedHours) {
        Task task = getTaskById(taskId);

        if (estimatedHours < 0) {
            throw new IllegalArgumentException("Les heures estimées ne peuvent pas être négatives");
        }

        task.setEstimatedHours(estimatedHours);
        return taskRepository.save(task);
    }

    /**
     * Démarre une tâche
     */
    public Task startTask(Long taskId) {
        Task task = getTaskById(taskId);

        if (task.getAssignedUser() == null) {
            throw new IllegalStateException("La tâche doit être assignée avant de démarrer");
        }

        task.start();
        return taskRepository.save(task);
    }

    /**
     * Met une tâche en revue
     */
    public Task moveTaskToReview(Long taskId) {
        Task task = getTaskById(taskId);
        task.moveToReview();
        return taskRepository.save(task);
    }

    /**
     * Met une tâche en test
     */
    public Task moveTaskToTesting(Long taskId) {
        Task task = getTaskById(taskId);
        task.moveToTesting();
        return taskRepository.save(task);
    }

    /**
     * Complète une tâche
     */
    public Task completeTask(Long taskId) {
        Task task = getTaskById(taskId);
        task.complete();
        return taskRepository.save(task);
    }

    /**
     * Met à jour le statut d'une tâche
     */
    public Task updateTaskStatus(Long taskId, WorkItemStatus status) {
        Task task = getTaskById(taskId);
        task.updateStatus(status);
        return taskRepository.save(task);
    }

    /**
     * Met à jour la description d'une tâche
     */
    public Task updateTaskDescription(Long taskId, String description) {
        Task task = getTaskById(taskId);
        task.setDescription(description);
        return taskRepository.save(task);
    }

    /**
     * Supprime une tâche
     */
    public void deleteTask(Long taskId) {
        Task task = getTaskById(taskId);

        if (task.getStatus() == WorkItemStatus.DONE) {
            throw new IllegalStateException("Impossible de supprimer une tâche terminée");
        }

        taskRepository.delete(task);
    }

    /**
     * Récupère les tâches non assignées d'un sprint
     */
    @Transactional(readOnly = true)
    public List<Task> getUnassignedTasksBySprint(Long sprintBacklogId) {
        return taskRepository.findBySprintBacklogIdAndAssignedUserIsNull(sprintBacklogId);
    }

    /**
     * Récupère les tâches en retard d'un sprint
     */
    @Transactional(readOnly = true)
    public List<Task> getOverEstimatedTasksBySprint(Long sprintBacklogId) {
        return taskRepository.findOverEstimatedTasksBySprint(sprintBacklogId);
    }

    /**
     * Calcule les métriques d'une User Story basées sur ses tâches
     */
    @Transactional(readOnly = true)
    public UserStoryTaskMetrics getUserStoryTaskMetrics(Long userStoryId) {
        List<Task> tasks = taskRepository.findByUserStoryId(userStoryId);

        int totalEstimated = tasks.stream()
                .mapToInt(Task::getEstimatedHours)
                .sum();

        int totalActual = tasks.stream()
                .mapToInt(Task::getActualHours)
                .sum();

        long completedTasks = tasks.stream()
                .filter(t -> t.getStatus() == WorkItemStatus.DONE)
                .count();

        double progress = tasks.isEmpty() ? 0.0 : (completedTasks * 100.0) / tasks.size();

        return new UserStoryTaskMetrics(
                tasks.size(),
                (int) completedTasks,
                totalEstimated,
                totalActual,
                progress
        );
    }

    /**
     * Classe interne pour les métriques des tâches d'une User Story
     */
    public record UserStoryTaskMetrics(
            int totalTasks,
            int completedTasks,
            int totalEstimatedHours,
            int totalActualHours,
            double progressPercentage
    ) {}



    // Méthodes à ajouter dans TaskService

    /**
     * Récupère les tâches par statut pour un sprint
     */
    @Transactional(readOnly = true)
    public List<Task> getTasksBySprintAndStatus(Long sprintBacklogId, WorkItemStatus status) {
        return taskRepository.findBySprintBacklogIdAndStatus(sprintBacklogId, status);
    }

    /**
     * Récupère les tâches bloquées
     */
    @Transactional(readOnly = true)
    public List<Task> getBlockedTasks(Integer sprintBacklogId) {
        return taskRepository.findBySprintBacklogIdAndIsBlockedTrue(sprintBacklogId);
    }

    /**
     * Marque une tâche comme bloquée
     */
    public Task blockTask(Long taskId, String blockReason) {
        Task task = getTaskById(taskId);

        if (task.getStatus() == WorkItemStatus.DONE) {
            throw new IllegalStateException("Impossible de bloquer une tâche terminée");
        }

        task.setBlocked(true);
        task.setBlockReason(blockReason);
        return taskRepository.save(task);
    }

    /**
     * Débloque une tâche
     */
    public Task unblockTask(Long taskId) {
        Task task = getTaskById(taskId);
        task.setBlocked(false);
        task.setBlockReason(null);
        return taskRepository.save(task);
    }

    /**
     * Récupère les tâches avec heures dépassées
     */
    @Transactional(readOnly = true)
    public List<Task> getTasksExceedingEstimate(Integer sprintBacklogId) {
        return taskRepository.findBySprintBacklogIdAndActualHoursGreaterThanEstimatedHours(sprintBacklogId);
    }

    /**
     * Réassigne toutes les tâches d'un utilisateur à un autre
     */
    public void reassignUserTasks(Long fromUserId, Long toUserId) {
        User toUser = userRepository.findById(toUserId)
                .orElseThrow(() -> new IllegalArgumentException("Utilisateur cible non trouvé"));

        List<Task> tasks = taskRepository.findByAssignedUserIdAndStatusNot(fromUserId, WorkItemStatus.DONE);

        tasks.forEach(task -> {
            task.assignTo(toUser);
            taskRepository.save(task);
        });
    }

    /**
     * Récupère les tâches critiques (peu de temps restant)
     */
    @Transactional(readOnly = true)
    public List<Task> getCriticalTasks(Integer SprintNumber) {

        SprintBacklog sprint = SprintBacklogRepository.findBySprintNumber(SprintNumber)
                .orElseThrow(() -> new IllegalArgumentException("Sprint non trouvé avec le numéro: " + SprintNumber));


        long daysRemaining = java.time.temporal.ChronoUnit.DAYS.between(
                LocalDate.now(), sprint.getEndDate());

        if (daysRemaining <= 0) {
            return List.of();
        }

        return taskRepository.findBySprintBacklogIdAndStatusNot(SprintNumber, WorkItemStatus.DONE)
                .stream()
                .filter(task -> {
                    int remainingHours = task.getEstimatedHours() - task.getActualHours();
                    return remainingHours > (daysRemaining * 8); // 8 heures par jour
                })
                .toList();
    }

    /**
     * Dupliquer une tâche
     */
    public Task duplicateTask(Long taskId) {
        Task original = getTaskById(taskId);

        Task duplicate = new Task(
                original.getTitle() + " (Copie)",
                original.getDescription(),
                original.getEstimatedHours()
        );
        duplicate.setUserStory(original.getUserStory());
        duplicate.setSprintBacklog(original.getSprintBacklog());

        return taskRepository.save(duplicate);
    }

    /**
     * Met à jour le titre d'une tâche
     */
    public Task updateTaskTitle(Long taskId, String title) {
        if (title == null || title.trim().isEmpty()) {
            throw new IllegalArgumentException("Le titre ne peut pas être vide");
        }

        Task task = getTaskById(taskId);
        task.setTitle(title);
        return taskRepository.save(task);
    }

    /**
     * Récupère les tâches terminées récemment
     */
    @Transactional(readOnly = true)
    public List<Task> getRecentlyCompletedTasks(Integer sprintBacklogId, int days) {
        LocalDate sinceDate = LocalDate.now().minusDays(days);
        return taskRepository.findBySprintBacklogIdAndStatusAndCompletedDateAfter(
                sprintBacklogId, WorkItemStatus.DONE, sinceDate);
    }

    /**
     * Calcule le temps restant total pour un sprint
     */
    @Transactional(readOnly = true)
    public int calculateRemainingHours(Integer sprintBacklogId) {
        List<Task> tasks = taskRepository.findBySprintBacklogIdAndStatusNot(
                sprintBacklogId, WorkItemStatus.DONE);

        return tasks.stream()
                .mapToInt(task -> Math.max(0, task.getEstimatedHours() - task.getActualHours()))
                .sum();
    }

    /**
     * Récupère les statistiques des tâches d'un sprint
     */
    @Transactional(readOnly = true)
    public SprintTaskStatistics getSprintTaskStatistics(Integer sprintBacklogId) {
        List<Task> allTasks = taskRepository.findBySprintBacklogId(sprintBacklogId);

        long todoCount = allTasks.stream().filter(t -> t.getStatus() == WorkItemStatus.TODO).count();
        long inProgressCount = allTasks.stream().filter(t -> t.getStatus() == WorkItemStatus.IN_PROGRESS).count();
        long inReviewCount = allTasks.stream().filter(t -> t.getStatus() == WorkItemStatus.IN_REVIEW).count();
        long testingCount = allTasks.stream().filter(t -> t.getStatus() == WorkItemStatus.TESTING).count();
        long doneCount = allTasks.stream().filter(t -> t.getStatus() == WorkItemStatus.DONE).count();
        long blockedCount = allTasks.stream().filter(Task::isBlocked).count();

        int totalEstimated = allTasks.stream().mapToInt(Task::getEstimatedHours).sum();
        int totalActual = allTasks.stream().mapToInt(Task::getActualHours).sum();

        double efficiency = totalEstimated > 0 ? (totalEstimated * 100.0) / totalActual : 0.0;

        return new SprintTaskStatistics(
                allTasks.size(),
                (int) todoCount,
                (int) inProgressCount,
                (int) inReviewCount,
                (int) testingCount,
                (int) doneCount,
                (int) blockedCount,
                totalEstimated,
                totalActual,
                efficiency
        );
    }

    /**
     * Vérifie si une tâche peut être supprimée
     */
    @Transactional(readOnly = true)
    public boolean canDeleteTask(Long taskId) {
        Task task = getTaskById(taskId);
        return task.getStatus() != WorkItemStatus.DONE && task.getActualHours() == 0;
    }

    /**
     * Récupère les tâches assignées à un utilisateur pour un sprint spécifique
     */
    @Transactional(readOnly = true)
    public List<Task> getUserTasksForSprint(Long userId, Long sprintBacklogId) {
        return taskRepository.findByAssignedUserIdAndSprintBacklogId(userId, sprintBacklogId);
    }

    /**
     * Classe pour les statistiques des tâches d'un sprint
     */
    public record SprintTaskStatistics(
            int totalTasks,
            int todoTasks,
            int inProgressTasks,
            int inReviewTasks,
            int testingTasks,
            int doneTasks,
            int blockedTasks,
            int totalEstimatedHours,
            int totalActualHours,
            double efficiency
    ) {}

}