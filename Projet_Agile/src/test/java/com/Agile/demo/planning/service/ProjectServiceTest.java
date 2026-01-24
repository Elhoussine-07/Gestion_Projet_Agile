package com.Agile.demo.planning.service;

import com.Agile.demo.execution.repositories.UserRepository;
import com.Agile.demo.model.ProductBacklog;
import com.Agile.demo.model.Project;
import com.Agile.demo.model.User;
import com.Agile.demo.planning.dto.project.CreateProjectDTO;
import com.Agile.demo.planning.dto.project.ProjectDTO;
import com.Agile.demo.planning.dto.project.UpdateProjectDTO;
import com.Agile.demo.planning.mapper.ProjectMapper;
import com.Agile.demo.planning.repository.ProjectRepository;
import com.Agile.demo.exception.ResourceNotFoundException;
import com.Agile.demo.exception.BusinessException;
import com.Agile.demo.exception.ValidationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProjectServiceTest {

    @Mock
    private ProjectRepository projectRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ProjectMapper projectMapper;

    @InjectMocks
    private ProjectService projectService;

    private Project project;
    private ProjectDTO projectDTO;
    private CreateProjectDTO createProjectDTO;
    private UpdateProjectDTO updateProjectDTO;
    private User user;

    @BeforeEach
    void setUp() {
        // Créer le ProductBacklog manuellement pour simuler @PrePersist
        ProductBacklog backlog = new ProductBacklog();
        backlog.setId(1L);
        backlog.setName("Test Project - Product Backlog");

        project = Project.builder()
                .name("Test Project")
                .description("Description")
                .startDate(LocalDate.now())
                .endDate(LocalDate.now().plusMonths(3))
                .build();
        project.setId(1L);
        project.setProductBacklog(backlog);
        backlog.setProject(project);

        projectDTO = ProjectDTO.builder()
                .id(1L)
                .name("Test Project")
                .description("Description")
                .startDate(LocalDate.now())
                .endDate(LocalDate.now().plusMonths(3))
                .productBacklogId(1L)
                .memberCount(0)
                .sprintCount(0)
                .build();

        createProjectDTO = new CreateProjectDTO(
                "Test Project",
                "Description",
                LocalDate.now(),
                LocalDate.now().plusMonths(3)
        );

        updateProjectDTO = new UpdateProjectDTO(
                "Updated Project",
                "Updated Description",
                LocalDate.now(),
                LocalDate.now().plusMonths(6)
        );

        user = new User();
        user.setId(1L);
        user.setUsername("testuser");
    }

    // ========== Tests CREATE ==========

    @Test
    void createProject_shouldCreateAndReturnDto() {
        when(projectRepository.existsByName("Test Project")).thenReturn(false);
        when(projectMapper.toEntity(createProjectDTO)).thenReturn(project);
        when(projectRepository.save(any(Project.class))).thenReturn(project);
        when(projectMapper.toDto(project)).thenReturn(projectDTO);

        ProjectDTO result = projectService.createProject(createProjectDTO);

        assertThat(result).isNotNull();
        assertThat(result.getName()).isEqualTo("Test Project");
        assertThat(result.getProductBacklogId()).isEqualTo(1L);
        verify(projectRepository).save(any(Project.class));
        verify(projectMapper).toDto(project);
    }

    @Test
    void createProject_shouldThrowException_whenProjectNameExists() {
        when(projectRepository.existsByName("Test Project")).thenReturn(true);

        assertThatThrownBy(() -> projectService.createProject(createProjectDTO))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("already exists");

        verify(projectRepository, never()).save(any());
    }

    @Test
    void createProject_shouldThrowException_whenEndDateBeforeStartDate() {
        CreateProjectDTO invalidDto = new CreateProjectDTO(
                "Test",
                "Desc",
                LocalDate.now(),
                LocalDate.now().minusDays(1)
        );

        assertThatThrownBy(() -> projectService.createProject(invalidDto))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("End date must be after start date");

        verify(projectRepository, never()).save(any());
    }

    // ========== Tests READ ==========

    @Test
    void getProjectById_shouldReturnProject() {
        when(projectRepository.findById(1L)).thenReturn(Optional.of(project));

        Project result = projectService.getProjectById(1L);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        verify(projectRepository).findById(1L);
    }

    @Test
    void getProjectDtoById_shouldReturnDto() {
        when(projectRepository.findById(1L)).thenReturn(Optional.of(project));
        when(projectMapper.toDto(project)).thenReturn(projectDTO);

        ProjectDTO result = projectService.getProjectDtoById(1L);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1);
        verify(projectMapper).toDto(project);
    }

    @Test
    void getProjectById_shouldThrowException_whenProjectNotFound() {
        when(projectRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> projectService.getProjectById(999L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void getProjectByName_shouldReturnProject() {
        when(projectRepository.findByName("Test Project")).thenReturn(Optional.of(project));

        Project result = projectService.getProjectByName("Test Project");

        assertThat(result).isNotNull();
        assertThat(result.getName()).isEqualTo("Test Project");
    }

    @Test
    void getProjectDtoByName_shouldReturnDto() {
        when(projectRepository.findByName("Test Project")).thenReturn(Optional.of(project));
        when(projectMapper.toDto(project)).thenReturn(projectDTO);

        ProjectDTO result = projectService.getProjectDtoByName("Test Project");

        assertThat(result).isNotNull();
        assertThat(result.getName()).isEqualTo("Test Project");
        verify(projectMapper).toDto(project);
    }

    @Test
    void getAllProjects_shouldReturnDtoList() {
        when(projectRepository.findAll()).thenReturn(List.of(project));
        when(projectMapper.toDtoList(List.of(project))).thenReturn(List.of(projectDTO));

        List<ProjectDTO> result = projectService.getAllProjects();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getName()).isEqualTo("Test Project");
        verify(projectMapper).toDtoList(List.of(project));
    }

    // ========== Tests UPDATE ==========

    @Test
    void updateProject_shouldUpdateAndReturnDto() {
        ProjectDTO updatedDTO = ProjectDTO.builder()
                .id(1L)
                .name("Updated Project")
                .description("Updated Description")
                .build();

        when(projectRepository.findById(1L)).thenReturn(Optional.of(project));
        when(projectRepository.existsByName("Updated Project")).thenReturn(false);
        doNothing().when(projectMapper).updateEntityFromDto(updateProjectDTO, project);
        when(projectRepository.save(project)).thenReturn(project);
        when(projectMapper.toDto(project)).thenReturn(updatedDTO);

        ProjectDTO result = projectService.updateProject(1L, updateProjectDTO);

        assertThat(result).isNotNull();
        assertThat(result.getName()).isEqualTo("Updated Project");
        verify(projectMapper).updateEntityFromDto(updateProjectDTO, project);
        verify(projectRepository).save(project);
    }

    @Test
    void updateProject_shouldThrowException_whenNewNameAlreadyExists() {
        when(projectRepository.findById(1L)).thenReturn(Optional.of(project));
        when(projectRepository.existsByName("Updated Project")).thenReturn(true);

        assertThatThrownBy(() -> projectService.updateProject(1L, updateProjectDTO))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("already exists");

        verify(projectRepository, never()).save(any());
    }

    @Test
    void updateProject_shouldThrowException_whenEndDateBeforeStartDate() {
        UpdateProjectDTO invalidDto = new UpdateProjectDTO(
                "Test",
                "Desc",
                LocalDate.now(),
                LocalDate.now().minusDays(1)
        );

        when(projectRepository.findById(1L)).thenReturn(Optional.of(project));

        assertThatThrownBy(() -> projectService.updateProject(1L, invalidDto))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("End date must be after start date");

        verify(projectRepository, never()).save(any());
    }

    // ========== Tests DELETE ==========

    @Test
    void deleteProject_shouldDeleteProject() {
        when(projectRepository.findById(1L)).thenReturn(Optional.of(project));

        projectService.deleteProject(1L);

        verify(projectRepository).delete(project);
    }

    @Test
    void deleteProject_shouldThrowException_whenProjectNotFound() {
        when(projectRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> projectService.deleteProject(999L))
                .isInstanceOf(ResourceNotFoundException.class);

        verify(projectRepository, never()).delete(any());
    }

    // ========== Tests MEMBRES ==========

    @Test
    void addMemberToProject_shouldAddMemberAndReturnDto() {
        when(projectRepository.findById(1L)).thenReturn(Optional.of(project));
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(projectRepository.save(project)).thenReturn(project);
        when(projectMapper.toDto(project)).thenReturn(projectDTO);

        ProjectDTO result = projectService.addMemberToProject(1L, 1L);

        assertThat(result).isNotNull();
        verify(projectRepository).save(project);
        verify(projectMapper).toDto(project);
    }

    @Test
    void addMemberToProject_shouldThrowException_whenUserAlreadyMember() {
        project.addMember(user);

        when(projectRepository.findById(1L)).thenReturn(Optional.of(project));
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        assertThatThrownBy(() -> projectService.addMemberToProject(1L, 1L))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("already a member");

        verify(projectRepository, never()).save(any());
    }

    @Test
    void removeMemberFromProject_shouldRemoveMemberAndReturnDto() {
        project.addMember(user);

        when(projectRepository.findById(1L)).thenReturn(Optional.of(project));
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(projectRepository.save(project)).thenReturn(project);
        when(projectMapper.toDto(project)).thenReturn(projectDTO);

        ProjectDTO result = projectService.removeMemberFromProject(1L, 1L);

        assertThat(result).isNotNull();
        verify(projectRepository).save(project);
        verify(projectMapper).toDto(project);
    }

    @Test
    void removeMemberFromProject_shouldThrowException_whenUserNotMember() {
        when(projectRepository.findById(1L)).thenReturn(Optional.of(project));
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        assertThatThrownBy(() -> projectService.removeMemberFromProject(1L, 1L))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("not a member");

        verify(projectRepository, never()).save(any());
    }

    // ========== Tests QUERIES ==========

    @Test
    void getCompletedProjects_shouldReturnDtoList() {
        LocalDate past = LocalDate.now().minusMonths(1);
        Project completedProject = Project.builder()
                .name("Completed")
                .endDate(past)
                .build();

        when(projectRepository.findCompletedProjects(any(LocalDate.class)))
                .thenReturn(List.of(completedProject));
        when(projectMapper.toDtoList(List.of(completedProject)))
                .thenReturn(List.of(projectDTO));

        List<ProjectDTO> result = projectService.getCompletedProjects();

        assertThat(result).hasSize(1);
        verify(projectMapper).toDtoList(List.of(completedProject));
    }

    @Test
    void getActiveProjects_shouldReturnDtoList() {
        when(projectRepository.findAll()).thenReturn(List.of(project));
        when(projectMapper.toDtoList(anyList())).thenReturn(List.of(projectDTO));

        List<ProjectDTO> result = projectService.getActiveProjects();

        assertThat(result).hasSize(1);
        verify(projectMapper).toDtoList(anyList());
    }

    @Test
    void getProjectsByUser_shouldReturnDtoList() {
        when(userRepository.existsById(1L)).thenReturn(true);
        when(projectRepository.findProjectsByMemberId(1L)).thenReturn(List.of(project));
        when(projectMapper.toDtoList(List.of(project))).thenReturn(List.of(projectDTO));

        List<ProjectDTO> result = projectService.getProjectsByUser(1L);

        assertThat(result).hasSize(1);
        verify(projectMapper).toDtoList(List.of(project));
    }

    @Test
    void getProjectsByUser_shouldThrowException_whenUserNotFound() {
        when(userRepository.existsById(999L)).thenReturn(false);

        assertThatThrownBy(() -> projectService.getProjectsByUser(999L))
                .isInstanceOf(ResourceNotFoundException.class);

        verify(projectRepository, never()).findProjectsByMemberId(any());
    }
}