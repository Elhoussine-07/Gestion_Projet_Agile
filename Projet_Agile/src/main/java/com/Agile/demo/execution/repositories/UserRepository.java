package com.Agile.demo.execution.repositories;

import com.Agile.demo.model.Role;
import com.Agile.demo.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    /**
     * Trouve un utilisateur par son nom d'utilisateur
     */
    Optional<User> findByUsername(String username);

    /**
     * Trouve un utilisateur par son email
     */
    Optional<User> findByEmail(String email);

    /**
     * Trouve tous les utilisateurs par rôle
     */
    List<User> findByRole(Role role);

    /**
     * Vérifie si un nom d'utilisateur existe
     */
    boolean existsByUsername(String username);

    /**
     * Vérifie si un email existe
     */
    boolean existsByEmail(String email);

    /**
     * Trouve tous les utilisateurs d'un projet
     */
    @Query("SELECT DISTINCT u FROM User u JOIN u.projects p WHERE p.id = :projectId")
    List<User> findUsersByProjectId(@Param("projectId") Long projectId);

    /**
     * Trouve les utilisateurs disponibles (avec peu de tâches en cours)
     */
    @Query("SELECT u FROM User u WHERE u.role = :role AND " +
            "(SELECT COUNT(t) FROM Task t WHERE t.assignedUser.id = u.id AND t.status IN ('TODO', 'IN_PROGRESS')) < :maxTasks")
    List<User> findAvailableUsersByRole(@Param("role") Role role, @Param("maxTasks") long maxTasks);

    /**
     * Compte le nombre de tâches assignées à un utilisateur
     */
    @Query("SELECT COUNT(t) FROM Task t WHERE t.assignedUser.id = :userId AND t.status = :status")
    long countTasksByUserAndStatus(@Param("userId") Long userId, @Param("status") String status);
}