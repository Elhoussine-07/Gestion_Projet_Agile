package com.Agile.demo.execution.services;

import com.Agile.demo.execution.dto.mapper.TaskMapper;
import com.Agile.demo.execution.dto.task.*;
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
    private final TaskMapper taskMapper;

    // Gestion mémoire des blocages (similaire à votre version précédente)
    private final Map<Long, TaskBlockInfo> blockedTasks = new HashMap<>();

    // ==================== CRÉATION ====================

    public TaskResponseDTO createTask(CreateTaskRequest request) {
        validateTaskCreation(request);

        UserStory userStory = userStoryRepository.findById(request.getUserStoryId())
                .orElseThrow(() -> new IllegalArgumentException("User Story non trouvée avec l'ID: " + request.getUserStoryId()));

        Task task = taskMapper.toEntity(request);
        task.setUserStory(userStory);
        userStory.addTask(task);

        // Gestion de l'assignation optionnelle à la création
        if (request.getAssignedUserId() != null) {
            User user = userRepository.findById(request.getAssignedUserId())
                    .orElseThrow(() -> new IllegalArgumentException("Utilisateur non trouvé: " + request.getAssignedUserId()));
            task.assignTo(user);
        }

        Task savedTask = taskRepository.save(task);
        return taskMapper.toResponseDTO(savedTask);
    }

    private void validateTaskCreation(CreateTaskRequest request) {
        if (request.getTitle() == null || request.getTitle().trim().isEmpty()) {
            throw new IllegalArgumentException("Le titre de la tâche est obligatoire");
        }
        if (request.getEstimatedHours() == null || request.getEstimatedHours() < 0) {
            throw new IllegalArgumentException("Les heures estimées doivent être positives");
        }
        if (request.getUserStoryId() == null) {
            throw new IllegalArgumentException("Une tâche doit être liée à une User Story");
        }
    }

    // ==================== LECTURE ====================

    @Transactional(readOnly = true)
    public TaskResponseDTO getTaskById(Long taskId) {
        Task task = findTaskByIdOrThrow(taskId);
        return taskMapper.toResponseDTO(task);
    }

    @Transactional(readOnly = true)
    public List<TaskResponseDTO> getTasksByUserStory(Long userStoryId) {
        List<Task> tasks = taskRepository.findByUserStoryId(userStoryId);
        return taskMapper.toResponseDTOList(tasks);
    }

    @Transactional(readOnly = true)
    public List<TaskResponseDTO> getTasksBySprint(Integer sprintId) {
        List<Task> tasks = taskRepository.findBySprintBacklogId(sprintId);
        return taskMapper.toResponseDTOList(tasks);
    }

    @Transactional(readOnly = true)
    public List<TaskResponseDTO> getTasksByUser(Long userId) {
        List<Task> tasks = taskRepository.findByAssignedUserId(userId);
        return taskMapper.toResponseDTOList(tasks);
    }

    @Transactional(readOnly = true)
    public List<TaskResponseDTO> getTasksByStatus(Long sprintId, WorkItemStatus status) {
        List<Task> tasks = taskRepository.findBySprintBacklogIdAndStatus(sprintId, status);
        return taskMapper.toResponseDTOList(tasks);
    }

    @Transactional(readOnly = true)
    public List<TaskResponseDTO> getUnassignedTasksBySprint(Long sprintId) {
        List<Task> tasks = taskRepository.findBySprintBacklogIdAndAssignedUserIsNull(sprintId);
        return taskMapper.toResponseDTOList(tasks);
    }

    // ==================== MISE À JOUR (Générale) ====================

    public TaskResponseDTO updateTask(Long taskId, UpdateTaskRequest request) {
        Task task = findTaskByIdOrThrow(taskId);

        if (request.getEstimatedHours() != null && request.getEstimatedHours() < 0) {
            throw new IllegalArgumentException("Les heures estimées ne peuvent pas être négatives");
        }

        taskMapper.updateEntityFromDTO(request, task);

        Task savedTask = taskRepository.save(task);
        return taskMapper.toResponseDTO(savedTask);
    }

    // ==================== GESTION DE L'EFFORT (Log Hours) ====================

    public TaskResponseDTO logHours(Long taskId, LogHoursRequest request) {
        Task task = findTaskByIdOrThrow(taskId);

        if (request.getHours() == null || request.getHours() <= 0) {
            throw new IllegalArgumentException("Le nombre d'heures doit être positif");
        }

        task.logHours(request.getHours());
        Task savedTask = taskRepository.save(task);
        return taskMapper.toResponseDTO(savedTask);
    }

    // ==================== ASSIGNATION ====================

    public TaskResponseDTO assignTask(Long taskId, Long userId) {
        Task task = findTaskByIdOrThrow(taskId);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Utilisateur non trouvé: " + userId));

        if (task.getStatus() == WorkItemStatus.DONE) {
            throw new IllegalStateException("Impossible de réassigner une tâche terminée");
        }

        task.assignTo(user);
        Task savedTask = taskRepository.save(task);
        return taskMapper.toResponseDTO(savedTask);
    }

    public TaskResponseDTO unassignTask(Long taskId) {
        Task task = findTaskByIdOrThrow(taskId);

        if (task.getStatus() == WorkItemStatus.IN_PROGRESS) {
            throw new IllegalStateException("Impossible de désassigner une tâche en cours");
        }

        task.unassign();
        Task savedTask = taskRepository.save(task);
        return taskMapper.toResponseDTO(savedTask);
    }

    public void reassignUserTasks(Long fromUserId, Long toUserId) {
        List<Task> tasks = taskRepository.findByAssignedUserId(fromUserId);
        User toUser = userRepository.findById(toUserId)
                .orElseThrow(() -> new IllegalArgumentException("Utilisateur cible non trouvé"));

        for (Task task : tasks) {
            if (task.getStatus() != WorkItemStatus.DONE) {
                task.assignTo(toUser);
                taskRepository.save(task);
            }
        }
    }

    // ==================== WORKFLOW (Status) ====================

    public TaskResponseDTO startTask(Long taskId, Long userId) {
        Task task = findTaskByIdOrThrow(taskId);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Utilisateur non trouvé: " + userId));

        validateTaskStart(task, userId);

        if (task.getAssignedUser() == null) {
            task.assignTo(user);
        }

        task.start();

        // Propagation au UserStory parent
        UserStory userStory = task.getUserStory();
        if (userStory != null && userStory.getStatus() == WorkItemStatus.TODO) {
            userStory.start();
        }

        return taskMapper.toResponseDTO(taskRepository.save(task));
    }

    private void validateTaskStart(Task task, Long userId) {
        if (task.getStatus() != WorkItemStatus.TODO) {
            throw new IllegalStateException("La tâche doit être en statut TODO pour être démarrée.");
        }
        if (isTaskBlocked(task.getId())) {
            TaskBlockInfo blockInfo = blockedTasks.get(task.getId());
            throw new IllegalStateException("La tâche est bloquée. Raison: " + blockInfo.reason());
        }
        if (task.getAssignedUser() != null && !task.getAssignedUser().getId().equals(userId)) {
            throw new IllegalStateException("La tâche est déjà assignée à un autre utilisateur.");
        }
        UserStory us = task.getUserStory();
        if (us != null && !us.canBeStarted() && !us.areDependenciesCompleted()) {
            throw new IllegalStateException("Les dépendances de la User Story ne sont pas terminées.");
        }
    }

    public TaskResponseDTO moveTaskToReview(Long taskId) {
        Task task = findTaskByIdOrThrow(taskId);

        if (task.getStatus() != WorkItemStatus.IN_PROGRESS) throw new IllegalStateException("La tâche doit être en cours");
        if (isTaskBlocked(taskId)) throw new IllegalStateException("Tâche bloquée");
        if (task.getActualHours() == 0) throw new IllegalStateException("Aucune heure enregistrée");

        task.moveToReview();
        return taskMapper.toResponseDTO(taskRepository.save(task));
    }

    public TaskResponseDTO moveTaskToTesting(Long taskId) {
        Task task = findTaskByIdOrThrow(taskId);

        if (task.getStatus() != WorkItemStatus.IN_REVIEW) throw new IllegalStateException("La tâche doit être en revue");
        if (isTaskBlocked(taskId)) throw new IllegalStateException("Tâche bloquée");

        task.moveToTesting();
        return taskMapper.toResponseDTO(taskRepository.save(task));
    }

    public TaskResponseDTO completeTask(Long taskId) {
        Task task = findTaskByIdOrThrow(taskId);

        if (task.getStatus() != WorkItemStatus.TESTING &&
                task.getStatus() != WorkItemStatus.IN_REVIEW &&
                task.getStatus() != WorkItemStatus.IN_PROGRESS) {
            throw new IllegalStateException("Statut invalide pour la complétion");
        }

        if (isTaskBlocked(taskId)) throw new IllegalStateException("Tâche bloquée");

        task.complete();
        blockedTasks.remove(taskId);

        UserStory userStory = task.getUserStory();
        if (userStory != null && userStory.areAllTasksCompleted()) {
            userStory.complete();
        }

        return taskMapper.toResponseDTO(taskRepository.save(task));
    }

    // ==================== BLOCAGE ====================

    public TaskResponseDTO blockTask(Long taskId, BlockTaskRequest request) {
        Task task = findTaskByIdOrThrow(taskId);

        if (task.getStatus() == WorkItemStatus.DONE) throw new IllegalStateException("Impossible de bloquer une tâche terminée");
        if (task.getStatus() == WorkItemStatus.TODO) throw new IllegalStateException("Une tâche non démarrée ne peut pas être bloquée");
        if (task.isBlocked()) throw new IllegalStateException("La tâche est déjà bloquée");

        TaskBlockInfo blockInfo = new TaskBlockInfo(
                request.getReason(),
                LocalDateTime.now(),
                task.getAssignedUser() != null ? task.getAssignedUser().getUsername() : "Système"
        );
        blockedTasks.put(taskId, blockInfo);
        task.setStatus(WorkItemStatus.BLOCKED);
        task.setBlocked(true);
        task.setBlockReason(request.getReason());

        return taskMapper.toResponseDTO(taskRepository.save(task));
    }

    public TaskResponseDTO unblockTask(Long taskId) {
        Task task = findTaskByIdOrThrow(taskId);

        if (!isTaskBlocked(taskId) && !task.isBlocked()) {
            throw new IllegalStateException("La tâche n'est pas bloquée");
        }

        blockedTasks.remove(taskId);
        task.setBlocked(false);
        task.setBlockReason(null);

        // Logique de retour au statut précédent ou défaut
        if (task.getPreviousStatus() != null) {
            task.updateStatus(task.getPreviousStatus());
        } else {
            task.updateStatus(WorkItemStatus.IN_PROGRESS);
        }

        return taskMapper.toResponseDTO(taskRepository.save(task));
    }

    // ==================== SUPPRESSION / DUPLICATION ====================

    public void deleteTask(Long taskId) {
        Task task = findTaskByIdOrThrow(taskId);

        if (task.getStatus() == WorkItemStatus.DONE) {
            throw new IllegalStateException("Impossible de supprimer une tâche terminée");
        }
        if (task.getActualHours() > 0) {
            // Optionnel : empêcher la suppression si du temps a été logué
            // throw new IllegalStateException("Impossible de supprimer une tâche avec des heures enregistrées");
        }

        taskRepository.delete(task);
    }

    public TaskResponseDTO duplicateTask(Long taskId) {
        Task originalTask = findTaskByIdOrThrow(taskId);

        Task duplicatedTask = new Task(
                originalTask.getTitle() + " (Copie)",
                originalTask.getEstimatedHours()
        );
        duplicatedTask.setDescription(originalTask.getDescription());
        duplicatedTask.setUserStory(originalTask.getUserStory());

        return taskMapper.toResponseDTO(taskRepository.save(duplicatedTask));
    }

    // ==================== STATISTIQUES (Records) ====================

    @Transactional(readOnly = true)
    public UserStoryTaskMetrics getUserStoryTaskMetrics(Long userStoryId) {
        List<Task> tasks = taskRepository.findByUserStoryId(userStoryId);

        int totalTasks = tasks.size();
        long completedTasks = tasks.stream().filter(t -> t.getStatus() == WorkItemStatus.DONE).count();
        long inProgressTasks = tasks.stream().filter(t -> t.getStatus() == WorkItemStatus.IN_PROGRESS).count();
        long todoTasks = tasks.stream().filter(t -> t.getStatus() == WorkItemStatus.TODO).count();

        double progressPercentage = totalTasks > 0 ? (completedTasks * 100.0) / totalTasks : 0.0;
        int totalEstimated = tasks.stream().mapToInt(Task::getEstimatedHours).sum();
        int totalActual = tasks.stream().mapToInt(Task::getActualHours).sum();

        return new UserStoryTaskMetrics(
                totalTasks, (int) completedTasks, (int) inProgressTasks, (int) todoTasks,
                progressPercentage, totalEstimated, totalActual
        );
    }

    @Transactional(readOnly = true)
    public SprintTaskStatistics getSprintTaskStatistics(Integer sprintId) {
        List<Task> tasks = taskRepository.findBySprintBacklogId(sprintId);

        int totalTasks = tasks.size();
        long todo = tasks.stream().filter(t -> t.getStatus() == WorkItemStatus.TODO).count();
        long inProgress = tasks.stream().filter(t -> t.getStatus() == WorkItemStatus.IN_PROGRESS).count();
        long inReview = tasks.stream().filter(t -> t.getStatus() == WorkItemStatus.IN_REVIEW).count();
        long testing = tasks.stream().filter(t -> t.getStatus() == WorkItemStatus.TESTING).count();
        long done = tasks.stream().filter(t -> t.getStatus() == WorkItemStatus.DONE).count();
        long blocked = tasks.stream().filter(Task::isBlocked).count();

        int estHours = tasks.stream().mapToInt(Task::getEstimatedHours).sum();
        int actHours = tasks.stream().mapToInt(Task::getActualHours).sum();
        int remHours = tasks.stream().filter(t -> t.getStatus() != WorkItemStatus.DONE)
                .mapToInt(Task::getRemainingHours).sum();

        double rate = totalTasks > 0 ? (done * 100.0) / totalTasks : 0.0;

        return new SprintTaskStatistics(
                totalTasks, (int) todo, (int) inProgress, (int) inReview, (int) testing, (int) done,
                (int) blocked, estHours, actHours, remHours, rate
        );
    }

    // ==================== MÉTHODES PRIVÉES ====================

    private Task findTaskByIdOrThrow(Long taskId) {
        return taskRepository.findById(taskId)
                .orElseThrow(() -> new IllegalArgumentException("Tâche non trouvée avec l'ID: " + taskId));
    }

    @Transactional(readOnly = true)
    public boolean isTaskBlocked(Long taskId) {
        return blockedTasks.containsKey(taskId);
    }

    @Transactional(readOnly = true)
    public TaskBlockInfo getTaskBlockInfo(Long taskId) {
        return blockedTasks.get(taskId);
    }

    // ==================== RECORDS ====================

    public record TaskBlockInfo(String reason, LocalDateTime blockedAt, String blockedBy) {}

    public record UserStoryTaskMetrics(
            int totalTasks, int completedTasks, int inProgressTasks, int todoTasks,
            double progressPercentage, int totalEstimatedHours, int totalActualHours
    ) {}

    public record SprintTaskStatistics(
            int totalTasks, int todoTasks, int inProgressTasks, int inReviewTasks, int testingTasks,
            int doneTasks, int blockedTasks, int totalEstimatedHours, int totalActualHours,
            int totalRemainingHours, double completionRate
    ) {}
}