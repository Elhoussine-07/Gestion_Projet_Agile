package com.Agile.demo.planning.service;

import com.Agile.demo.execution.repositories.UserRepository;
import com.Agile.demo.model.Project;
import com.Agile.demo.model.User;
import com.Agile.demo.planning.repository.ProjectRepository;
import com.Agile.demo.planning.mapper.ProjectMapper;
import com.Agile.demo.planning.dto.project.CreateProjectDTO;
import com.Agile.demo.planning.dto.project.ProjectDTO;
import com.Agile.demo.planning.dto.project.UpdateProjectDTO;
import com.Agile.demo.exception.ResourceNotFoundException;
import com.Agile.demo.exception.BusinessException;
import com.Agile.demo.exception.ValidationException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class ProjectService {

    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;
    private final ProjectMapper projectMapper;

    /**
     * Crée un nouveau projet
     * Le ProductBacklog est créé automatiquement via @PrePersist dans l'entité Project
     */
    @Transactional
    public ProjectDTO createProject(CreateProjectDTO createDto) {
        log.info("Creating project: {}", createDto.getName());

        // Validation métier
        if (projectRepository.existsByName(createDto.getName())) {
            throw new BusinessException("A project with name '" + createDto.getName() + "' already exists");
        }

        if (createDto.getEndDate().isBefore(createDto.getStartDate())) {
            throw new ValidationException("End date must be after start date");
        }

        // Conversion DTO -> Entity via mapper
        Project project = projectMapper.toEntity(createDto);

        // Sauvegarde (le @PrePersist créera automatiquement le ProductBacklog)
        Project savedProject = projectRepository.save(project);

        log.info("Project created with id: {} and ProductBacklog id: {}",
                savedProject.getId(),
                savedProject.getProductBacklog().getId());

        return projectMapper.toDto(savedProject);
    }

    /**
     * Récupère tous les projets
     */
    public List<ProjectDTO> getAllProjects() {
        log.debug("Fetching all projects");
        List<Project> projects = projectRepository.findAll();
        return projectMapper.toDtoList(projects);
    }

    /**
     * Récupère un projet par ID (retourne l'entité)
     */
    public Project getProjectById(Long id) {
        log.debug("Fetching project with id: {}", id);
        return projectRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Project", id));
    }

    /**
     * Récupère un projet par ID (retourne le DTO)
     */
    public ProjectDTO getProjectDtoById(Long id) {
        Project project = getProjectById(id);
        return projectMapper.toDto(project);
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
     * Récupère un projet par nom (retourne le DTO)
     */
    public ProjectDTO getProjectDtoByName(String name) {
        Project project = getProjectByName(name);
        return projectMapper.toDto(project);
    }

    /**
     * Met à jour un projet
     */
    @Transactional
    public ProjectDTO updateProject(Long id, UpdateProjectDTO updateDto) {
        log.info("Updating project with id: {}", id);

        Project project = getProjectById(id);

        // Vérifier si le nouveau nom existe déjà (si changé)
        if (updateDto.getName() != null
                && !project.getName().equals(updateDto.getName())
                && projectRepository.existsByName(updateDto.getName())) {
            throw new BusinessException("A project with name '" + updateDto.getName() + "' already exists");
        }

        // Valider les dates si elles sont fournies
        LocalDate startDate = updateDto.getStartDate() != null ? updateDto.getStartDate() : project.getStartDate();
        LocalDate endDate = updateDto.getEndDate() != null ? updateDto.getEndDate() : project.getEndDate();

        if (endDate.isBefore(startDate)) {
            throw new ValidationException("End date must be after start date");
        }

        // Mettre à jour via le mapper
        projectMapper.updateEntityFromDto(updateDto, project);

        Project updatedProject = projectRepository.save(project);

        log.info("Project updated: {}", id);
        return projectMapper.toDto(updatedProject);
    }

    /**
     * Supprime un projet
     */
    @Transactional
    public void deleteProject(Long id) {
        log.info("Deleting project with id: {}", id);

        Project project = getProjectById(id);

        // Vérification métier optionnelle
        if (project.getSprints() != null && !project.getSprints().isEmpty()) {
            log.warn("Deleting project {} with {} sprints", id, project.getSprints().size());
        }

        projectRepository.delete(project);

        log.info("Project deleted: {}", id);
    }

    /**
     * Ajoute un membre au projet
     */
    @Transactional
    public ProjectDTO addMemberToProject(Long projectId, Long userId) {
        log.info("Adding user {} to project {}", userId, projectId);

        Project project = getProjectById(projectId);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", userId));

        if (project.getMembers().contains(user)) {
            throw new BusinessException("User is already a member of this project");
        }

        project.addMember(user);
        Project updatedProject = projectRepository.save(project);

        log.info("User {} added to project {}", userId, projectId);
        return projectMapper.toDto(updatedProject);
    }

    /**
     * Retire un membre du projet
     */
    @Transactional
    public ProjectDTO removeMemberFromProject(Long projectId, Long userId) {
        log.info("Removing user {} from project {}", userId, projectId);

        Project project = getProjectById(projectId);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", userId));

        if (!project.getMembers().contains(user)) {
            throw new BusinessException("User is not a member of this project");
        }

        project.removeMember(user);
        Project updatedProject = projectRepository.save(project);

        log.info("User {} removed from project {}", userId, projectId);
        return projectMapper.toDto(updatedProject);
    }

    /**
     * Récupère les projets terminés
     */
    public List<ProjectDTO> getCompletedProjects() {
        log.debug("Fetching completed projects");
        List<Project> projects = projectRepository.findCompletedProjects(LocalDate.now());
        return projectMapper.toDtoList(projects);
    }

    /**
     * Récupère les projets actifs (non terminés)
     */
    public List<ProjectDTO> getActiveProjects() {
        log.debug("Fetching active projects");
        LocalDate now = LocalDate.now();
        List<Project> projects = projectRepository.findAll().stream()
                .filter(p -> p.getEndDate().isAfter(now) || p.getEndDate().isEqual(now))
                .toList();
        return projectMapper.toDtoList(projects);
    }

    /**
     * Récupère les projets d'un utilisateur
     */
    public List<ProjectDTO> getProjectsByUser(Long userId) {
        log.debug("Fetching projects for user: {}", userId);

        // Vérifier que l'utilisateur existe
        if (!userRepository.existsById(userId)) {
            throw new ResourceNotFoundException("User", userId);
        }

        List<Project> projects = projectRepository.findProjectsByMemberId(userId);
        return projectMapper.toDtoList(projects);
    }
}