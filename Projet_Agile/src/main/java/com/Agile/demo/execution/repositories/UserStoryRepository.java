package com.Agile.demo.execution.repositories;

import com.Agile.demo.model.UserStory;
import com.Agile.demo.model.WorkItemStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserStoryRepository extends JpaRepository<UserStory, Long> {

    List<UserStory> findByProductBacklogId(Long productBacklogId);

    List<UserStory> findBySprintBacklogId(Long sprintBacklogId);

    List<UserStory> findByEpicId(Long epicId);

    List<UserStory> findByStatus(WorkItemStatus status);

    List<UserStory> findByProductBacklogIdAndStatus(Long productBacklogId, WorkItemStatus status);

    @Query("SELECT us FROM UserStory us WHERE us.productBacklog.id = :productBacklogId ORDER BY us.priority ASC")
    List<UserStory> findByProductBacklogIdOrderByPriority(@Param("productBacklogId") Long productBacklogId);

    @Query("SELECT COALESCE(SUM(us.storyPoints), 0) FROM UserStory us WHERE us.sprintBacklog.id = :sprintBacklogId")
    Integer getTotalStoryPointsBySprint(@Param("sprintBacklogId") Long sprintBacklogId);

    long countByProductBacklogIdAndStatus(Long productBacklogId, WorkItemStatus status);
}