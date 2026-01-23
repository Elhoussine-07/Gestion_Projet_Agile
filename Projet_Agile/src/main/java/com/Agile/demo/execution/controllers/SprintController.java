package com.Agile.demo.execution.controllers;

import com.Agile.demo.execution.dto.SprintBacklogResponseDTO;
import com.Agile.demo.execution.dto.SprintCloneRequest;
import com.Agile.demo.execution.dto.SprintCreateRequest;
import com.Agile.demo.execution.dto.SprintUpdateRequest;
import com.Agile.demo.execution.services.SprintService;
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
    public ResponseEntity<SprintBacklogResponseDTO> createSprint(@RequestBody SprintCreateRequest request) {
        SprintBacklogResponseDTO sprint = sprintService.createSprint(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(sprint);
    }

    @GetMapping("/project/{projectId}")
    public ResponseEntity<List<SprintBacklogResponseDTO>> getSprintsByProject(@PathVariable Long projectId) {
        List<SprintBacklogResponseDTO> sprints = sprintService.getSprintsByProject(projectId);
        return ResponseEntity.ok(sprints);
    }

    @GetMapping("/project/{projectId}/active")
    public ResponseEntity<SprintBacklogResponseDTO> getActiveSprint(@PathVariable Long projectId) {
        SprintBacklogResponseDTO sprint = sprintService.getActiveSprint(projectId);
        return ResponseEntity.ok(sprint);
    }

    @GetMapping("/{sprintId}")
    public ResponseEntity<SprintBacklogResponseDTO> getSprintById(@PathVariable Long sprintId) {
        SprintBacklogResponseDTO sprint = sprintService.getSprintDtoById(sprintId);
        return ResponseEntity.ok(sprint);
    }

    @PutMapping("/{sprintId}")
    public ResponseEntity<SprintBacklogResponseDTO> updateSprint(
            @PathVariable Long sprintId,
            @RequestBody SprintUpdateRequest request) {

        SprintBacklogResponseDTO sprint = sprintService.updateSprint(
                sprintId,
                request
        );
        return ResponseEntity.ok(sprint);
    }

    @PostMapping("/{sprintId}/start")
    public ResponseEntity<SprintBacklogResponseDTO> startSprint(@PathVariable Long sprintId) {
        SprintBacklogResponseDTO sprint = sprintService.startSprint(sprintId);
        return ResponseEntity.ok(sprint);
    }

    @PostMapping("/{sprintId}/complete")
    public ResponseEntity<SprintBacklogResponseDTO> completeSprint(@PathVariable Long sprintId) {
        SprintBacklogResponseDTO sprint = sprintService.completeSprint(sprintId);
        return ResponseEntity.ok(sprint);
    }

    @PostMapping("/{sprintId}/cancel")
    public ResponseEntity<SprintBacklogResponseDTO> cancelSprint(@PathVariable Long sprintId) {
        SprintBacklogResponseDTO sprint = sprintService.cancelSprint(sprintId);
        return ResponseEntity.ok(sprint);
    }

    @DeleteMapping("/{sprintId}")
    public ResponseEntity<Void> deleteSprint(@PathVariable Long sprintId) {
        sprintService.deleteSprint(sprintId);
        return ResponseEntity.noContent().build();
    }


    @PostMapping("/{sprintId}/user-stories/{userStoryId}")
    public ResponseEntity<SprintBacklogResponseDTO> addUserStoryToSprint(
            @PathVariable Long sprintId,
            @PathVariable Long userStoryId) {
        SprintBacklogResponseDTO sprint = sprintService.addUserStoryToSprint(sprintId, userStoryId);
        return ResponseEntity.ok(sprint);
    }

    @PostMapping("/{sprintId}/user-stories/batch")
    public ResponseEntity<SprintBacklogResponseDTO> addMultipleUserStoriesToSprint(
            @PathVariable Long sprintId,
            @RequestBody List<Long> userStoryIds) {
        SprintBacklogResponseDTO sprint = sprintService.addMultipleUserStoriesToSprint(sprintId, userStoryIds);
        return ResponseEntity.ok(sprint);
    }

    // NOUVEL ENDPOINT: Retirer une User Story d'un Sprint
    @DeleteMapping("/{sprintId}/user-stories/{userStoryId}")
    public ResponseEntity<SprintBacklogResponseDTO> removeUserStoryFromSprint(
            @PathVariable Long sprintId,
            @PathVariable Long userStoryId) {
        SprintBacklogResponseDTO sprint = sprintService.removeUserStoryFromSprint(sprintId, userStoryId);
        return ResponseEntity.ok(sprint);
    }

    @GetMapping("/{sprintId}/metrics")
    public ResponseEntity<SprintService.SprintMetrics> getSprintMetrics(@PathVariable Long sprintId) {
        SprintService.SprintMetrics metrics = sprintService.getSprintMetrics(sprintId);
        return ResponseEntity.ok(metrics);
    }

    @GetMapping("/project/{projectId}/last")
    public ResponseEntity<SprintBacklogResponseDTO> getLastSprint(@PathVariable Long projectId) {
        SprintBacklogResponseDTO sprint = sprintService.getLastSprint(projectId);
        return ResponseEntity.ok(sprint);
    }

    @GetMapping("/project/{projectId}/status/{status}")
    public ResponseEntity<List<SprintBacklogResponseDTO>> getSprintsByStatus(
            @PathVariable Long projectId,
            @PathVariable SprintStatus status) {

        List<SprintBacklogResponseDTO> sprints = sprintService.getSprintsByStatus(projectId, status);
        return ResponseEntity.ok(sprints);
    }

    @GetMapping("/{sprintId}/can-start")
    public ResponseEntity<Boolean> canStartSprint(@PathVariable Long sprintId) {
        boolean canStart = sprintService.canStartSprint(sprintId);
        return ResponseEntity.ok(canStart);
    }

    @GetMapping("/project/{projectId}/between-dates")
    public ResponseEntity<List<SprintBacklogResponseDTO>> getSprintsBetweenDates(
            @PathVariable Long projectId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {

        List<SprintBacklogResponseDTO> sprints = sprintService.getSprintsBetweenDates(projectId, startDate, endDate);
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
    public ResponseEntity<List<SprintBacklogResponseDTO>> getSprintsWithIncompleteStories(@PathVariable Long projectId) {
        List<SprintBacklogResponseDTO> sprints = sprintService.getSprintsWithIncompleteStories(projectId);
        return ResponseEntity.ok(sprints);
    }

    @PostMapping("/{sprintId}/clone")
    public ResponseEntity<SprintBacklogResponseDTO> cloneSprint(
            @PathVariable Long sprintId,
            @RequestBody SprintCloneRequest request) {

        SprintBacklogResponseDTO sprint = sprintService.cloneSprint(
                sprintId,
                request
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


//dyal db