package com.Agile.demo.planning.controller;

import com.Agile.demo.planning.dto.productbacklog.*;
import com.Agile.demo.planning.dto.userstory.UserStoryDTO;
import com.Agile.demo.planning.service.ProductBacklogService;
import com.Agile.demo.planning.mapper.UserStoryMapper;
import com.Agile.demo.model.UserStory;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/product-backlogs")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class ProductBacklogController {

    private final ProductBacklogService productBacklogService;
    private final UserStoryMapper userStoryMapper;

    /**
     * Récupère un Product Backlog par son ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<ProductBacklogDTO> getProductBacklogById(@PathVariable Long id) {
        ProductBacklogDTO dto = productBacklogService.getProductBacklogDtoById(id);
        return ResponseEntity.ok(dto);
    }

    /**
     * Récupère le Product Backlog d'un projet
     * Note: Le ProductBacklog est automatiquement créé avec le Project
     */
    @GetMapping("/project/{projectId}")
    public ResponseEntity<ProductBacklogDTO> getProductBacklogByProject(@PathVariable Long projectId) {
        ProductBacklogDTO dto = productBacklogService.getProductBacklogDtoByProject(projectId);
        return ResponseEntity.ok(dto);
    }

    /**
     * Met à jour un Product Backlog existant
     * Seul le nom et la méthode de priorisation peuvent être modifiés
     */
    @PutMapping("/{id}")
    public ResponseEntity<ProductBacklogDTO> updateProductBacklog(
            @PathVariable Long id,
            @Valid @RequestBody UpdateProductBacklogDTO updateDto) {
        ProductBacklogDTO dto = productBacklogService.updateProductBacklog(id, updateDto);
        return ResponseEntity.ok(dto);
    }

    /**
     * Récupère toutes les User Stories d'un backlog
     */
    @GetMapping("/{id}/stories")
    public ResponseEntity<List<UserStoryDTO>> getAllStories(@PathVariable Long id) {
        List<UserStory> stories = productBacklogService.getAllStories(id);
        List<UserStoryDTO> dtos = userStoryMapper.toDtoList(stories);
        return ResponseEntity.ok(dtos);
    }

    /**
     * Récupère les User Stories non assignées à un sprint
     */
    @GetMapping("/{id}/stories/unassigned")
    public ResponseEntity<List<UserStoryDTO>> getUnassignedStories(@PathVariable Long id) {
        List<UserStory> stories = productBacklogService.getUnassignedStories(id);
        List<UserStoryDTO> dtos = userStoryMapper.toDtoList(stories);
        return ResponseEntity.ok(dtos);
    }

    /**
     * Récupère les User Stories les plus prioritaires
     */
    @GetMapping("/{id}/top-stories")
    public ResponseEntity<List<UserStoryDTO>> getTopPriorityStories(
            @PathVariable Long id,
            @RequestParam(defaultValue = "10") int limit) {

        List<UserStory> stories = productBacklogService.getTopPriorityStories(id, limit);
        List<UserStoryDTO> dtos = userStoryMapper.toDtoList(stories);
        return ResponseEntity.ok(dtos);
    }

    /**
     * Applique une méthode de priorisation au backlog
     */
    @PostMapping("/{id}/prioritize")
    public ResponseEntity<Void> applyPrioritization(
            @PathVariable Long id,
            @Valid @RequestBody PrioritizeRequestDTO request) {

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