package com.Agile.demo.planning.controller;

import com.Agile.demo.planning.dto.epic.CreateEpicDTO;
import com.Agile.demo.planning.dto.epic.EpicDTO;
import com.Agile.demo.planning.dto.epic.UpdateEpicDTO;
import com.Agile.demo.planning.service.EpicService;
import com.Agile.demo.model.Epic;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/epics")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class EpicController {

    private final EpicService epicService;

    @PostMapping
    public ResponseEntity<EpicDTO> createEpic(@RequestBody CreateEpicDTO request) {
        Epic epic = epicService.createEpic(
                request.getProductBacklogId(),
                request.getTitle(),
                request.getDescription()
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(EpicDTO.fromEntity(epic));
    }

    @GetMapping
    public ResponseEntity<List<EpicDTO>> getAllEpics() {
        List<Epic> epics = epicService.getAllEpics();
        List<EpicDTO> dtos = epics.stream()
                .map(EpicDTO::fromEntity)
                .collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }

    @GetMapping("/{id}")
    public ResponseEntity<EpicDTO> getEpicById(@PathVariable Long id) {
        Epic epic = epicService.getEpicById(id);
        return ResponseEntity.ok(EpicDTO.fromEntity(epic));
    }

    @GetMapping("/backlog/{backlogId}")
    public ResponseEntity<List<EpicDTO>> getEpicsByBacklog(@PathVariable Long backlogId) {
        List<Epic> epics = epicService.getEpicsByProductBacklog(backlogId);
        List<EpicDTO> dtos = epics.stream()
                .map(EpicDTO::fromEntity)
                .collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }

    @PutMapping("/{id}")
    public ResponseEntity<EpicDTO> updateEpic(
            @PathVariable Long id,
            @RequestBody UpdateEpicDTO request) {

        Epic epic = epicService.updateEpic(
                id,
                request.getTitle(),
                request.getDescription()
        );
        return ResponseEntity.ok(EpicDTO.fromEntity(epic));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteEpic(@PathVariable Long id) {
        epicService.deleteEpic(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/stories/{storyId}")
    public ResponseEntity<Void> addUserStoryToEpic(
            @PathVariable Long id,
            @PathVariable Long storyId) {

        epicService.addUserStoryToEpic(id, storyId);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{id}/stories/{storyId}")
    public ResponseEntity<Void> removeUserStoryFromEpic(
            @PathVariable Long id,
            @PathVariable Long storyId) {

        epicService.removeUserStoryFromEpic(id, storyId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}/progress")
    public ResponseEntity<Integer> getEpicProgress(@PathVariable Long id) {
        int progress = epicService.calculateEpicProgress(id);
        return ResponseEntity.ok(progress);
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