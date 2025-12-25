package com.Agile.demo.execution.controllers;

import com.Agile.demo.execution.dto.SprintCloneRequest;
import com.Agile.demo.execution.dto.SprintCreateRequest;
import com.Agile.demo.execution.dto.SprintUpdateRequest;
import com.Agile.demo.execution.services.SprintService;
import com.Agile.demo.model.SprintBacklog;
import com.Agile.demo.model.SprintStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/sprints")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class SprintController {

    private final SprintService sprintService;

    @PostMapping
    public ResponseEntity<SprintBacklog> createSprint(@RequestBody SprintCreateRequest request) {
        SprintBacklog sprint = sprintService.createSprint(
                request.getProjectId(),
                request.getSprintNumber(),
                request.getStartDate(),
                request.getEndDate(),
                request.getGoal()
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(sprint);
    }


    @GetMapping("/project/{projectId}")
    public ResponseEntity<List<SprintBacklog>> getSprintsByProject(@PathVariable Long projectId) {
        List<SprintBacklog> sprints = sprintService.getSprintsByProject(projectId);
        return ResponseEntity.ok(sprints);
    }


    @GetMapping("/project/{projectId}/active")
    public ResponseEntity<SprintBacklog> getActiveSprint(@PathVariable Long projectId) {
        SprintBacklog sprint = sprintService.getActiveSprint(projectId);
        return ResponseEntity.ok(sprint);
    }


    @GetMapping("/{sprintId}")
    public ResponseEntity<SprintBacklog> getSprintById(@PathVariable Long sprintId) {
        SprintBacklog sprint = sprintService.getSprintById(sprintId);
        return ResponseEntity.ok(sprint);
    }


    @PutMapping("/{sprintId}")
    public ResponseEntity<SprintBacklog> updateSprint(
            @PathVariable Long sprintId,
            @RequestBody SprintUpdateRequest request) {

        SprintBacklog sprint = sprintService.updateSprint(
                sprintId,
                request.getStartDate(),
                request.getEndDate(),
                request.getGoal()
        );
        return ResponseEntity.ok(sprint);
    }


    @PostMapping("/{sprintId}/start")
    public ResponseEntity<SprintBacklog> startSprint(@PathVariable Long sprintId) {
        SprintBacklog sprint = sprintService.startSprint(sprintId);
        return ResponseEntity.ok(sprint);
    }


    @PostMapping("/{sprintId}/complete")
    public ResponseEntity<SprintBacklog> completeSprint(@PathVariable Long sprintId) {
        SprintBacklog sprint = sprintService.completeSprint(sprintId);
        return ResponseEntity.ok(sprint);
    }


    @PostMapping("/{sprintId}/cancel")
    public ResponseEntity<SprintBacklog> cancelSprint(@PathVariable Long sprintId) {
        SprintBacklog sprint = sprintService.cancelSprint(sprintId);
        return ResponseEntity.ok(sprint);
    }


    @DeleteMapping("/{sprintId}")
    public ResponseEntity<Void> deleteSprint(@PathVariable Long sprintId) {
        sprintService.deleteSprint(sprintId);
        return ResponseEntity.noContent().build();
    }


    @GetMapping("/{sprintId}/metrics")
    public ResponseEntity<SprintService.SprintMetrics> getSprintMetrics(@PathVariable Long sprintId) {
        SprintService.SprintMetrics metrics = sprintService.getSprintMetrics(sprintId);
        return ResponseEntity.ok(metrics);
    }


    @GetMapping("/project/{projectId}/last")
    public ResponseEntity<SprintBacklog> getLastSprint(@PathVariable Long projectId) {
        SprintBacklog sprint = sprintService.getLastSprint(projectId);
        return ResponseEntity.ok(sprint);
    }


    @GetMapping("/project/{projectId}/status/{status}")
    public ResponseEntity<List<SprintBacklog>> getSprintsByStatus(
            @PathVariable Long projectId,
            @PathVariable SprintStatus status) {

        List<SprintBacklog> sprints = sprintService.getSprintsByStatus(projectId, status);
        return ResponseEntity.ok(sprints);
    }


    @GetMapping("/{sprintId}/can-start")
    public ResponseEntity<Boolean> canStartSprint(@PathVariable Long sprintId) {
        boolean canStart = sprintService.canStartSprint(sprintId);
        return ResponseEntity.ok(canStart);
    }


    @GetMapping("/project/{projectId}/between-dates")
    public ResponseEntity<List<SprintBacklog>> getSprintsBetweenDates(
            @PathVariable Long projectId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {

        List<SprintBacklog> sprints = sprintService.getSprintsBetweenDates(projectId, startDate, endDate);
        return ResponseEntity.ok(sprints);
    }


    @PostMapping("/{fromSprintId}/move-story/{userStoryId}/to/{toSprintId}")
    public ResponseEntity<Void> moveUserStoryBetweenSprints(
            @PathVariable("fromSprintId") Long fromSprintId,
            @PathVariable("userStoryId") Long userStoryId,
            @PathVariable("toSprintId") Long toSprintId) {

        sprintService.moveUserStoryBetweenSprints(fromSprintId, toSprintId, userStoryId);
        return ResponseEntity.ok().build();
    }



    @GetMapping("/{sprintId}/burndown")
    public ResponseEntity<SprintService.SprintBurndown> getSprintBurndown(@PathVariable Long sprintId) {
        SprintService.SprintBurndown burndown = sprintService.getSprintBurndown(sprintId);
        return ResponseEntity.ok(burndown);
    }


    @GetMapping("/project/{projectId}/incomplete-stories")
    public ResponseEntity<List<SprintBacklog>> getSprintsWithIncompleteStories(@PathVariable Long projectId) {
        List<SprintBacklog> sprints = sprintService.getSprintsWithIncompleteStories(projectId);
        return ResponseEntity.ok(sprints);
    }


    @PostMapping("/{sprintId}/clone")
    public ResponseEntity<SprintBacklog> cloneSprint(
            @PathVariable Long sprintId,
            @RequestBody SprintCloneRequest request) {

        SprintBacklog sprint = sprintService.cloneSprint(
                sprintId,
                request.getNewSprintNumber(),
                request.getNewStartDate(),
                request.getNewEndDate()
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(sprint);
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