package com.Agile.demo.planning.controller;

import com.Agile.demo.planning.dto.productbacklog.*;
import com.Agile.demo.planning.dto.userstory.UserStoryDTO;
import com.Agile.demo.planning.service.ProductBacklogService;
import com.Agile.demo.model.ProductBacklog;
import com.Agile.demo.model.UserStory;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/product-backlogs")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class ProductBacklogController {

    private final ProductBacklogService productBacklogService;

    @GetMapping("/{id}")
    public ResponseEntity<ProductBacklogDTO> getProductBacklogById(@PathVariable Long id) {
        ProductBacklog backlog = productBacklogService.getProductBacklogById(id);
        return ResponseEntity.ok(ProductBacklogDTO.fromEntity(backlog));
    }

    @GetMapping("/project/{projectId}")
    public ResponseEntity<ProductBacklogDTO> getProductBacklogByProject(@PathVariable Long projectId) {
        ProductBacklog backlog = productBacklogService.getProductBacklogByProject(projectId);
        return ResponseEntity.ok(ProductBacklogDTO.fromEntity(backlog));
    }

    @GetMapping("/{id}/stories")
    public ResponseEntity<List<UserStoryDTO>> getAllStories(@PathVariable Long id) {
        List<UserStory> stories = productBacklogService.getAllStories(id);
        List<UserStoryDTO> dtos = stories.stream()
                .map(UserStoryDTO::fromEntity)
                .collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }

    @GetMapping("/{id}/stories/unassigned")
    public ResponseEntity<List<UserStoryDTO>> getUnassignedStories(@PathVariable Long id) {
        List<UserStory> stories = productBacklogService.getUnassignedStories(id);
        List<UserStoryDTO> dtos = stories.stream()
                .map(UserStoryDTO::fromEntity)
                .collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }

    @GetMapping("/{id}/top-stories")
    public ResponseEntity<List<UserStoryDTO>> getTopPriorityStories(
            @PathVariable Long id,
            @RequestParam(defaultValue = "10") int limit) {

        List<UserStory> stories = productBacklogService.getTopPriorityStories(id, limit);
        List<UserStoryDTO> dtos = stories.stream()
                .map(UserStoryDTO::fromEntity)
                .collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }

    @PostMapping("/{id}/prioritize")
    public ResponseEntity<Void> applyPrioritization(
            @PathVariable Long id,
            @RequestBody PrioritizeRequestDTO request) {

        productBacklogService.applyPrioritization(id, request.getMethod());
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