package com.Agile.demo.execution.services;

import com.Agile.demo.model.*;
import com.Agile.demo.execution.repositories.TaskRepository;
import com.Agile.demo.execution.repositories.UserRepository;
import com.Agile.demo.execution.repositories.UserStoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class TaskService {

    private final TaskRepository taskRepository;
    private final UserStoryRepository userStoryRepository;
    private final UserRepository userRepository;

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
    public List<Task> getTasksBySprint(Long sprintBacklogId) {
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
}