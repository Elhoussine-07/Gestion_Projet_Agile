package com.Agile.demo.execution.repositories;

import com.Agile.demo.model.Task;
import com.Agile.demo.model.WorkItemStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface TaskRepository extends JpaRepository<Task, Long> {

    // ==================== BASIC QUERIES ====================

    List<Task> findByUserStoryId(Long userStoryId);

    List<Task> findBySprintBacklogId(Integer sprintBacklogId);

    List<Task> findByAssignedUserId(Long userId);

    List<Task> findByAssignedUserIdAndStatus(Long userId, WorkItemStatus status);

    List<Task> findBySprintBacklogIdAndStatus(Long sprintBacklogId, WorkItemStatus status);

    List<Task> findByAssignedUserIdAndSprintBacklogId(Long userId, Long sprintBacklogId);

    // ==================== NULL/NOT NULL QUERIES ====================

    List<Task> findBySprintBacklogIdAndAssignedUserIsNull(Long sprintBacklogId);

    List<Task> findByAssignedUserIdAndStatusNot(Long userId, WorkItemStatus status);

    List<Task> findBySprintBacklogIdAndStatusNot(Long sprintBacklogId, WorkItemStatus status);

    // ==================== BLOCKED TASKS ====================

    List<Task> findBySprintBacklogIdAndIsBlocked(Integer sprintBacklogId, boolean blocked);

    List<Task> findBySprintBacklogIdAndIsBlockedTrue(Integer sprintBacklogId);

    // ==================== AGGREGATION QUERIES ====================

    @Query("SELECT COALESCE(SUM(t.estimatedHours), 0) FROM Task t WHERE t.userStory.id = :userStoryId")
    Integer getTotalEstimatedHoursByUserStory(@Param("userStoryId") Long userStoryId);

    @Query("SELECT COALESCE(SUM(t.actualHours), 0) FROM Task t WHERE t.userStory.id = :userStoryId")
    Integer getTotalActualHoursByUserStory(@Param("userStoryId") Long userStoryId);

    long countByUserStoryIdAndStatus(Long userStoryId, WorkItemStatus status);

    // ==================== OVER ESTIMATED TASKS ====================

    @Query("SELECT t FROM Task t WHERE t.sprintBacklog.id = :sprintBacklogId AND t.actualHours > t.estimatedHours")
    List<Task> findOverEstimatedTasksBySprint(@Param("sprintBacklogId") Long sprintBacklogId);

    @Query("SELECT t FROM Task t WHERE t.sprintBacklog.id = :sprintBacklogId AND t.actualHours > t.estimatedHours * 1.2")
    List<Task> findTasksExceedingEstimate(@Param("sprintBacklogId") Integer sprintBacklogId);

    // ==================== CRITICAL TASKS ====================

    @Query("SELECT t FROM Task t WHERE t.sprintBacklog.sprintNumber = :sprintNumber " +
            "AND t.status != com.Agile.demo.model.WorkItemStatus.DONE " +
            "AND (t.isBlocked = true OR t.actualHours > t.estimatedHours * 1.5)")
    List<Task> findCriticalTasks(@Param("sprintNumber") Integer sprintNumber);

    // ==================== RECENTLY COMPLETED TASKS ====================

    @Query("SELECT t FROM Task t WHERE t.sprintBacklog.id = :sprintBacklogId " +
            "AND t.status = com.Agile.demo.model.WorkItemStatus.DONE " +
            "AND t.completedDate >= :sinceDate")
    List<Task> findRecentlyCompletedTasks(
            @Param("sprintBacklogId") Integer sprintBacklogId,
            @Param("sinceDate") LocalDate sinceDate
    );

    default List<Task> findRecentlyCompletedTasks(Integer sprintBacklogId, int days) {
        LocalDate sinceDate = LocalDate.now().minusDays(days);
        return findRecentlyCompletedTasks(sprintBacklogId, sinceDate);
    }

    // Alternative avec status et date (pour compatibilité avec l'ancien code)
    List<Task> findBySprintBacklogIdAndStatusAndCompletedDateAfter(
            Integer sprintBacklogId, WorkItemStatus status, LocalDate date);
}