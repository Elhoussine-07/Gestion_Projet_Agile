package com.Agile.demo.execution.repositories;

import com.Agile.demo.model.Role;
import com.Agile.demo.model.User;
import com.Agile.demo.model.WorkItemStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    // ✅ AJOUT CRITIQUE : Méthode avec JOIN FETCH pour charger les rôles
    @Query("SELECT u FROM User u LEFT JOIN FETCH u.roles WHERE u.username = :username")
    Optional<User> findByUsernameWithRoles(@Param("username") String username);

    // ✅ AJOUT : Méthode avec JOIN FETCH pour charger par email
    @Query("SELECT u FROM User u LEFT JOIN FETCH u.roles WHERE u.email = :email")
    Optional<User> findByEmailWithRoles(@Param("email") String email);

    // ✅ AJOUT : Méthode avec JOIN FETCH pour charger par ID
    @Query("SELECT u FROM User u LEFT JOIN FETCH u.roles WHERE u.id = :id")
    Optional<User> findByIdWithRoles(@Param("id") Long id);

    Optional<User> findByUsername(String username);

    Optional<User> findByEmail(String email);

    @Query("SELECT DISTINCT u FROM User u JOIN u.roles r WHERE r = :role")
    List<User> findByRole(@Param("role") Role role);

    boolean existsByUsername(String username);

    boolean existsByEmail(String email);

    @Query("SELECT DISTINCT u FROM User u JOIN u.projects p WHERE p.id = :projectId")
    List<User> findUsersByProjectId(@Param("projectId") Long projectId);

    @Query("SELECT DISTINCT u FROM User u JOIN u.roles r WHERE r = :role AND " +
            "(SELECT COUNT(t) FROM Task t WHERE t.assignedUser.id = u.id AND t.status IN ('TODO', 'IN_PROGRESS')) < :maxTasks")
    List<User> findAvailableUsersByRole(@Param("role") Role role, @Param("maxTasks") long maxTasks);

    @Query("SELECT COUNT(t) FROM Task t WHERE t.assignedUser.id = :userId AND t.status = :status")
    long countTasksByUserAndStatus(@Param("userId") Long userId, @Param("status") WorkItemStatus status);

    List<User> findByIsActiveTrue();

    @Query("SELECT DISTINCT u FROM User u JOIN u.roles r WHERE r = :role AND u.isActive = true")
    List<User> findByRoleAndIsActiveTrue(@Param("role") Role role);

    List<User> findByUsernameContainingIgnoreCaseOrEmailContainingIgnoreCase(String username, String email);

    @Query("SELECT COUNT(DISTINCT u) FROM User u JOIN u.roles r WHERE r = :role")
    int countByRole(@Param("role") Role role);

    @Query("SELECT COUNT(t) FROM Task t WHERE t.assignedUser.id = :userId AND t.completedDate BETWEEN :startDate AND :endDate AND t.status = 'DONE'")
    int countTasksCompletedByUserBetweenDates(@Param("userId") Long userId, @Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    @Query("SELECT DISTINCT u FROM User u JOIN u.roles r WHERE r IN :roles")
    List<User> findByRolesIn(@Param("roles") List<Role> roles);

    @Query("SELECT COUNT(u.roles) FROM User u WHERE u.id = :userId")
    int countRolesByUserId(@Param("userId") Long userId);
}