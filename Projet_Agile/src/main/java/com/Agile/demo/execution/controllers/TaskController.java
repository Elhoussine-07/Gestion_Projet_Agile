package com.Agile.demo.execution.controllers;

import com.Agile.demo.execution.dto.TaskCreateRequest;
import com.Agile.demo.execution.services.TaskService;
import com.Agile.demo.model.Task;
import com.Agile.demo.model.WorkItemStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/tasks")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class TaskController {

    private final TaskService taskService;

    @PostMapping
    public ResponseEntity<Task> createTask(@RequestBody TaskCreateRequest request) {
        Task task;
        if (request.getDescription() != null && !request.getDescription().isEmpty()) {
            task = taskService.createTask(
                    request.getUserStoryId(),
                    request.getTitle(),
                    request.getDescription(),
                    request.getEstimatedHours()
            );
        } else {
            task = taskService.createTask(
                    request.getUserStoryId(),
                    request.getTitle(),
                    request.getEstimatedHours()
            );
        }
        return ResponseEntity.status(HttpStatus.CREATED).body(task);
    }


    @GetMapping("/{taskId}")
    public ResponseEntity<Task> getTaskById(@PathVariable Long taskId) {
        Task task = taskService.getTaskById(taskId);
        return ResponseEntity.ok(task);
    }


    @GetMapping("/user-story/{userStoryId}")
    public ResponseEntity<List<Task>> getTasksByUserStory(@PathVariable Long userStoryId) {
        List<Task> tasks = taskService.getTasksByUserStory(userStoryId);
        return ResponseEntity.ok(tasks);
    }

    @GetMapping("/sprint/{sprintId}")
    public ResponseEntity<List<Task>> getTasksBySprint(@PathVariable Long sprintId) {
        List<Task> tasks = taskService.getTasksBySprint(sprintId.intValue());
        return ResponseEntity.ok(tasks);
    }


    @GetMapping("/user/{userId}")
    public ResponseEntity<List<Task>> getTasksByUser(@PathVariable Long userId) {
        List<Task> tasks = taskService.getTasksByUser(userId);
        return ResponseEntity.ok(tasks);
    }


    @PostMapping("/{taskId}/assign/{userId}")
    public ResponseEntity<Task> assignTask(@PathVariable Long taskId, @PathVariable Long userId) {
        Task task = taskService.assignTask(taskId, userId);
        return ResponseEntity.ok(task);
    }


    @PostMapping("/{taskId}/unassign")
    public ResponseEntity<Task> unassignTask(@PathVariable Long taskId) {
        Task task = taskService.unassignTask(taskId);
        return ResponseEntity.ok(task);
    }


    @PostMapping("/{taskId}/log-hours")
    public ResponseEntity<Task> logHours(@PathVariable Long taskId, @RequestBody Map<String, Integer> request) {
        Task task = taskService.logHours(taskId, request.get("hours"));
        return ResponseEntity.ok(task);
    }


    @PutMapping("/{taskId}/estimated-hours")
    public ResponseEntity<Task> updateEstimatedHours(@PathVariable Long taskId, @RequestBody Map<String, Integer> request) {
        Task task = taskService.updateEstimatedHours(taskId, request.get("estimatedHours"));
        return ResponseEntity.ok(task);
    }


    @PostMapping("/{taskId}/start")
    public ResponseEntity<Task> startTask(@PathVariable Long taskId) {
        Task task = taskService.startTask(taskId);
        return ResponseEntity.ok(task);
    }


    @PostMapping("/{taskId}/review")
    public ResponseEntity<Task> moveTaskToReview(@PathVariable Long taskId) {
        Task task = taskService.moveTaskToReview(taskId);
        return ResponseEntity.ok(task);
    }


    @PostMapping("/{taskId}/testing")
    public ResponseEntity<Task> moveTaskToTesting(@PathVariable Long taskId) {
        Task task = taskService.moveTaskToTesting(taskId);
        return ResponseEntity.ok(task);
    }


    @PostMapping("/{taskId}/complete")
    public ResponseEntity<Task> completeTask(@PathVariable Long taskId) {
        Task task = taskService.completeTask(taskId);
        return ResponseEntity.ok(task);
    }


    @PutMapping("/{taskId}/status")
    public ResponseEntity<Task> updateTaskStatus(@PathVariable Long taskId, @RequestBody Map<String, WorkItemStatus> request) {
        Task task = taskService.updateTaskStatus(taskId, request.get("status"));
        return ResponseEntity.ok(task);
    }

    @PutMapping("/{taskId}/description")
    public ResponseEntity<Task> updateTaskDescription(@PathVariable Long taskId, @RequestBody Map<String, String> request) {
        Task task = taskService.updateTaskDescription(taskId, request.get("description"));
        return ResponseEntity.ok(task);
    }


    @PutMapping("/{taskId}/title")
    public ResponseEntity<Task> updateTaskTitle(@PathVariable Long taskId, @RequestBody Map<String, String> request) {
        Task task = taskService.updateTaskTitle(taskId, request.get("title"));
        return ResponseEntity.ok(task);
    }


    @DeleteMapping("/{taskId}")
    public ResponseEntity<Void> deleteTask(@PathVariable Long taskId) {
        taskService.deleteTask(taskId);
        return ResponseEntity.noContent().build();
    }


    @GetMapping("/sprint/{sprintId}/unassigned")
    public ResponseEntity<List<Task>> getUnassignedTasksBySprint(@PathVariable Long sprintId) {
        List<Task> tasks = taskService.findOverEstimatedTasksBySprint(sprintId);
        return ResponseEntity.ok(tasks);
    }


    @GetMapping("/sprint/{sprintId}/over-estimated")
    public ResponseEntity<List<Task>> getOverEstimatedTasksBySprint(@PathVariable Long sprintId) {
        List<Task> tasks = taskService.findOverEstimatedTasksBySprint(sprintId);
        return ResponseEntity.ok(tasks);
    }


    @GetMapping("/user-story/{userStoryId}/metrics")
    public ResponseEntity<TaskService.UserStoryTaskMetrics> getUserStoryTaskMetrics(@PathVariable Long userStoryId) {
        TaskService.UserStoryTaskMetrics metrics = taskService.getUserStoryTaskMetrics(userStoryId);
        return ResponseEntity.ok(metrics);
    }


    @GetMapping("/sprint/{sprintId}/status/{status}")
    public ResponseEntity<List<Task>> getTasksBySprintAndStatus(
            @PathVariable Long sprintId,
            @PathVariable WorkItemStatus status) {

        List<Task> tasks = taskService.getTasksBySprintAndStatus(sprintId, status);
        return ResponseEntity.ok(tasks);
    }


    @GetMapping("/sprint/{sprintId}/blocked")
    public ResponseEntity<List<Task>> getBlockedTasks(@PathVariable Long sprintId) {
        List<Task> tasks = taskService.getBlockedTasks(sprintId.intValue());
        return ResponseEntity.ok(tasks);
    }


    @PostMapping("/{taskId}/block")
    public ResponseEntity<Task> blockTask(@PathVariable Long taskId, @RequestBody Map<String, String> request) {
        Task task = taskService.blockTask(taskId, request.get("blockReason"));
        return ResponseEntity.ok(task);
    }


    @PostMapping("/{taskId}/unblock")
    public ResponseEntity<Task> unblockTask(@PathVariable Long taskId) {
        Task task = taskService.unblockTask(taskId);
        return ResponseEntity.ok(task);
    }


    @GetMapping("/sprint/{sprintId}/exceeding-estimate")
    public ResponseEntity<List<Task>> getTasksExceedingEstimate(@PathVariable Long sprintId) {
        List<Task> tasks = taskService.getTasksExceedingEstimate(sprintId.intValue());
        return ResponseEntity.ok(tasks);
    }


    @PostMapping("/reassign/from/{fromUserId}/to/{toUserId}")
    public ResponseEntity<Void> reassignUserTasks(@PathVariable Long fromUserId, @PathVariable Long toUserId) {
        taskService.reassignUserTasks(fromUserId, toUserId);
        return ResponseEntity.ok().build();
    }


    @GetMapping("/sprint/{sprintNumber}/critical")
    public ResponseEntity<List<Task>> getCriticalTasks(@PathVariable Integer sprintNumber) {
        List<Task> tasks = taskService.getCriticalTasks(sprintNumber);
        return ResponseEntity.ok(tasks);
    }


    @PostMapping("/{taskId}/duplicate")
    public ResponseEntity<Task> duplicateTask(@PathVariable Long taskId) {
        Task task = taskService.duplicateTask(taskId);
        return ResponseEntity.status(HttpStatus.CREATED).body(task);
    }


    @GetMapping("/sprint/{sprintId}/recently-completed")
    public ResponseEntity<List<Task>> getRecentlyCompletedTasks(
            @PathVariable Long sprintId,
            @RequestParam(defaultValue = "7") int days) {

        List<Task> tasks = taskService.getRecentlyCompletedTasks(sprintId.intValue(), days);
        return ResponseEntity.ok(tasks);
    }


    @GetMapping("/sprint/{sprintId}/remaining-hours")
    public ResponseEntity<Integer> calculateRemainingHours(@PathVariable Long sprintId) {
        int remainingHours = taskService.calculateRemainingHours(sprintId.intValue());
        return ResponseEntity.ok(remainingHours);
    }


    @GetMapping("/sprint/{sprintId}/statistics")
    public ResponseEntity<TaskService.SprintTaskStatistics> getSprintTaskStatistics(@PathVariable Long sprintId) {
        TaskService.SprintTaskStatistics stats = taskService.getSprintTaskStatistics(sprintId.intValue());
        return ResponseEntity.ok(stats);
    }


    @GetMapping("/{taskId}/can-delete")
    public ResponseEntity<Boolean> canDeleteTask(@PathVariable Long taskId) {
        boolean canDelete = taskService.canDeleteTask(taskId);
        return ResponseEntity.ok(canDelete);
    }


    @GetMapping("/user/{userId}/sprint/{sprintId}")
    public ResponseEntity<List<Task>> getUserTasksForSprint(@PathVariable Long userId, @PathVariable Long sprintId) {
        List<Task> tasks = taskService.getUserTasksForSprint(userId, sprintId);
        return ResponseEntity.ok(tasks);
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