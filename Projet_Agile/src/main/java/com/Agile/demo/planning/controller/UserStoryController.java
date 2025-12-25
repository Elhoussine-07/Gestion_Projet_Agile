package com.Agile.demo.planning.controller;

import com.Agile.demo.planning.dto.userstory.*;
import com.Agile.demo.planning.service.UserStoryService;
import com.Agile.demo.model.UserStory;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/user-stories")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class UserStoryController {

    private final UserStoryService userStoryService;

    @PostMapping
    public ResponseEntity<UserStoryDTO> createUserStory(@RequestBody CreateUserStoryDTO request) {
        UserStory story = userStoryService.createUserStory(
                request.getProductBacklogId(),
                request.getTitle(),
                request.getRole(),
                request.getAction(),
                request.getPurpose(),
                request.getStoryPoints()
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(UserStoryDTO.fromEntity(story));
    }

    @PostMapping("/with-criteria")
    public ResponseEntity<UserStoryDTO> createUserStoryWithCriteria(
            @RequestBody CreateUserStoryWithCriteriaDTO request) {

        UserStory story = userStoryService.createUserStoryWithCriteria(
                request.getProductBacklogId(),
                request.getTitle(),
                request.getRole(),
                request.getAction(),
                request.getPurpose(),
                request.getGivenClauses(),
                request.getWhenClauses(),
                request.getThenClauses(),
                request.getStoryPoints()
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(UserStoryDTO.fromEntity(story));
    }

    @GetMapping
    public ResponseEntity<List<UserStoryDTO>> getAllUserStories() {
        List<UserStory> stories = userStoryService.getAllUserStories();
        List<UserStoryDTO> dtos = stories.stream()
                .map(UserStoryDTO::fromEntity)
                .collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserStoryDTO> getUserStoryById(@PathVariable Long id) {
        UserStory story = userStoryService.getUserStoryById(id);
        return ResponseEntity.ok(UserStoryDTO.fromEntity(story));
    }

    @GetMapping("/backlog/{backlogId}")
    public ResponseEntity<List<UserStoryDTO>> getUserStoriesByBacklog(@PathVariable Long backlogId) {
        List<UserStory> stories = userStoryService.getUserStoriesByProductBacklog(backlogId);
        List<UserStoryDTO> dtos = stories.stream()
                .map(UserStoryDTO::fromEntity)
                .collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }

    @GetMapping("/epic/{epicId}")
    public ResponseEntity<List<UserStoryDTO>> getUserStoriesByEpic(@PathVariable Long epicId) {
        List<UserStory> stories = userStoryService.getUserStoriesByEpic(epicId);
        List<UserStoryDTO> dtos = stories.stream()
                .map(UserStoryDTO::fromEntity)
                .collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }

    @GetMapping("/backlog/{backlogId}/unassigned")
    public ResponseEntity<List<UserStoryDTO>> getUnassignedStories(@PathVariable Long backlogId) {
        List<UserStory> stories = userStoryService.getUnassignedStories(backlogId);
        List<UserStoryDTO> dtos = stories.stream()
                .map(UserStoryDTO::fromEntity)
                .collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }

    @GetMapping("/backlog/{backlogId}/ready")
    public ResponseEntity<List<UserStoryDTO>> getReadyStories(@PathVariable Long backlogId) {
        List<UserStory> stories = userStoryService.getReadyStories(backlogId);
        List<UserStoryDTO> dtos = stories.stream()
                .map(UserStoryDTO::fromEntity)
                .collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }

    @PutMapping("/{id}")
    public ResponseEntity<UserStoryDTO> updateUserStory(
            @PathVariable Long id,
            @RequestBody UpdateUserStoryDTO request) {

        UserStory story = userStoryService.updateUserStory(
                id,
                request.getTitle(),
                request.getRole(),
                request.getAction(),
                request.getPurpose(),
                request.getStoryPoints()
        );
        return ResponseEntity.ok(UserStoryDTO.fromEntity(story));
    }

    @PatchMapping("/{id}/priority")
    public ResponseEntity<Void> updatePriority(
            @PathVariable Long id,
            @RequestBody UpdatePriorityDTO request) {

        userStoryService.updatePriority(id, request.getPriority());
        return ResponseEntity.ok().build();
    }

    @PutMapping("/{id}/acceptance-criteria")
    public ResponseEntity<UserStoryDTO> updateAcceptanceCriteria(
            @PathVariable Long id,
            @RequestBody UpdateAcceptanceCriteriaDTO request) {

        UserStory story = userStoryService.updateAcceptanceCriteria(
                id,
                request.getGivenClauses(),
                request.getWhenClauses(),
                request.getThenClauses()
        );
        return ResponseEntity.ok(UserStoryDTO.fromEntity(story));
    }

    @PatchMapping("/{id}/metrics")
    public ResponseEntity<UserStoryDTO> updateMetrics(
            @PathVariable Long id,
            @RequestBody UpdateMetricsDTO request) {

        UserStory story = userStoryService.updateMetrics(id, request.getMetrics());
        return ResponseEntity.ok(UserStoryDTO.fromEntity(story));
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