package com.Agile.demo.planning.controller;

import com.Agile.demo.planning.dto.userstory.*;
import com.Agile.demo.planning.service.UserStoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/user-stories")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class UserStoryController {

    private final UserStoryService userStoryService;

    @PostMapping
    public ResponseEntity<UserStoryDTO> createUserStory(@RequestBody CreateUserStoryDTO request) {
        UserStoryDTO dto = userStoryService.createUserStory(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(dto);
    }

    @PostMapping("/with-criteria")
    public ResponseEntity<UserStoryDTO> createUserStoryWithCriteria(
            @RequestBody CreateUserStoryWithCriteriaDTO request) {
        UserStoryDTO dto = userStoryService.createUserStoryWithCriteria(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(dto);
    }

    @GetMapping
    public ResponseEntity<List<UserStoryDTO>> getAllUserStories() {
        List<UserStoryDTO> dtos = userStoryService.getAllUserStories();
        return ResponseEntity.ok(dtos);
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserStoryDTO> getUserStoryById(@PathVariable Long id) {
        UserStoryDTO dto = userStoryService.getUserStoryById(id);
        return ResponseEntity.ok(dto);
    }

    @GetMapping("/backlog/{backlogId}")
    public ResponseEntity<List<UserStoryDTO>> getUserStoriesByBacklog(@PathVariable Long backlogId) {
        List<UserStoryDTO> dtos = userStoryService.getUserStoriesByProductBacklog(backlogId);
        return ResponseEntity.ok(dtos);
    }

    @GetMapping("/epic/{epicId}")
    public ResponseEntity<List<UserStoryDTO>> getUserStoriesByEpic(@PathVariable Long epicId) {
        List<UserStoryDTO> dtos = userStoryService.getUserStoriesByEpic(epicId);
        return ResponseEntity.ok(dtos);
    }

    @GetMapping("/backlog/{backlogId}/unassigned")
    public ResponseEntity<List<UserStoryDTO>> getUnassignedStories(@PathVariable Long backlogId) {
        List<UserStoryDTO> dtos = userStoryService.getUnassignedStories(backlogId);
        return ResponseEntity.ok(dtos);
    }

    @GetMapping("/backlog/{backlogId}/ready")
    public ResponseEntity<List<UserStoryDTO>> getReadyStories(@PathVariable Long backlogId) {
        List<UserStoryDTO> dtos = userStoryService.getReadyStories(backlogId);
        return ResponseEntity.ok(dtos);
    }

    @PutMapping("/{id}")
    public ResponseEntity<UserStoryDTO> updateUserStory(
            @PathVariable Long id,
            @RequestBody UpdateUserStoryDTO request) {
        UserStoryDTO dto = userStoryService.updateUserStory(id, request);
        return ResponseEntity.ok(dto);
    }

    @PatchMapping("/{id}/priority")
    public ResponseEntity<Void> updatePriority(
            @PathVariable Long id,
            @RequestBody UpdatePriorityDTO request) {
        userStoryService.updatePriority(id, request);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/{id}/acceptance-criteria")
    public ResponseEntity<UserStoryDTO> updateAcceptanceCriteria(
            @PathVariable Long id,
            @RequestBody UpdateAcceptanceCriteriaDTO request) {
        UserStoryDTO dto = userStoryService.updateAcceptanceCriteria(id, request);
        return ResponseEntity.ok(dto);
    }

    @PatchMapping("/{id}/metrics")
    public ResponseEntity<UserStoryDTO> updateMetrics(
            @PathVariable Long id,
            @RequestBody UpdateMetricsDTO request) {
        UserStoryDTO dto = userStoryService.updateMetrics(id, request);
        return ResponseEntity.ok(dto);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUserStory(@PathVariable Long id) {
        userStoryService.deleteUserStory(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}/gherkin")
    public ResponseEntity<String> getGherkinFormat(@PathVariable Long id) {
        String gherkin = userStoryService.getGherkinFormat(id);
        return ResponseEntity.ok(gherkin);
    }

    @GetMapping("/{id}/ready-for-sprint")
    public ResponseEntity<Boolean> isReadyForSprint(@PathVariable Long id) {
        boolean ready = userStoryService.isReadyForSprint(id);
        return ResponseEntity.ok(ready);
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