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

    /**
     * Trouve tous les sprints d'un projet
     */
    Optional<SprintBacklog> findBySprintNumber(Integer sprintNumber);

    // language: java
    List<SprintBacklog> findByProjectSprintNumber(Long projectId);
    /**
     * Trouve le sprint actif d'un projet
     */
    List<SprintBacklog> findAllByProjectIdAndSprintStatus(Long projectId, SprintStatus status);

    /**
     * Trouve un sprint par son numéro dans un projet
     *
     */

    List<SprintBacklog> findBySprintStatus(SprintStatus status);

    Optional<SprintBacklog> findByProjectIdAndSprintNumber(Long projectId, Integer sprintNumber);

    /**
     * Trouve tous les sprints dans une plage de dates
     */
    List<SprintBacklog> findByProjectIdAndStartDateBetween(Long projectId, LocalDate start, LocalDate end);

    /**
     * Trouve tous les sprints par statut
     */
    List<SprintBacklog> findByProjectIdAndSprintStatus(Long projectId, SprintStatus status);

    /**
     * Vérifie si un sprint existe avec ce numéro dans le projet
     */
    boolean existsByProjectIdAndSprintNumber(Long projectId, Integer sprintNumber);

    /**
     * Trouve le dernier sprint d'un projet
     */
    @Query("SELECT s FROM SprintBacklog s WHERE s.project.id = :projectId ORDER BY s.sprintNumber DESC")
    Optional<SprintBacklog> findLatestSprint(@Param("projectId") Long projectId);

    /**
     * Compte le nombre de sprints actifs pour un projet
     */
    long countByProjectIdAndSprintStatus(Long projectId, SprintStatus status);
}