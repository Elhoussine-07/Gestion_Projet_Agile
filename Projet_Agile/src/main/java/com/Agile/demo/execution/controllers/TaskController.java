package com.Agile.demo.execution.controllers;


import com.Agile.demo.execution.dto.task.*;
import com.Agile.demo.execution.services.TaskService;
import com.Agile.demo.model.WorkItemStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/tasks")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class TaskController {

    private final TaskService taskService;

    // ==================== CRÉATION ====================

    @PostMapping
    public ResponseEntity<TaskResponseDTO> createTask(@RequestBody CreateTaskRequest request) {
        TaskResponseDTO task = taskService.createTask(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(task);
    }

    // ==================== LECTURE ====================

    @GetMapping("/{taskId}")
    public ResponseEntity<TaskResponseDTO> getTaskById(@PathVariable Long taskId) {
        return ResponseEntity.ok(taskService.getTaskById(taskId));
    }

    @GetMapping("/user-story/{userStoryId}")
    public ResponseEntity<List<TaskResponseDTO>> getTasksByUserStory(@PathVariable Long userStoryId) {
        return ResponseEntity.ok(taskService.getTasksByUserStory(userStoryId));
    }

    @GetMapping("/sprint/{sprintId}")
    public ResponseEntity<List<TaskResponseDTO>> getTasksBySprint(@PathVariable Integer sprintId) {
        return ResponseEntity.ok(taskService.getTasksBySprint(sprintId));
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<TaskResponseDTO>> getTasksByUser(@PathVariable Long userId) {
        return ResponseEntity.ok(taskService.getTasksByUser(userId));
    }

    @GetMapping("/sprint/{sprintId}/status/{status}")
    public ResponseEntity<List<TaskResponseDTO>> getTasksByStatus(
            @PathVariable Long sprintId,
            @PathVariable WorkItemStatus status) {
        return ResponseEntity.ok(taskService.getTasksByStatus(sprintId, status));
    }

    @GetMapping("/sprint/{sprintId}/unassigned")
    public ResponseEntity<List<TaskResponseDTO>> getUnassignedTasksBySprint(@PathVariable Long sprintId) {
        return ResponseEntity.ok(taskService.getUnassignedTasksBySprint(sprintId));
    }

    // ==================== MISE À JOUR ====================

    @PutMapping("/{taskId}")
    public ResponseEntity<TaskResponseDTO> updateTask(
            @PathVariable Long taskId,
            @RequestBody UpdateTaskRequest request) {
        return ResponseEntity.ok(taskService.updateTask(taskId, request));
    }

    // ==================== GESTION EFFORT & ASSIGNATION ====================

    @PostMapping("/{taskId}/log-hours")
    public ResponseEntity<TaskResponseDTO> logHours(
            @PathVariable Long taskId,
            @RequestBody LogHoursRequest request) {
        return ResponseEntity.ok(taskService.logHours(taskId, request));
    }

    @PostMapping("/{taskId}/assign/{userId}")
    public ResponseEntity<TaskResponseDTO> assignTask(
            @PathVariable Long taskId,
            @PathVariable Long userId) {
        return ResponseEntity.ok(taskService.assignTask(taskId, userId));
    }

    @PostMapping("/{taskId}/unassign")
    public ResponseEntity<TaskResponseDTO> unassignTask(@PathVariable Long taskId) {
        return ResponseEntity.ok(taskService.unassignTask(taskId));
    }

    @PostMapping("/reassign/from/{fromUserId}/to/{toUserId}")
    public ResponseEntity<Void> reassignUserTasks(
            @PathVariable Long fromUserId,
            @PathVariable Long toUserId) {
        taskService.reassignUserTasks(fromUserId, toUserId);
        return ResponseEntity.ok().build();
    }

    // ==================== WORKFLOW ====================

    @PostMapping("/{taskId}/start/{userId}")
    public ResponseEntity<TaskResponseDTO> startTask(
            @PathVariable Long taskId,
            @PathVariable Long userId) {
        return ResponseEntity.ok(taskService.startTask(taskId, userId));
    }

    @PostMapping("/{taskId}/review")
    public ResponseEntity<TaskResponseDTO> moveTaskToReview(@PathVariable Long taskId) {
        return ResponseEntity.ok(taskService.moveTaskToReview(taskId));
    }

    @PostMapping("/{taskId}/testing")
    public ResponseEntity<TaskResponseDTO> moveTaskToTesting(@PathVariable Long taskId) {
        return ResponseEntity.ok(taskService.moveTaskToTesting(taskId));
    }

    @PostMapping("/{taskId}/complete")
    public ResponseEntity<TaskResponseDTO> completeTask(@PathVariable Long taskId) {
        return ResponseEntity.ok(taskService.completeTask(taskId));
    }

    // ==================== BLOCAGE ====================

    @PostMapping("/{taskId}/block")
    public ResponseEntity<TaskResponseDTO> blockTask(
            @PathVariable Long taskId,
            @RequestBody BlockTaskRequest request) {
        return ResponseEntity.ok(taskService.blockTask(taskId, request));
    }

    @PostMapping("/{taskId}/unblock")
    public ResponseEntity<TaskResponseDTO> unblockTask(@PathVariable Long taskId) {
        return ResponseEntity.ok(taskService.unblockTask(taskId));
    }

    // Méthodes utilitaires exposées car publiques dans le service
    @GetMapping("/{taskId}/is-blocked")
    public ResponseEntity<Boolean> isTaskBlocked(@PathVariable Long taskId) {
        return ResponseEntity.ok(taskService.isTaskBlocked(taskId));
    }

    @GetMapping("/{taskId}/block-info")
    public ResponseEntity<TaskService.TaskBlockInfo> getTaskBlockInfo(@PathVariable Long taskId) {
        TaskService.TaskBlockInfo info = taskService.getTaskBlockInfo(taskId);
        if (info == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(info);
    }

    // ==================== ACTIONS SPÉCIALES ====================

    @DeleteMapping("/{taskId}")
    public ResponseEntity<Void> deleteTask(@PathVariable Long taskId) {
        taskService.deleteTask(taskId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{taskId}/duplicate")
    public ResponseEntity<TaskResponseDTO> duplicateTask(@PathVariable Long taskId) {
        return ResponseEntity.status(HttpStatus.CREATED).body(taskService.duplicateTask(taskId));
    }

    // ==================== STATISTIQUES ====================

    @GetMapping("/user-story/{userStoryId}/metrics")
    public ResponseEntity<TaskService.UserStoryTaskMetrics> getUserStoryTaskMetrics(@PathVariable Long userStoryId) {
        return ResponseEntity.ok(taskService.getUserStoryTaskMetrics(userStoryId));
    }

    @GetMapping("/sprint/{sprintId}/statistics")
    public ResponseEntity<TaskService.SprintTaskStatistics> getSprintTaskStatistics(@PathVariable Integer sprintId) {
        return ResponseEntity.ok(taskService.getSprintTaskStatistics(sprintId));
    }

    // ==================== GESTION ERREURS ====================

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<String> handleIllegalArgument(IllegalArgumentException ex) {
        return ResponseEntity.badRequest().body(ex.getMessage());
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<String> handleIllegalState(IllegalStateException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(ex.getMessage());
    }
}