package com.Agile.demo.execution.services;

import com.Agile.demo.execution.repositories.TaskRepository;
import com.Agile.demo.execution.repositories.UserRepository;
import com.Agile.demo.planning.repository.UserStoryRepository;
import com.Agile.demo.model.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Transactional
public class TaskService {

    private final TaskRepository taskRepository;
    private final UserRepository userRepository;
    private final UserStoryRepository userStoryRepository;

    private final Map<Long, TaskBlockInfo> blockedTasks = new HashMap<>();

    // ==================== CRUD OPERATIONS ====================

    public Task createTask(Long userStoryId, String title, Integer estimatedHours) {
        UserStory userStory = userStoryRepository.findById(userStoryId)
                .orElseThrow(() -> new IllegalArgumentException("User Story non trouvée avec l'ID: " + userStoryId));

        if (estimatedHours < 0) {
            throw new IllegalArgumentException("Les heures estimées ne peuvent pas être négatives");
        }

        Task task = new Task(title, estimatedHours);
        task.setUserStory(userStory);
        userStory.addTask(task);

        return taskRepository.save(task);
    }

    public Task createTask(Long userStoryId, String title, String description, Integer estimatedHours) {
        UserStory userStory = userStoryRepository.findById(userStoryId)
                .orElseThrow(() -> new IllegalArgumentException("User Story non trouvée avec l'ID: " + userStoryId));

        if (estimatedHours < 0) {
            throw new IllegalArgumentException("Les heures estimées ne peuvent pas être négatives");
        }

        Task task = new Task(title, estimatedHours);
        task.setDescription(description);
        task.setUserStory(userStory);
        userStory.addTask(task);

        return taskRepository.save(task);
    }

    @Transactional(readOnly = true)
    public Task getTaskById(Long taskId) {
        return taskRepository.findById(taskId)
                .orElseThrow(() -> new IllegalArgumentException("Tâche non trouvée avec l'ID: " + taskId));
    }

    @Transactional(readOnly = true)
    public List<Task> getTasksByUserStory(Long userStoryId) {
        return taskRepository.findByUserStoryId(userStoryId);
    }

    @Transactional(readOnly = true)
    public List<Task> getTasksBySprint(Integer sprintId) {
        return taskRepository.findBySprintBacklogId(sprintId);
    }

    @Transactional(readOnly = true)
    public List<Task> getTasksByUser(Long userId) {
        return taskRepository.findByAssignedUserId(userId);
    }

    public Task assignTask(Long taskId, Long userId) {
        Task task = getTaskById(taskId);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Utilisateur non trouvé avec l'ID: " + userId));

        task.assignTo(user);
        return taskRepository.save(task);
    }

    public Task unassignTask(Long taskId) {
        Task task = getTaskById(taskId);

        if (task.getStatus() == WorkItemStatus.IN_PROGRESS) {
            throw new IllegalStateException("Impossible de désassigner une tâche en cours");
        }

        task.unassign();
        return taskRepository.save(task);
    }

    public Task logHours(Long taskId, Integer hours) {
        Task task = getTaskById(taskId);

        if (hours <= 0) {
            throw new IllegalArgumentException("Le nombre d'heures doit être positif");
        }

        task.logHours(hours);
        return taskRepository.save(task);
    }

    public Task updateEstimatedHours(Long taskId, Integer estimatedHours) {
        Task task = getTaskById(taskId);

        if (estimatedHours < 0) {
            throw new IllegalArgumentException("Les heures estimées ne peuvent pas être négatives");
        }

        task.setEstimatedHours(estimatedHours);
        return taskRepository.save(task);
    }

    public Task updateTaskStatus(Long taskId, WorkItemStatus status) {
        Task task = getTaskById(taskId);
        task.updateStatus(status);
        return taskRepository.save(task);
    }

    public Task updateTaskDescription(Long taskId, String description) {
        Task task = getTaskById(taskId);
        task.setDescription(description);
        return taskRepository.save(task);
    }

    public Task updateTaskTitle(Long taskId, String title) {
        if (title == null || title.trim().isEmpty()) {
            throw new IllegalArgumentException("Le titre ne peut pas être vide");
        }

        Task task = getTaskById(taskId);
        task.setTitle(title);
        return taskRepository.save(task);
    }

    public void deleteTask(Long taskId) {
        Task task = getTaskById(taskId);

        if (task.getStatus() == WorkItemStatus.DONE) {
            throw new IllegalStateException("Impossible de supprimer une tâche terminée");
        }

        taskRepository.delete(task);
    }

    public Task duplicateTask(Long taskId) {
        Task originalTask = getTaskById(taskId);

        Task duplicatedTask = new Task(
                originalTask.getTitle() + " (Copie)",
                originalTask.getEstimatedHours()
        );
        duplicatedTask.setDescription(originalTask.getDescription());
        duplicatedTask.setUserStory(originalTask.getUserStory());

        return taskRepository.save(duplicatedTask);
    }

    // ==================== WORKFLOW OPERATIONS ====================

    public Task startTask(Long taskId) {
        Task task = getTaskById(taskId);

        if (task.getAssignedUser() == null) {
            throw new IllegalStateException("La tâche doit être assignée avant d'être démarrée");
        }

        if (task.getStatus() != WorkItemStatus.TODO) {
            throw new IllegalStateException(
                    String.format("La tâche doit être en statut TODO pour être démarrée. Statut actuel: %s",
                            task.getStatus())
            );
        }

        task.start();

        UserStory userStory = task.getUserStory();
        if (userStory != null && userStory.getStatus() == WorkItemStatus.TODO) {
            userStory.start();
        }

        return taskRepository.save(task);
    }

    public void startTask(Long taskId, Long userId) {
        Task task = getTaskById(taskId);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Utilisateur non trouvé avec l'ID: " + userId));

        if (task.getStatus() != WorkItemStatus.TODO) {
            throw new IllegalStateException(
                    String.format("La tâche doit être en statut TODO pour être démarrée. Statut actuel: %s",
                            task.getStatus())
            );
        }

        if (isTaskBlocked(taskId)) {
            TaskBlockInfo blockInfo = blockedTasks.get(taskId);
            throw new IllegalStateException(
                    String.format("La tâche est bloquée. Raison: %s. Débloquée la tâche avant de la démarrer.",
                            blockInfo.reason())
            );
        }

        UserStory userStory = task.getUserStory();
        if (userStory != null && !userStory.canBeStarted()) {
            if (!userStory.areDependenciesCompleted()) {
                throw new IllegalStateException(
                        "La User Story parente a des dépendances non complétées. " +
                                "Impossible de démarrer cette tâche."
                );
            }
        }

        if (task.getAssignedUser() == null) {
            task.assignTo(user);
        } else if (!task.getAssignedUser().getId().equals(userId)) {
            throw new IllegalStateException(
                    String.format("La tâche est déjà assignée à '%s'. Réassignez la tâche avant de la démarrer.",
                            task.getAssignedUser().getUsername())
            );
        }

        task.start();

        if (userStory != null && userStory.getStatus() == WorkItemStatus.TODO) {
            userStory.start();
        }

        taskRepository.save(task);
    }

    public Task moveTaskToReview(Long taskId) {
        Task task = getTaskById(taskId);

        if (task.getStatus() != WorkItemStatus.IN_PROGRESS) {
            throw new IllegalStateException(
                    String.format("La tâche doit être en cours pour passer en revue. Statut actuel: %s",
                            task.getStatus())
            );
        }

        task.moveToReview();
        return taskRepository.save(task);
    }

    public void moveToReview(Long taskId) {
        Task task = getTaskById(taskId);

        if (task.getStatus() != WorkItemStatus.IN_PROGRESS) {
            throw new IllegalStateException(
                    String.format("La tâche doit être en cours pour passer en revue. Statut actuel: %s",
                            task.getStatus())
            );
        }

        if (isTaskBlocked(taskId)) {
            TaskBlockInfo blockInfo = blockedTasks.get(taskId);
            throw new IllegalStateException(
                    String.format("La tâche est bloquée. Raison: %s", blockInfo.reason())
            );
        }

        if (task.getActualHours() == 0) {
            throw new IllegalStateException(
                    "Aucune heure n'a été enregistrée sur cette tâche. " +
                            "Enregistrez des heures avant de passer en revue."
            );
        }

        task.moveToReview();
        taskRepository.save(task);
    }

    public Task moveTaskToTesting(Long taskId) {
        Task task = getTaskById(taskId);

        if (task.getStatus() != WorkItemStatus.IN_REVIEW) {
            throw new IllegalStateException(
                    String.format("La tâche doit être en revue pour passer en test. Statut actuel: %s",
                            task.getStatus())
            );
        }

        task.moveToTesting();
        return taskRepository.save(task);
    }

    public void moveToTesting(Long taskId) {
        Task task = getTaskById(taskId);

        if (task.getStatus() != WorkItemStatus.IN_REVIEW) {
            throw new IllegalStateException(
                    String.format("La tâche doit être en revue pour passer en test. Statut actuel: %s",
                            task.getStatus())
            );
        }

        if (isTaskBlocked(taskId)) {
            TaskBlockInfo blockInfo = blockedTasks.get(taskId);
            throw new IllegalStateException(
                    String.format("La tâche est bloquée. Raison: %s", blockInfo.reason())
            );
        }

        task.moveToTesting();
        taskRepository.save(task);
    }

    public Task completeTask(Long taskId) {
        Task task = getTaskById(taskId);

        if (task.getStatus() != WorkItemStatus.TESTING) {
            throw new IllegalStateException("La tâche doit être testée avant d'être complétée");
        }

        task.complete();

        blockedTasks.remove(taskId);

        UserStory userStory = task.getUserStory();
        if (userStory != null && userStory.areAllTasksCompleted()) {
            userStory.complete();
        }

        return taskRepository.save(task);
    }

    public void completeTaskWorkflow(Long taskId) {
        Task task = getTaskById(taskId);

        if (task.getStatus() != WorkItemStatus.IN_PROGRESS &&
                task.getStatus() != WorkItemStatus.IN_REVIEW &&
                task.getStatus() != WorkItemStatus.TESTING) {
            throw new IllegalStateException(
                    String.format("La tâche doit être en cours, en revue ou en test pour être complétée. Statut actuel: %s",
                            task.getStatus())
            );
        }

        if (isTaskBlocked(taskId)) {
            TaskBlockInfo blockInfo = blockedTasks.get(taskId);
            throw new IllegalStateException(
                    String.format("La tâche est bloquée. Raison: %s. Débloquez la tâche avant de la compléter.",
                            blockInfo.reason())
            );
        }

        task.complete();
        blockedTasks.remove(taskId);

        UserStory userStory = task.getUserStory();
        if (userStory != null && userStory.areAllTasksCompleted()) {
            userStory.complete();
        }

        taskRepository.save(task);
    }

    // ==================== BLOCKING OPERATIONS ====================

    public Task blockTask(Long taskId, String blockReason) {
        Task task = getTaskById(taskId);

        if (task.getStatus() == WorkItemStatus.DONE) {
            throw new IllegalStateException("Impossible de bloquer une tâche terminée");
        }

        if (task.getStatus() == WorkItemStatus.TODO) {
            throw new IllegalStateException(
                    "Une tâche qui n'a pas encore démarré ne peut pas être bloquée. " +
                            "Démarrez la tâche avant de la bloquer."
            );
        }

        if (task.isBlocked()) {
            throw new IllegalStateException("La tâche est déjà bloquée");
        }

        if (blockReason == null || blockReason.trim().isEmpty()) {
            throw new IllegalArgumentException("Une raison de blocage doit être fournie");
        }

        TaskBlockInfo blockInfo = new TaskBlockInfo(
                blockReason.trim(),
                LocalDateTime.now(),
                task.getAssignedUser() != null ? task.getAssignedUser().getUsername() : "Non assignée"
        );
        blockedTasks.put(taskId, blockInfo);

        task.setBlocked(true);
        task.setBlockReason(blockReason);

        return taskRepository.save(task);
    }

    public Task unblockTask(Long taskId) {
        Task task = getTaskById(taskId);

        if (!isTaskBlocked(taskId) && !task.isBlocked()) {
            throw new IllegalStateException("La tâche n'est pas bloquée");
        }

        blockedTasks.remove(taskId);

        task.setBlocked(false);
        task.setBlockReason(null);

        if (task.getPreviousStatus() != null) {
            task.updateStatus(task.getPreviousStatus());
        } else if (task.getStatus() != WorkItemStatus.IN_PROGRESS &&
                task.getStatus() != WorkItemStatus.IN_REVIEW &&
                task.getStatus() != WorkItemStatus.TESTING) {
            task.updateStatus(WorkItemStatus.IN_PROGRESS);
        }

        return taskRepository.save(task);
    }

    @Transactional(readOnly = true)
    public boolean isTaskBlocked(Long taskId) {
        return blockedTasks.containsKey(taskId);
    }

    @Transactional(readOnly = true)
    public TaskBlockInfo getTaskBlockInfo(Long taskId) {
        return blockedTasks.get(taskId);
    }

    // ==================== ADVANCED OPERATIONS ====================

    public void reassignTask(Long taskId, Long newUserId) {
        Task task = getTaskById(taskId);
        User newUser = userRepository.findById(newUserId)
                .orElseThrow(() -> new IllegalArgumentException("Utilisateur non trouvé avec l'ID: " + newUserId));

        if (task.getStatus() == WorkItemStatus.DONE) {
            throw new IllegalStateException("Impossible de réassigner une tâche terminée");
        }

        task.assignTo(newUser);
        taskRepository.save(task);
    }

    public void moveTaskBackward(Long taskId, String reason) {
        Task task = getTaskById(taskId);

        WorkItemStatus newStatus = switch (task.getStatus()) {
            case IN_REVIEW -> WorkItemStatus.IN_PROGRESS;
            case TESTING -> WorkItemStatus.IN_REVIEW;
            case DONE -> throw new IllegalStateException("Impossible de faire reculer une tâche terminée");
            default -> throw new IllegalStateException(
                    String.format("Impossible de faire reculer une tâche en statut %s", task.getStatus())
            );
        };

        task.updateStatus(newStatus);
        taskRepository.save(task);
    }

    public void reassignUserTasks(Long fromUserId, Long toUserId) {
        List<Task> tasks = taskRepository.findByAssignedUserId(fromUserId);
        User toUser = userRepository.findById(toUserId)
                .orElseThrow(() -> new IllegalArgumentException("Utilisateur non trouvé avec l'ID: " + toUserId));

        for (Task task : tasks) {
            if (task.getStatus() != WorkItemStatus.DONE) {
                task.assignTo(toUser);
                taskRepository.save(task);
            }
        }
    }

    // ==================== QUERY OPERATIONS ====================

    @Transactional(readOnly = true)
    public List<Task> getUnassignedTasksBySprint(Long sprintId) {
        return taskRepository.findBySprintBacklogIdAndAssignedUserIsNull(sprintId);
    }

    @Transactional(readOnly = true)
    public List<Task> findOverEstimatedTasksBySprint(Long sprintId) {
        return taskRepository.findOverEstimatedTasksBySprint(sprintId);
    }

    @Transactional(readOnly = true)
    public List<Task> getTasksBySprintAndStatus(Long sprintId, WorkItemStatus status) {
        return taskRepository.findBySprintBacklogIdAndStatus(sprintId, status);
    }

    @Transactional(readOnly = true)
    public List<Task> getBlockedTasks(Integer sprintId) {
        return taskRepository.findBySprintBacklogIdAndIsBlocked(sprintId, true);
    }

    @Transactional(readOnly = true)
    public List<Task> getTasksExceedingEstimate(Integer sprintId) {
        return taskRepository.findTasksExceedingEstimate(sprintId);
    }

    @Transactional(readOnly = true)
    public List<Task> getCriticalTasks(Integer sprintNumber) {
        return taskRepository.findCriticalTasks(sprintNumber);
    }

    @Transactional(readOnly = true)
    public List<Task> getRecentlyCompletedTasks(Integer sprintId, int days) {
        return taskRepository.findRecentlyCompletedTasks(sprintId, days);
    }

    @Transactional(readOnly = true)
    public List<Task> getUserTasksForSprint(Long userId, Long sprintId) {
        return taskRepository.findByAssignedUserIdAndSprintBacklogId(userId, sprintId);
    }

    @Transactional(readOnly = true)
    public int calculateRemainingHours(Long sprintId) {
        List<Task> tasks = taskRepository.findBySprintBacklogId(sprintId.intValue());
        return tasks.stream()
                .filter(task -> task.getStatus() != WorkItemStatus.DONE)
                .mapToInt(Task::getRemainingHours)
                .sum();
    }

    @Transactional(readOnly = true)
    public boolean canDeleteTask(Long taskId) {
        Task task = getTaskById(taskId);
        return task.getStatus() != WorkItemStatus.DONE && task.getActualHours() == 0;
    }

    // ==================== METRICS & STATISTICS ====================

    @Transactional(readOnly = true)
    public UserStoryTaskMetrics getUserStoryTaskMetrics(Long userStoryId) {
        List<Task> tasks = taskRepository.findByUserStoryId(userStoryId);

        int totalTasks = tasks.size();
        long completedTasks = tasks.stream()
                .filter(task -> task.getStatus() == WorkItemStatus.DONE)
                .count();
        long inProgressTasks = tasks.stream()
                .filter(task -> task.getStatus() == WorkItemStatus.IN_PROGRESS)
                .count();
        long todoTasks = tasks.stream()
                .filter(task -> task.getStatus() == WorkItemStatus.TODO)
                .count();

        double progressPercentage = totalTasks > 0 ? (completedTasks * 100.0) / totalTasks : 0.0;

        int totalEstimatedHours = tasks.stream().mapToInt(Task::getEstimatedHours).sum();
        int totalActualHours = tasks.stream().mapToInt(Task::getActualHours).sum();

        return new UserStoryTaskMetrics(
                totalTasks,
                (int) completedTasks,
                (int) inProgressTasks,
                (int) todoTasks,
                progressPercentage,
                totalEstimatedHours,
                totalActualHours
        );
    }

    @Transactional(readOnly = true)
    public SprintTaskStatistics getSprintTaskStatistics(Integer sprintId) {
        List<Task> tasks = taskRepository.findBySprintBacklogId(sprintId);

        int totalTasks = tasks.size();
        long todoTasks = tasks.stream().filter(t -> t.getStatus() == WorkItemStatus.TODO).count();
        long inProgressTasks = tasks.stream().filter(t -> t.getStatus() == WorkItemStatus.IN_PROGRESS).count();
        long inReviewTasks = tasks.stream().filter(t -> t.getStatus() == WorkItemStatus.IN_REVIEW).count();
        long testingTasks = tasks.stream().filter(t -> t.getStatus() == WorkItemStatus.TESTING).count();
        long doneTasks = tasks.stream().filter(t -> t.getStatus() == WorkItemStatus.DONE).count();
        long blockedTasks = tasks.stream().filter(Task::isBlocked).count();

        int totalEstimatedHours = tasks.stream().mapToInt(Task::getEstimatedHours).sum();
        int totalActualHours = tasks.stream().mapToInt(Task::getActualHours).sum();
        int totalRemainingHours = tasks.stream()
                .filter(t -> t.getStatus() != WorkItemStatus.DONE)
                .mapToInt(Task::getRemainingHours)
                .sum();

        double completionRate = totalTasks > 0 ? (doneTasks * 100.0) / totalTasks : 0.0;

        return new SprintTaskStatistics(
                totalTasks,
                (int) todoTasks,
                (int) inProgressTasks,
                (int) inReviewTasks,
                (int) testingTasks,
                (int) doneTasks,
                (int) blockedTasks,
                totalEstimatedHours,
                totalActualHours,
                totalRemainingHours,
                completionRate
        );
    }

    // ==================== RECORDS ====================

    public record TaskBlockInfo(
            String reason,
            LocalDateTime blockedAt,
            String blockedBy
    ) {}

    public record UserStoryTaskMetrics(
            int totalTasks,
            int completedTasks,
            int inProgressTasks,
            int todoTasks,
            double progressPercentage,
            int totalEstimatedHours,
            int totalActualHours
    ) {}

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
            int totalRemainingHours,
            double completionRate
    ) {}
}