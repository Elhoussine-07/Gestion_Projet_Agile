package com.agile.demo.planning.repository;

import com.agile.demo.model.Project;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface ProjectRepository extends JpaRepository<Project, Long> {

    // Requêtes dérivées (Spring Data génère automatiquement)
    Optional<Project> findByName(String name);

    List<Project> findByStartDateBetween(LocalDate start, LocalDate end);

    boolean existsByName(String name);

    // Requêtes personnalisées JPQL
    @Query("SELECT p FROM Project p WHERE p.endDate < :date")
    List<Project> findCompletedProjects(LocalDate date);

    @Query("SELECT p FROM Project p JOIN p.members m WHERE m.id = :userId")
    List<Project> findProjectsByMemberId(Long userId);
}