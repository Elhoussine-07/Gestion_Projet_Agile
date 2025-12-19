package com.Agile.demo.planning.repository;

import com.Agile.demo.model.UserStory;
import com.Agile.demo.model.WorkItemStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface UserStoryRepository extends JpaRepository<UserStory, Long> {

    List<UserStory> findByProductBacklogId(Long productBacklogId);

    List<UserStory> findByProductBacklogIdAndStatus(Long productBacklogId, WorkItemStatus status);

    List<UserStory> findByEpicId(Long epicId);

    List<UserStory> findByProductBacklogIdAndEpicIsNull(Long productBacklogId);

    @Query("SELECT us FROM UserStory us WHERE us.productBacklog.id = :backlogId ORDER BY us.priority ASC")
    List<UserStory> findByProductBacklogIdOrderedByPriority(Long backlogId);

    @Query("SELECT us FROM UserStory us WHERE us.productBacklog.id = :backlogId AND us.sprintBacklog IS NULL ORDER BY us.priority ASC")
    List<UserStory> findUnassignedStoriesByBacklogId(Long backlogId);
}