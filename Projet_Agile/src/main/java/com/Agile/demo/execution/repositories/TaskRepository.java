package com.Agile.demo.execution.repositories;
import java.util.List;
import java.time.LocalDate;
import com.Agile.demo.model.Task;
import com.Agile.demo.model.WorkItemStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TaskRepository extends JpaRepository<Task, Long> {

    /**
     * Trouve toutes les tâches d'une User Story
     */
    List<Task> findByUserStoryId(Long userStoryId);

    /**
     * Trouve toutes les tâches d'un Sprint Backlog
     */
    List<Task> findBySprintBacklogId(Integer sprintBacklogId);

    /**
     * Trouve toutes les tâches assignées à un utilisateur
     */
    List<Task> findByAssignedUserId(Long userId);

    /**
     * Trouve les tâches assignées à un utilisateur avec un statut spécifique
     */
    List<Task> findByAssignedUserIdAndStatus(Long userId, WorkItemStatus status);

    /**
     * Trouve les tâches non assignées d'un sprint
     */
    List<Task> findBySprintBacklogIdAndAssignedUserIsNull(Long sprintBacklogId);

    /**
     * Trouve les tâches par statut dans un sprint
     */
    List<Task> findBySprintBacklogIdAndStatus(Long sprintBacklogId, WorkItemStatus status);

    /**
     * Calcule le total des heures estimées pour une User Story
     */
    @Query("SELECT COALESCE(SUM(t.estimatedHours), 0) FROM Task t WHERE t.userStory.id = :userStoryId")
    Integer getTotalEstimatedHoursByUserStory(@Param("userStoryId") Long userStoryId);

    /**
     * Calcule le total des heures réelles pour une User Story
     */
    @Query("SELECT COALESCE(SUM(t.actualHours), 0) FROM Task t WHERE t.userStory.id = :userStoryId")
    Integer getTotalActualHoursByUserStory(@Param("userStoryId") Long userStoryId);

    /**
     * Compte les tâches complétées d'une User Story
     */
    long countByUserStoryIdAndStatus(Long userStoryId, WorkItemStatus status);

    /**
     * Trouve les tâches en retard (heures réelles > heures estimées)
     */
    @Query("SELECT t FROM Task t WHERE t.sprintBacklog.id = :sprintBacklogId AND t.actualHours > t.estimatedHours")
    List<Task> findOverEstimatedTasksBySprint(@Param("sprintBacklogId") Long sprintBacklogId);

    List<Task> findBySprintBacklogIdAndIsBlockedTrue(Integer sprintBacklogId);
    List<Task> findBySprintBacklogIdAndActualHoursGreaterThanEstimatedHours(Integer sprintBacklogId);
    List<Task> findByAssignedUserIdAndStatusNot(Long userId, WorkItemStatus status);
    List<Task> findBySprintBacklogIdAndStatusNot(Integer sprintBacklogId, WorkItemStatus status);
    List<Task> findBySprintBacklogIdAndStatusAndCompletedDateAfter(Integer sprintBacklogId, WorkItemStatus status, LocalDate date);
    List<Task> findByAssignedUserIdAndSprintBacklogId(Long userId, Long sprintBacklogId);

}