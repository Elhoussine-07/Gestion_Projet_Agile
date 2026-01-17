package com.Agile.demo.planning.service;

import com.Agile.demo.model.Project;
import com.Agile.demo.planning.repository.ProjectRepository;
import com.Agile.demo.common.exception.ResourceNotFoundException;
import com.Agile.demo.common.exception.BusinessException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import java.time.LocalDate;
import java.util.Optional;
import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProjectServiceTest {

    @Mock
    private ProjectRepository projectRepository;

    @InjectMocks
    private ProjectService projectService;

    private Project project;

    @BeforeEach
    void setUp() {
        project = Project.builder()
                .name("Test Project")
                .description("Description")
                .startDate(LocalDate.now())
                .endDate(LocalDate.now().plusMonths(3))
                .build();

        project.setId(1L);

    }

    @Test
    void shouldCreateProject() {
        // Arrange
        when(projectRepository.existsByName("Test Project")).thenReturn(false);
        when(projectRepository.save(any(Project.class))).thenReturn(project);

        // Act
        Project created = projectService.createProject("Test Project", "Description",
                LocalDate.now(), LocalDate.now().plusMonths(3));

        // Assert
        assertThat(created).isNotNull();
        assertThat(created.getName()).isEqualTo("Test Project");
        verify(projectRepository).save(any(Project.class));
    }

    @Test
    void shouldThrowExceptionWhenProjectNameExists() {
        // Arrange
        when(projectRepository.existsByName("Test Project")).thenReturn(true);

        // Act & Assert
        assertThatThrownBy(() ->
                projectService.createProject("Test Project", "Description",
                        LocalDate.now(), LocalDate.now().plusMonths(3))
        ).isInstanceOf(BusinessException.class)
                .hasMessageContaining("already exists");

        verify(projectRepository, never()).save(any());
    }

    @Test
    void shouldThrowExceptionWhenEndDateBeforeStartDate() {
        // Act & Assert
        assertThatThrownBy(() ->
                projectService.createProject("Test", "Desc",
                        LocalDate.now(), LocalDate.now().minusDays(1))
        ).isInstanceOf(BusinessException.class)
                .hasMessageContaining("End date must be after start date");
    }

    @Test
    void shouldGetProjectById() {
        // Arrange
        when(projectRepository.findById(1L)).thenReturn(Optional.of(project));

        // Act
        Project found = projectService.getProjectById(1L);

        // Assert
        assertThat(found).isNotNull();
        assertThat(found.getId()).isEqualTo(1L);
    }

    @Test
    void shouldThrowExceptionWhenProjectNotFound() {
        // Arrange
        when(projectRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> projectService.getProjectById(999L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void shouldGetAllProjects() {
        // Arrange
        when(projectRepository.findAll()).thenReturn(List.of(project));

        // Act
        List<Project> projects = projectService.getAllProjects();

        // Assert
        assertThat(projects).hasSize(1);
        assertThat(projects.get(0).getName()).isEqualTo("Test Project");
    }

    @Test
    void shouldDeleteProject() {
        // Arrange
        when(projectRepository.findById(1L)).thenReturn(Optional.of(project));

        // Act
        projectService.deleteProject(1L);

        // Assert
        verify(projectRepository).delete(project);
    }
}