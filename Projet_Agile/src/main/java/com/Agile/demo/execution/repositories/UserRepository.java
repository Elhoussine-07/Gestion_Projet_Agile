package com.Agile.demo.execution.repositories;

import com.Agile.demo.model.Role;
import com.Agile.demo.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByUsername(String username);


    Optional<User> findByEmail(String email);

    List<User> findByRole(Role role);

    boolean existsByUsername(String username);


    boolean existsByEmail(String email);


    @Query("SELECT DISTINCT u FROM User u JOIN u.projects p WHERE p.id = :projectId")
    List<User> findUsersByProjectId(@Param("projectId") Long projectId);

    @Query("SELECT u FROM User u WHERE u.role = :role AND " +
            "(SELECT COUNT(t) FROM Task t WHERE t.assignedUser.id = u.id AND t.status IN ('TODO', 'IN_PROGRESS')) < :maxTasks")
    List<User> findAvailableUsersByRole(@Param("role") Role role, @Param("maxTasks") long maxTasks);

    @Query("SELECT COUNT(t) FROM Task t WHERE t.assignedUser.id = :userId AND t.status = :status")
    long countTasksByUserAndStatus(@Param("userId") Long userId, @Param("status") String status);


    List<User> findByIsActiveTrue();
    List<User> findByRoleAndIsActiveTrue(Role role);
    List<User> findByUsernameContainingIgnoreCaseOrEmailContainingIgnoreCase(String username, String email);
    int countByRole(Role role);

    @Query("SELECT COUNT(t) FROM Task t WHERE t.assignedUser.id = :userId AND t.completedDate BETWEEN :startDate AND :endDate AND t.status = 'DONE'")
    int countTasksCompletedByUserBetweenDates(@Param("userId") Long userId, @Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);
}