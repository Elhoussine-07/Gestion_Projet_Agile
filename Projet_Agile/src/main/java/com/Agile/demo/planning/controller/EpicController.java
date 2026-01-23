package com.Agile.demo.planning.controller;

import com.Agile.demo.planning.dto.epic.CreateEpicDTO;
import com.Agile.demo.planning.dto.epic.EpicDTO;
import com.Agile.demo.planning.dto.epic.UpdateEpicDTO;
import com.Agile.demo.planning.service.EpicService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/epics")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class EpicController {

    private final EpicService epicService;

    @PostMapping
    public ResponseEntity<EpicDTO> createEpic(@Valid @RequestBody CreateEpicDTO request) {
        EpicDTO epicDto = epicService.createEpic(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(epicDto);
    }

    @GetMapping
    public ResponseEntity<List<EpicDTO>> getAllEpics() {
        List<EpicDTO> epics = epicService.getAllEpics();
        return ResponseEntity.ok(epics);
    }

    @GetMapping("/{id}")
    public ResponseEntity<EpicDTO> getEpicById(@PathVariable Long id) {
        EpicDTO epic = epicService.getEpicById(id);
        return ResponseEntity.ok(epic);
    }

    @GetMapping("/backlog/{backlogId}")
    public ResponseEntity<List<EpicDTO>> getEpicsByBacklog(@PathVariable Long backlogId) {
        List<EpicDTO> epics = epicService.getEpicsByProductBacklog(backlogId);
        return ResponseEntity.ok(epics);
    }

    @PutMapping("/{id}")
    public ResponseEntity<EpicDTO> updateEpic(
            @PathVariable Long id,
            @Valid @RequestBody UpdateEpicDTO request) {

        EpicDTO epic = epicService.updateEpic(id, request);
        return ResponseEntity.ok(epic);
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
}