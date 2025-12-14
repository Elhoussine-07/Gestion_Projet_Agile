package com.Agile.demo.execution.workflow;

import com.Agile.demo.execution.repositories.TaskRepository;
import com.Agile.demo.execution.repositories.UserRepository;
import com.Agile.demo.model.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Service de gestion du workflow des tâches
 * Gère les transitions d'état et les règles métier des tâches
 */
@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class TaskWorkflowService {

    private final TaskRepository taskRepository;
    private final UserRepository userRepository;

    // Map pour stocker les raisons de blocage des tâches
    private final Map<Long, TaskBlockInfo> blockedTasks = new HashMap<>();

    /**
     * Démarre une tâche et l'assigne à un utilisateur
     *
     * @param taskId ID de la tâche
     * @param userId ID de l'utilisateur
     * @throws IllegalStateException si la tâche ne peut pas être démarrée
     */
    public void startTask(Long taskId, Long userId) {
        log.info("Tentative de démarrage de la tâche ID: {} par l'utilisateur ID: {}", taskId, userId);

        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new IllegalArgumentException("Tâche non trouvée avec l'ID: " + taskId));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Utilisateur non trouvé avec l'ID: " + userId));

        // RÈGLE MÉTIER: La tâche doit être en statut TODO
        if (task.getStatus() != WorkItemStatus.TODO) {
            throw new IllegalStateException(
                    String.format("La tâche doit être en statut TODO pour être démarrée. Statut actuel: %s",
                            task.getStatus())
            );
        }

        // RÈGLE MÉTIER: La tâche ne doit pas être bloquée
        if (isTaskBlocked(taskId)) {
            TaskBlockInfo blockInfo = blockedTasks.get(taskId);
            throw new IllegalStateException(
                    String.format("La tâche est bloquée. Raison: %s. Débloquée la tâche avant de la démarrer.",
                            blockInfo.reason())
            );
        }

        // RÈGLE MÉTIER: Vérifier que la User Story parente peut être démarrée
        UserStory userStory = task.getUserStory();
        if (userStory != null && !userStory.canBeStarted()) {
            if (!userStory.areDependenciesCompleted()) {
                throw new IllegalStateException(
                        "La User Story parente a des dépendances non complétées. " +
                                "Impossible de démarrer cette tâche."
                );
            }
        }

        // RÈGLE MÉTIER: Assigner la tâche si elle ne l'est pas déjà
        if (task.getAssignedUser() == null) {
            task.assignTo(user);
            log.info("Tâche ID: {} assignée à l'utilisateur '{}'", taskId, user.getUsername());
        } else if (!task.getAssignedUser().getId().equals(userId)) {
            // Si la tâche est déjà assignée à quelqu'un d'autre
            throw new IllegalStateException(
                    String.format("La tâche est déjà assignée à '%s'. Réassignez la tâche avant de la démarrer.",
                            task.getAssignedUser().getUsername())
            );
        }

        // Démarrer la tâche
        task.start();

        // Si c'est la première tâche de la User Story, la démarrer aussi
        if (userStory != null && userStory.getStatus() == WorkItemStatus.TODO) {
            userStory.start();
            log.info("User Story '{}' démarrée automatiquement", userStory.getTitle());
        }

        taskRepository.save(task);

        log.info("Tâche '{}' démarrée avec succès par '{}'", task.getTitle(), user.getUsername());
    }

    /**
     * Déplace une tâche en revue
     *
     * @param taskId ID de la tâche
     */
    public void moveToReview(Long taskId) {
        log.info("Déplacement de la tâche ID: {} vers IN_REVIEW", taskId);

        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new IllegalArgumentException("Tâche non trouvée avec l'ID: " + taskId));

        // RÈGLE MÉTIER: La tâche doit être IN_PROGRESS
        if (task.getStatus() != WorkItemStatus.IN_PROGRESS) {
            throw new IllegalStateException(
                    String.format("La tâche doit être en cours pour passer en revue. Statut actuel: %s",
                            task.getStatus())
            );
        }

        // RÈGLE MÉTIER: La tâche ne doit pas être bloquée
        if (isTaskBlocked(taskId)) {
            TaskBlockInfo blockInfo = blockedTasks.get(taskId);
            throw new IllegalStateException(
                    String.format("La tâche est bloquée. Raison: %s", blockInfo.reason())
            );
        }

        // RÈGLE MÉTIER: Vérifier qu'un minimum de travail a été effectué
        if (task.getActualHours() == 0) {
            throw new IllegalStateException(
                    "Aucune heure n'a été enregistrée sur cette tâche. " +
                            "Enregistrez des heures avant de passer en revue."
            );
        }

        task.moveToReview();
        taskRepository.save(task);

        log.info("Tâche '{}' déplacée en revue avec succès", task.getTitle());
    }

    /**
     * Déplace une tâche en test
     *
     * @param taskId ID de la tâche
     */
    public void moveToTesting(Long taskId) {
        log.info("Déplacement de la tâche ID: {} vers TESTING", taskId);

        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new IllegalArgumentException("Tâche non trouvée avec l'ID: " + taskId));

        // RÈGLE MÉTIER: La tâche doit être IN_REVIEW
        if (task.getStatus() != WorkItemStatus.IN_REVIEW) {
            throw new IllegalStateException(
                    String.format("La tâche doit être en revue pour passer en test. Statut actuel: %s",
                            task.getStatus())
            );
        }

        // RÈGLE MÉTIER: La tâche ne doit pas être bloquée
        if (isTaskBlocked(taskId)) {
            TaskBlockInfo blockInfo = blockedTasks.get(taskId);
            throw new IllegalStateException(
                    String.format("La tâche est bloquée. Raison: %s", blockInfo.reason())
            );
        }

        task.moveToTesting();
        taskRepository.save(task);

        log.info("Tâche '{}' déplacée en test avec succès", task.getTitle());
    }

    /**
     * Complète une tâche
     *
     * @param taskId ID de la tâche
     */
    public void completeTask(Long taskId) {
        log.info("Tentative de complétion de la tâche ID: {}", taskId);

        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new IllegalArgumentException("Tâche non trouvée avec l'ID: " + taskId));

        // RÈGLE MÉTIER: La tâche doit être dans un état permettant la complétion
        if (task.getStatus() != WorkItemStatus.IN_PROGRESS &&
                task.getStatus() != WorkItemStatus.IN_REVIEW &&
                task.getStatus() != WorkItemStatus.TESTING) {
            throw new IllegalStateException(
                    String.format("La tâche doit être en cours, en revue ou en test pour être complétée. Statut actuel: %s",
                            task.getStatus())
            );
        }

        // RÈGLE MÉTIER: La tâche ne doit pas être bloquée
        if (isTaskBlocked(taskId)) {
            TaskBlockInfo blockInfo = blockedTasks.get(taskId);
            throw new IllegalStateException(
                    String.format("La tâche est bloquée. Raison: %s. Débloquez la tâche avant de la compléter.",
                            blockInfo.reason())
            );
        }

        // RÈGLE MÉTIER: Vérifier qu'au moins une heure a été enregistrée
        if (task.getActualHours() == 0) {
            log.warn("Complétion d'une tâche sans heures enregistrées: {}", task.getTitle());
        }

        // Compléter la tâche
        task.complete();

        // Supprimer l'info de blocage si elle existe
        blockedTasks.remove(taskId);

        // RÈGLE MÉTIER: Vérifier si toutes les tâches de la User Story sont complétées
        UserStory userStory = task.getUserStory();
        if (userStory != null && userStory.areAllTasksCompleted()) {
            userStory.complete();
            log.info("User Story '{}' complétée automatiquement (toutes les tâches sont terminées)",
                    userStory.getTitle());
        }

        taskRepository.save(task);

        log.info("Tâche '{}' complétée avec succès", task.getTitle());
    }

    /**
     * Bloque une tâche avec une raison
     *
     * @param taskId ID de la tâche
     * @param reason Raison du blocage
     */
    public void blockTask(Long taskId, String reason) {
        log.info("Blocage de la tâche ID: {} avec raison: {}", taskId, reason);

        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new IllegalArgumentException("Tâche non trouvée avec l'ID: " + taskId));

        // RÈGLE MÉTIER: Ne peut pas bloquer une tâche terminée
        if (task.getStatus() == WorkItemStatus.DONE) {
            throw new IllegalStateException("Impossible de bloquer une tâche terminée");
        }

        // RÈGLE MÉTIER: Une tâche TODO ne peut pas être bloquée
        if (task.getStatus() == WorkItemStatus.TODO) {
            throw new IllegalStateException(
                    "Une tâche qui n'a pas encore démarré ne peut pas être bloquée. " +
                            "Démarrez la tâche avant de la bloquer."
            );
        }

        // Valider la raison
        if (reason == null || reason.trim().isEmpty()) {
            throw new IllegalArgumentException("Une raison de blocage doit être fournie");
        }

        // Enregistrer le blocage
        TaskBlockInfo blockInfo = new TaskBlockInfo(
                reason.trim(),
                LocalDateTime.now(),
                task.getAssignedUser() != null ? task.getAssignedUser().getUsername() : "Non assignée"
        );
        blockedTasks.put(taskId, blockInfo);

        // Changer le statut vers BLOCKED (si ce statut existe dans votre enum)
        // Sinon, on peut utiliser un flag ou garder le statut actuel
        // task.updateStatus(WorkItemStatus.BLOCKED);

        taskRepository.save(task);

        log.info("Tâche '{}' bloquée. Raison: {}", task.getTitle(), reason);
    }

    /**
     * Débloque une tâche
     *
     * @param taskId ID de la tâche
     */
    public void unblockTask(Long taskId) {
        log.info("Déblocage de la tâche ID: {}", taskId);

        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new IllegalArgumentException("Tâche non trouvée avec l'ID: " + taskId));

        // RÈGLE MÉTIER: Vérifier que la tâche est effectivement bloquée
        if (!isTaskBlocked(taskId)) {
            throw new IllegalStateException("La tâche n'est pas bloquée");
        }

        // Supprimer l'information de blocage
        TaskBlockInfo removedBlock = blockedTasks.remove(taskId);

        // Restaurer le statut IN_PROGRESS si nécessaire
        if (task.getStatus() != WorkItemStatus.IN_PROGRESS &&
                task.getStatus() != WorkItemStatus.IN_REVIEW &&
                task.getStatus() != WorkItemStatus.TESTING) {
            task.updateStatus(WorkItemStatus.IN_PROGRESS);
        }

        taskRepository.save(task);

        log.info("Tâche '{}' débloquée. Elle était bloquée pour: {}",
                task.getTitle(), removedBlock.reason());
    }

    /**
     * Vérifie si une tâche est bloquée
     *
     * @param taskId ID de la tâche
     * @return true si la tâche est bloquée
     */
    @Transactional(readOnly = true)
    public boolean isTaskBlocked(Long taskId) {
        return blockedTasks.containsKey(taskId);
    }

    /**
     * Récupère les informations de blocage d'une tâche
     *
     * @param taskId ID de la tâche
     * @return Informations de blocage ou null si non bloquée
     */
    @Transactional(readOnly = true)
    public TaskBlockInfo getTaskBlockInfo(Long taskId) {
        return blockedTasks.get(taskId);
    }

    /**
     * Réassigne une tâche à un autre utilisateur
     *
     * @param taskId ID de la tâche
     * @param newUserId ID du nouvel utilisateur
     */
    public void reassignTask(Long taskId, Long newUserId) {
        log.info("Réassignation de la tâche ID: {} à l'utilisateur ID: {}", taskId, newUserId);

        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new IllegalArgumentException("Tâche non trouvée avec l'ID: " + taskId));

        User newUser = userRepository.findById(newUserId)
                .orElseThrow(() -> new IllegalArgumentException("Utilisateur non trouvé avec l'ID: " + newUserId));

        // RÈGLE MÉTIER: Ne peut pas réassigner une tâche terminée
        if (task.getStatus() == WorkItemStatus.DONE) {
            throw new IllegalStateException("Impossible de réassigner une tâche terminée");
        }

        String oldAssignee = task.getAssignedUser() != null
                ? task.getAssignedUser().getUsername()
                : "Non assignée";

        task.assignTo(newUser);
        taskRepository.save(task);

        log.info("Tâche '{}' réassignée de '{}' à '{}'",
                task.getTitle(), oldAssignee, newUser.getUsername());
    }

    /**
     * Fait reculer une tâche d'un statut (ex: de REVIEW vers IN_PROGRESS)
     * Utile quand une revue échoue
     *
     * @param taskId ID de la tâche
     * @param reason Raison du retour en arrière
     */
    public void moveTaskBackward(Long taskId, String reason) {
        log.info("Retour en arrière de la tâche ID: {} pour raison: {}", taskId, reason);

        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new IllegalArgumentException("Tâche non trouvée avec l'ID: " + taskId));

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

        log.info("Tâche '{}' renvoyée de {} vers {}. Raison: {}",
                task.getTitle(), task.getStatus(), newStatus, reason);
    }

    /**
     * Record pour stocker les informations de blocage d'une tâche
     */
    public record TaskBlockInfo(
            String reason,
            LocalDateTime blockedAt,
            String blockedBy
    ) {}
}