package com.Agile.demo.execution.controllers;

import com.Agile.demo.execution.dto.PasswordUpdateRequest;
import com.Agile.demo.execution.dto.UserCreateRequest;
import com.Agile.demo.execution.dto.UserUpdateRequest;
import com.Agile.demo.execution.services.UserService;
import com.Agile.demo.model.Role;
import com.Agile.demo.model.User;
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
    public ResponseEntity<User> createUser(@RequestBody UserCreateRequest request) {
        User user = userService.createUser(
                request.getUsername(),
                request.getEmail(),
                request.getPassword(),
                request.getRole()
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(user);
    }

    @GetMapping("/{userId}")
    public ResponseEntity<User> getUserById(@PathVariable Long userId) {
        User user = userService.getUserById(userId);
        return ResponseEntity.ok(user);
    }


    @GetMapping("/username/{username}")
    public ResponseEntity<User> getUserByUsername(@PathVariable String username) {
        User user = userService.getUserByUsername(username);
        return ResponseEntity.ok(user);
    }


    @GetMapping("/email/{email}")
    public ResponseEntity<User> getUserByEmail(@PathVariable String email) {
        User user = userService.getUserByEmail(email);
        return ResponseEntity.ok(user);
    }


    @GetMapping
    public ResponseEntity<List<User>> getAllUsers() {
        List<User> users = userService.getAllUsers();
        return ResponseEntity.ok(users);
    }


    @GetMapping("/role/{role}")
    public ResponseEntity<List<User>> getUsersByRole(@PathVariable Role role) {
        List<User> users = userService.getUsersByRole(role);
        return ResponseEntity.ok(users);
    }


    @GetMapping("/project/{projectId}")
    public ResponseEntity<List<User>> getUsersByProject(@PathVariable Long projectId) {
        List<User> users = userService.getUsersByProject(projectId);
        return ResponseEntity.ok(users);
    }


    @PutMapping("/{userId}")
    public ResponseEntity<User> updateUser(@PathVariable Long userId, @RequestBody UserUpdateRequest request) {
        User user = userService.updateUser(userId, request.getEmail(), request.getRole());
        return ResponseEntity.ok(user);
    }


    @PutMapping("/{userId}/password")
    public ResponseEntity<User> updatePassword(@PathVariable Long userId, @RequestBody PasswordUpdateRequest request) {
        User user = userService.updatePassword(userId, request.getCurrentPassword(), request.getNewPassword());
        return ResponseEntity.ok(user);
    }


    @DeleteMapping("/{userId}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long userId) {
        userService.deleteUser(userId);
        return ResponseEntity.noContent().build();
    }


    @GetMapping("/available")
    public ResponseEntity<List<User>> getAvailableUsers(
            @RequestParam Role role,
            @RequestParam(defaultValue = "5") long maxTasks) {

        List<User> users = userService.getAvailableUsers(role, maxTasks);
        return ResponseEntity.ok(users);
    }


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


    @GetMapping("/{userId}/statistics")
    public ResponseEntity<UserService.UserStatistics> getUserStatistics(@PathVariable Long userId) {
        UserService.UserStatistics stats = userService.getUserStatistics(userId);
        return ResponseEntity.ok(stats);
    }


    @PostMapping("/{userId}/activate")
    public ResponseEntity<User> activateUser(@PathVariable Long userId) {
        User user = userService.activateUser(userId);
        return ResponseEntity.ok(user);
    }


    @PostMapping("/{userId}/deactivate")
    public ResponseEntity<User> deactivateUser(@PathVariable Long userId) {
        User user = userService.deactivateUser(userId);
        return ResponseEntity.ok(user);
    }


    @GetMapping("/active")
    public ResponseEntity<List<User>> getActiveUsers() {
        List<User> users = userService.getActiveUsers();
        return ResponseEntity.ok(users);
    }


    @GetMapping("/active/role/{role}")
    public ResponseEntity<List<User>> getActiveUsersByRole(@PathVariable Role role) {
        List<User> users = userService.getActiveUsersByRole(role);
        return ResponseEntity.ok(users);
    }

    @GetMapping("/search")
    public ResponseEntity<List<User>> searchUsers(@RequestParam String searchTerm) {
        List<User> users = userService.searchUsers(searchTerm);
        return ResponseEntity.ok(users);
    }


    @GetMapping("/available-developers")
    public ResponseEntity<List<User>> getAvailableDevelopers(
            @RequestParam(defaultValue = "5") int maxActiveTasks) {

        List<User> developers = userService.getAvailableDevelopers(maxActiveTasks);
        return ResponseEntity.ok(developers);
    }


    @GetMapping("/most-loaded")
    public ResponseEntity<List<UserService.UserWorkload>> getMostLoadedUsers(
            @RequestParam(defaultValue = "10") int limit) {

        List<UserService.UserWorkload> workloads = userService.getMostLoadedUsers(limit);
        return ResponseEntity.ok(workloads);
    }


    @GetMapping("/project/{projectId}/team-workload")
    public ResponseEntity<UserService.TeamWorkload> getTeamWorkload(@PathVariable Long projectId) {
        UserService.TeamWorkload workload = userService.getTeamWorkload(projectId);
        return ResponseEntity.ok(workload);
    }


    @GetMapping("/{userId}/available")
    public ResponseEntity<Boolean> isUserAvailable(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "5") int maxTaskThreshold) {

        boolean available = userService.isUserAvailable(userId, maxTaskThreshold);
        return ResponseEntity.ok(available);
    }


    @GetMapping("/least-loaded/role/{role}")
    public ResponseEntity<User> getLeastLoadedUserByRole(@PathVariable Role role) {
        User user = userService.getLeastLoadedUserByRole(role);
        return ResponseEntity.ok(user);
    }


    @PutMapping("/{userId}/profile")
    public ResponseEntity<User> updateUserProfile(@PathVariable Long userId, @RequestBody Map<String, String> request) {
        User user = userService.updateUserProfile(
                userId,
                request.get("email"),
                request.get("firstName"),
                request.get("lastName"),
                request.get("phoneNumber")
        );
        return ResponseEntity.ok(user);
    }


    @GetMapping("/count-by-role")
    public ResponseEntity<Map<Role, Integer>> getUserCountByRole() {
        Map<Role, Integer> counts = userService.getUserCountByRole();
        return ResponseEntity.ok(counts);
    }


    @PostMapping("/{userId}/reset-password")
    public ResponseEntity<User> resetPassword(@PathVariable Long userId, @RequestBody Map<String, String> request) {
        User user = userService.resetPassword(userId, request.get("newPassword"));
        return ResponseEntity.ok(user);
    }


    @GetMapping("/{userId}/requires-password-reset")
    public ResponseEntity<Boolean> requiresPasswordReset(@PathVariable Long userId) {
        boolean requires = userService.requiresPasswordReset(userId);
        return ResponseEntity.ok(requires);
    }


    @PostMapping("/{userId}/mark-password-changed")
    public ResponseEntity<User> markPasswordChanged(@PathVariable Long userId) {
        User user = userService.markPasswordChanged(userId);
        return ResponseEntity.ok(user);
    }


    @GetMapping("/{userId}/performance")
    public ResponseEntity<UserService.UserPerformance> getUserPerformance(
            @PathVariable Long userId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {

        UserService.UserPerformance performance = userService.getUserPerformance(userId, startDate, endDate);
        return ResponseEntity.ok(performance);
    }


    @GetMapping("/project/{projectId}/without-tasks")
    public ResponseEntity<List<User>> getUsersWithoutTasks(@PathVariable Long projectId) {
        List<User> users = userService.getUsersWithoutTasks(projectId);
        return ResponseEntity.ok(users);
    }

    @PostMapping("/{userId}/send-welcome")
    public ResponseEntity<Void> sendWelcomeNotification(@PathVariable Long userId) {
        userService.sendWelcomeNotification(userId);
        return ResponseEntity.ok().build();
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<String> handleIllegalArgument(IllegalArgumentException ex) {
        return ResponseEntity.badRequest().body(ex.getMessage());
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<String> handleIllegalState(IllegalStateException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(ex.getMessage());
    }
}