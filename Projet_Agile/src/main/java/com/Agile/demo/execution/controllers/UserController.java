package com.Agile.demo.execution.controllers;

import com.Agile.demo.execution.dto.user.*;
import com.Agile.demo.execution.services.UserService;
import com.Agile.demo.execution.services.UserService.*;
import com.Agile.demo.model.Role;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class UserController {

    private final UserService userService;

    @PostMapping
    public ResponseEntity<UserResponseDTO> createUser(@Valid @RequestBody CreateUserRequest request) {
        UserResponseDTO user = userService.createUser(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(user);
    }

    // ==================== LECTURE ====================

    @GetMapping("/{userId}")
    public ResponseEntity<UserResponseDTO> getUserById(@PathVariable Long userId) {
        UserResponseDTO user = userService.getUserById(userId);
        return ResponseEntity.ok(user);
    }

    @GetMapping("/username/{username}")
    public ResponseEntity<UserResponseDTO> getUserByUsername(@PathVariable String username) {
        UserResponseDTO user = userService.getUserByUsername(username);
        return ResponseEntity.ok(user);
    }

    @GetMapping("/email/{email}")
    public ResponseEntity<UserResponseDTO> getUserByEmail(@PathVariable String email) {
        UserResponseDTO user = userService.getUserByEmail(email);
        return ResponseEntity.ok(user);
    }

    @GetMapping
    public ResponseEntity<List<UserResponseDTO>> getAllUsers() {
        List<UserResponseDTO> users = userService.getAllUsers();
        return ResponseEntity.ok(users);
    }

    @GetMapping("/role/{role}")
    public ResponseEntity<List<UserResponseDTO>> getUsersByRole(@PathVariable Role role) {
        List<UserResponseDTO> users = userService.getUsersByRole(role);
        return ResponseEntity.ok(users);
    }

    @GetMapping("/project/{projectId}")
    public ResponseEntity<List<UserResponseDTO>> getUsersByProject(@PathVariable Long projectId) {
        List<UserResponseDTO> users = userService.getUsersByProject(projectId);
        return ResponseEntity.ok(users);
    }

    @GetMapping("/active")
    public ResponseEntity<List<UserResponseDTO>> getActiveUsers() {
        List<UserResponseDTO> users = userService.getActiveUsers();
        return ResponseEntity.ok(users);
    }

    @GetMapping("/active/role/{role}")
    public ResponseEntity<List<UserResponseDTO>> getActiveUsersByRole(@PathVariable Role role) {
        List<UserResponseDTO> users = userService.getActiveUsersByRole(role);
        return ResponseEntity.ok(users);
    }

    @GetMapping("/search")
    public ResponseEntity<List<UserResponseDTO>> searchUsers(@RequestParam String searchTerm) {
        List<UserResponseDTO> users = userService.searchUsers(searchTerm);
        return ResponseEntity.ok(users);
    }

    // ==================== MISE À JOUR ====================

    @PutMapping("/{userId}")
    public ResponseEntity<UserResponseDTO> updateUser(
            @PathVariable Long userId,
            @Valid @RequestBody UserUpdateRequest request) {
        UserResponseDTO user = userService.updateUser(userId, request);
        return ResponseEntity.ok(user);
    }

    @PutMapping("/{userId}/profile")
    public ResponseEntity<UserResponseDTO> updateUserProfile(
            @PathVariable Long userId,
            @Valid @RequestBody UserProfileUpdateRequest request) {
        UserResponseDTO user = userService.updateUserProfile(userId, request);
        return ResponseEntity.ok(user);
    }

    @PutMapping("/{userId}/password")
    public ResponseEntity<UserResponseDTO> updatePassword(
            @PathVariable Long userId,
            @Valid @RequestBody PasswordUpdateRequest request) {
        UserResponseDTO user = userService.updatePassword(userId, request);
        return ResponseEntity.ok(user);
    }

    @PostMapping("/{userId}/reset-password")
    public ResponseEntity<UserResponseDTO> resetPassword(
            @PathVariable Long userId,
            @Valid @RequestBody PasswordResetRequest request) {
        UserResponseDTO user = userService.resetPassword(userId, request);
        return ResponseEntity.ok(user);
    }

    // ==================== GESTION DES RÔLES ====================

    @PostMapping("/{userId}/roles/add")
    public ResponseEntity<UserResponseDTO> addRoleToUser(
            @PathVariable Long userId,
            @Valid @RequestBody RoleManagementRequest request) {
        UserResponseDTO user = userService.addRoleToUser(userId, request);
        return ResponseEntity.ok(user);
    }

    @PostMapping("/{userId}/roles/remove")
    public ResponseEntity<UserResponseDTO> removeRoleFromUser(
            @PathVariable Long userId,
            @Valid @RequestBody RoleManagementRequest request) {
        UserResponseDTO user = userService.removeRoleFromUser(userId, request);
        return ResponseEntity.ok(user);
    }

    @GetMapping("/{userId}/roles/has/{role}")
    public ResponseEntity<Boolean> userHasRole(
            @PathVariable Long userId,
            @PathVariable Role role) {
        boolean hasRole = userService.userHasRole(userId, role);
        return ResponseEntity.ok(hasRole);
    }

    // ==================== ACTIVATION / DÉSACTIVATION ====================

    @PostMapping("/{userId}/activate")
    public ResponseEntity<UserResponseDTO> activateUser(@PathVariable Long userId) {
        UserResponseDTO user = userService.activateUser(userId);
        return ResponseEntity.ok(user);
    }

    @PostMapping("/{userId}/deactivate")
    public ResponseEntity<UserResponseDTO> deactivateUser(@PathVariable Long userId) {
        UserResponseDTO user = userService.deactivateUser(userId);
        return ResponseEntity.ok(user);
    }

    // ==================== SUPPRESSION ====================

    @DeleteMapping("/{userId}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long userId) {
        userService.deleteUser(userId);
        return ResponseEntity.noContent().build();
    }

    // ==================== STATISTIQUES (Records directement) ====================

    @GetMapping("/{userId}/statistics")
    public ResponseEntity<UserStatistics> getUserStatistics(@PathVariable Long userId) {
        UserStatistics stats = userService.getUserStatistics(userId);
        return ResponseEntity.ok(stats);
    }

    @GetMapping("/most-loaded")
    public ResponseEntity<List<UserWorkload>> getMostLoadedUsers(
            @RequestParam(defaultValue = "10") int limit) {
        List<UserWorkload> workloads = userService.getMostLoadedUsers(limit);
        return ResponseEntity.ok(workloads);
    }

    @GetMapping("/project/{projectId}/team-workload")
    public ResponseEntity<TeamWorkload> getTeamWorkload(@PathVariable Long projectId) {
        TeamWorkload workload = userService.getTeamWorkload(projectId);
        return ResponseEntity.ok(workload);
    }

    @GetMapping("/{userId}/performance")
    public ResponseEntity<UserPerformance> getUserPerformance(
            @PathVariable Long userId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        UserPerformance performance = userService.getUserPerformance(userId, startDate, endDate);
        return ResponseEntity.ok(performance);
    }

    @GetMapping("/count-by-role")
    public ResponseEntity<Map<Role, Integer>> getUserCountByRole() {
        Map<Role, Integer> counts = userService.getUserCountByRole();
        return ResponseEntity.ok(counts);
    }

    @GetMapping("/role-statistics")
    public ResponseEntity<Map<String, Object>> getUserRoleStatistics() {
        Map<String, Object> stats = userService.getUserRoleStatistics();
        return ResponseEntity.ok(stats);
    }

    // ==================== DISPONIBILITÉ ====================

    @GetMapping("/available")
    public ResponseEntity<List<UserResponseDTO>> getAvailableUsers(
            @RequestParam Role role,
            @RequestParam(defaultValue = "5") long maxTasks) {
        List<UserResponseDTO> users = userService.getAvailableUsers(role, maxTasks);
        return ResponseEntity.ok(users);
    }

    @GetMapping("/available-developers")
    public ResponseEntity<List<UserResponseDTO>> getAvailableDevelopers(
            @RequestParam(defaultValue = "5") int maxActiveTasks) {
        List<UserResponseDTO> developers = userService.getAvailableDevelopers(maxActiveTasks);
        return ResponseEntity.ok(developers);
    }

    @GetMapping("/{userId}/available")
    public ResponseEntity<Boolean> isUserAvailable(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "5") int maxTaskThreshold) {
        boolean available = userService.isUserAvailable(userId, maxTaskThreshold);
        return ResponseEntity.ok(available);
    }

    @GetMapping("/least-loaded/role/{role}")
    public ResponseEntity<UserResponseDTO> getLeastLoadedUserByRole(@PathVariable Role role) {
        UserResponseDTO user = userService.getLeastLoadedUserByRole(role);
        return ResponseEntity.ok(user);
    }

    @GetMapping("/project/{projectId}/without-tasks")
    public ResponseEntity<List<UserResponseDTO>> getUsersWithoutTasks(@PathVariable Long projectId) {
        List<UserResponseDTO> users = userService.getUsersWithoutTasks(projectId);
        return ResponseEntity.ok(users);
    }

    // ==================== UTILITAIRES ====================

    @GetMapping("/{userId}/active-tasks-count")
    public ResponseEntity<Long> countUserActiveTasks(@PathVariable Long userId) {
        long count = userService.countUserActiveTasks(userId);
        return ResponseEntity.ok(count);
    }

    @GetMapping("/{userId}/exists")
    public ResponseEntity<Boolean> userExists(@PathVariable Long userId) {
        boolean exists = userService.userExists(userId);
        return ResponseEntity.ok(exists);
    }

    @GetMapping("/username/{username}/exists")
    public ResponseEntity<Boolean> usernameExists(@PathVariable String username) {
        boolean exists = userService.usernameExists(username);
        return ResponseEntity.ok(exists);
    }

    @GetMapping("/email/{email}/exists")
    public ResponseEntity<Boolean> emailExists(@PathVariable String email) {
        boolean exists = userService.emailExists(email);
        return ResponseEntity.ok(exists);
    }

    @GetMapping("/{userId}/requires-password-reset")
    public ResponseEntity<Boolean> requiresPasswordReset(@PathVariable Long userId) {
        boolean requires = userService.requiresPasswordReset(userId);
        return ResponseEntity.ok(requires);
    }

    @PostMapping("/{userId}/mark-password-changed")
    public ResponseEntity<UserResponseDTO> markPasswordChanged(@PathVariable Long userId) {
        UserResponseDTO user = userService.markPasswordChanged(userId);
        return ResponseEntity.ok(user);
    }

    @PostMapping("/{userId}/send-welcome")
    public ResponseEntity<Void> sendWelcomeNotification(@PathVariable Long userId) {
        userService.sendWelcomeNotification(userId);
        return ResponseEntity.ok().build();
    }

    // ==================== GESTION DES ERREURS ====================

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgument(IllegalArgumentException ex) {
        ErrorResponse error = new ErrorResponse(
                HttpStatus.BAD_REQUEST.value(),
                ex.getMessage(),
                System.currentTimeMillis()
        );
        return ResponseEntity.badRequest().body(error);
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<ErrorResponse> handleIllegalState(IllegalStateException ex) {
        ErrorResponse error = new ErrorResponse(
                HttpStatus.CONFLICT.value(),
                ex.getMessage(),
                System.currentTimeMillis()
        );
        return ResponseEntity.status(HttpStatus.CONFLICT).body(error);
    }

    // ==================== CLASSE INTERNE POUR LES ERREURS ====================

    private record ErrorResponse(
            int status,
            String message,
            long timestamp
    ) {}
}