package com.Agile.demo.planning.service;

import com.Agile.demo.model.Project;
import com.Agile.demo.model.User;
import com.Agile.demo.planning.repository.ProjectRepository;
import com.Agile.demo.common.exception.ResourceNotFoundException;
import com.Agile.demo.common.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor  // Lombok génère le constructeur
@Slf4j  // Lombok génère le logger
@Transactional(readOnly = true)  // Par défaut, lecture seule
public class ProjectService {

    private final ProjectRepository projectRepository;

    /**
     * Crée un nouveau projet
     */
    @Transactional  // Override pour écriture
    public Project createProject(String name, String description,
                                 LocalDate startDate, LocalDate endDate) {
        log.info("Creating project: {}", name);

        // Validation métier
        if (projectRepository.existsByName(name)) {
            throw new BusinessException("A project with name '" + name + "' already exists");
        }

        if (endDate.isBefore(startDate)) {
            throw new BusinessException("End date must be after start date");
        }

        // Création
        Project project = new Project(name, description, startDate, endDate);

        // Le ProductBacklog est créé automatiquement dans le constructeur

        Project saved = projectRepository.save(project);
        log.info("Project created with id: {}", saved.getId());

        return saved;
    }

    /**
     * Récupère tous les projets
     */
    public List<Project> getAllProjects() {
        log.debug("Fetching all projects");
        return projectRepository.findAll();
    }

    /**
     * Récupère un projet par ID
     */
    public Project getProjectById(Long id) {
        log.debug("Fetching project with id: {}", id);
        return projectRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Project", id));
    }

    /**
     * Récupère un projet par nom
     */
    public Project getProjectByName(String name) {
        log.debug("Fetching project with name: {}", name);
        return projectRepository.findByName(name)
                .orElseThrow(() -> new ResourceNotFoundException("Project with name '" + name + "' not found"));
    }

    /**
     * Met à jour un projet
     */
    @Transactional
    public Project updateProject(Long id, String name, String description,
                                 LocalDate startDate, LocalDate endDate) {
        log.info("Updating project with id: {}", id);

        Project project = getProjectById(id);

        // Vérifier si le nouveau nom existe déjà (si changé)
        if (!project.getName().equals(name) && projectRepository.existsByName(name)) {
            throw new BusinessException("A project with name '" + name + "' already exists");
        }

        if (endDate.isBefore(startDate)) {
            throw new BusinessException("End date must be after start date");
        }

        project.setName(name);
        project.setDescription(description);
        project.setStartDate(startDate);
        project.setEndDate(endDate);

        return projectRepository.save(project);
    }

    /**
     * Supprime un projet
     */
    @Transactional
    public void deleteProject(Long id) {
        log.info("Deleting project with id: {}", id);

        Project project = getProjectById(id);
        projectRepository.delete(project);

        log.info("Project deleted: {}", id);
    }

    /**
     * Ajoute un membre au projet
     */
    @Transactional
    public void addMemberToProject(Long projectId, User user) {
        log.info("Adding user {} to project {}", user.getId(), projectId);

        Project project = getProjectById(projectId);

        if (project.getMembers().contains(user)) {
            throw new BusinessException("User is already a member of this project");
        }

        project.addMember(user);
        projectRepository.save(project);
    }

    /**
     * Retire un membre du projet
     */
    @Transactional
    public void removeMemberFromProject(Long projectId, User user) {
        log.info("Removing user {} from project {}", user.getId(), projectId);

        Project project = getProjectById(projectId);
        project.removeMember(user);
        projectRepository.save(project);
    }

    /**
     * Récupère les projets terminés
     */
    public List<Project> getCompletedProjects() {
        log.debug("Fetching completed projects");
        return projectRepository.findCompletedProjects(LocalDate.now());
    }

    /**
     * Récupère les projets d'un utilisateur
     */
    public List<Project> getProjectsByUser(Long userId) {
        log.debug("Fetching projects for user: {}", userId);
        return projectRepository.findProjectsByMemberId(userId);
    }
}