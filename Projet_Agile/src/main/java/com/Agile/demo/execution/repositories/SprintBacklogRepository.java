package com.Agile.demo.execution.repositories;

import com.Agile.demo.model.SprintBacklog;
import com.Agile.demo.model.SprintStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface SprintBacklogRepository extends JpaRepository<SprintBacklog, Long> {


    Optional<SprintBacklog> findBySprintNumber(Integer SprintNumber);

    // language: java
// ✅ CORRECT
    List<SprintBacklog> findByProjectId(Long projectId);

    List<SprintBacklog> findAllByProjectIdAndSprintStatus(Long projectId, SprintStatus status);

    List<SprintBacklog> findBySprintStatus(SprintStatus status);

    Optional<SprintBacklog> findByProjectIdAndSprintNumber(Long projectId, Integer sprintNumber);

    List<SprintBacklog> findByProjectIdAndStartDateBetween(Long projectId, LocalDate start, LocalDate end);

    List<SprintBacklog> findByProjectIdAndSprintStatus(Long projectId, SprintStatus status);


    boolean existsByProjectIdAndSprintNumber(Long projectId, Integer sprintNumber);


    @Query("SELECT s FROM SprintBacklog s WHERE s.project.id = :projectId ORDER BY s.sprintNumber DESC")
    Optional<SprintBacklog> findLatestSprint(@Param("projectId") Long projectId);

    long countByProjectIdAndSprintStatus(Long projectId, SprintStatus status);


    Optional<SprintBacklog> findTopByProjectIdOrderBySprintNumberDesc(Long projectId);
    @Query("SELECT s FROM SprintBacklog s WHERE s.project.id = :projectId AND EXISTS (SELECT us FROM s.userStories us WHERE us.status != 'DONE')")
    List<SprintBacklog> findSprintsWithIncompleteStories(@Param("projectId") Long projectId);
}