package com.Agile.demo.planning.controller;

import com.Agile.demo.planning.dto.project.AddMemberDTO;
import com.Agile.demo.planning.dto.project.CreateProjectDTO;
import com.Agile.demo.planning.dto.project.ProjectDTO;
import com.Agile.demo.planning.dto.project.UpdateProjectDTO;
import com.Agile.demo.planning.service.ProjectService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/projects")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class ProjectController {

    private final ProjectService projectService;

    /**
     * Crée un nouveau projet
     * Le ProductBacklog est créé automatiquement
     */
    @PostMapping
    public ResponseEntity<ProjectDTO> createProject(@Valid @RequestBody CreateProjectDTO createDto) {
        ProjectDTO dto = projectService.createProject(createDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(dto);
    }

    /**
     * Récupère tous les projets
     */
    @GetMapping
    public ResponseEntity<List<ProjectDTO>> getAllProjects() {
        List<ProjectDTO> projects = projectService.getAllProjects();
        return ResponseEntity.ok(projects);
    }

    /**
     * Récupère un projet par son ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<ProjectDTO> getProjectById(@PathVariable Long id) {
        ProjectDTO dto = projectService.getProjectDtoById(id);
        return ResponseEntity.ok(dto);
    }

    /**
     * Met à jour un projet existant
     */
    @PutMapping("/{id}")
    public ResponseEntity<ProjectDTO> updateProject(
            @PathVariable Long id,
            @Valid @RequestBody UpdateProjectDTO updateDto) {
        ProjectDTO dto = projectService.updateProject(id, updateDto);
        return ResponseEntity.ok(dto);
    }

    /**
     * Supprime un projet
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProject(@PathVariable Long id) {
        projectService.deleteProject(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Ajoute un membre au projet
     */
    @PostMapping("/{id}/members")
    public ResponseEntity<ProjectDTO> addMemberToProject(
            @PathVariable Long id,
            @Valid @RequestBody AddMemberDTO addMemberDto) {
        ProjectDTO dto = projectService.addMemberToProject(id, addMemberDto.getUserId());
        return ResponseEntity.ok(dto);
    }

    /**
     * Retire un membre du projet
     */
    @DeleteMapping("/{id}/members")
    public ResponseEntity<ProjectDTO> removeMemberFromProject(
            @PathVariable Long id,
            @Valid @RequestBody AddMemberDTO addMemberDTO) {
        ProjectDTO dto = projectService.removeMemberFromProject(id, addMemberDTO.getUserId());
        return ResponseEntity.ok(dto);
    }

    /**
     * Récupère les projets terminés
     */
    @GetMapping("/completed")
    public ResponseEntity<List<ProjectDTO>> getCompletedProjects() {
        List<ProjectDTO> projects = projectService.getCompletedProjects();
        return ResponseEntity.ok(projects);
    }

    /**
     * Récupère les projets actifs
     */
    @GetMapping("/active")
    public ResponseEntity<List<ProjectDTO>> getActiveProjects() {
        List<ProjectDTO> projects = projectService.getActiveProjects();
        return ResponseEntity.ok(projects);
    }

    /**
     * Récupère les projets d'un utilisateur
     */
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<ProjectDTO>> getProjectsByUser(@PathVariable Long userId) {
        List<ProjectDTO> projects = projectService.getProjectsByUser(userId);
        return ResponseEntity.ok(projects);
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