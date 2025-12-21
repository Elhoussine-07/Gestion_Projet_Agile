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

    List<Task> findByUserStoryId(Long userStoryId);

    List<Task> findBySprintBacklogId(Integer sprintBacklogId);

    List<Task> findByAssignedUserId(Long userId);

    List<Task> findByAssignedUserIdAndStatus(Long userId, WorkItemStatus status);

    // ✅ AJOUTE CETTE MÉTHODE (utilisée dans le service)
    List<Task> findBySprintBacklogIdAndAssignedUserIsNull(Long sprintBacklogId);

    List<Task> findBySprintBacklogIdAndStatus(Long sprintBacklogId, WorkItemStatus status);

    @Query("SELECT COALESCE(SUM(t.estimatedHours), 0) FROM Task t WHERE t.userStory.id = :userStoryId")
    Integer getTotalEstimatedHoursByUserStory(@Param("userStoryId") Long userStoryId);

    @Query("SELECT COALESCE(SUM(t.actualHours), 0) FROM Task t WHERE t.userStory.id = :userStoryId")
    Integer getTotalActualHoursByUserStory(@Param("userStoryId") Long userStoryId);

    long countByUserStoryIdAndStatus(Long userStoryId, WorkItemStatus status);

    @Query("SELECT t FROM Task t WHERE t.sprintBacklog.id = :sprintBacklogId AND t.actualHours > t.estimatedHours")
    List<Task> findOverEstimatedTasksBySprint(@Param("sprintBacklogId") Long sprintBacklogId);

    List<Task> findBySprintBacklogIdAndIsBlockedTrue(Integer sprintBacklogId);


    List<Task> findByAssignedUserIdAndStatusNot(Long userId, WorkItemStatus status);

    List<Task> findBySprintBacklogIdAndStatusNot(Integer sprintBacklogId, WorkItemStatus status);

    List<Task> findBySprintBacklogIdAndStatusAndCompletedDateAfter(
            Integer sprintBacklogId, WorkItemStatus status, LocalDate date);

    List<Task> findByAssignedUserIdAndSprintBacklogId(Long userId, Long sprintBacklogId);
}