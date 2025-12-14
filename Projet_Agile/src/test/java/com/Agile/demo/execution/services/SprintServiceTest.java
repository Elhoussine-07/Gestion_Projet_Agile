package com.Agile.demo.execution.services;

import com.Agile.demo.model.*;
import com.Agile.demo.execution.repositories.SprintBacklogRepository;
import com.Agile.demo.execution.repositories.ProjectRepository;
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
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SprintServiceTest {

    @Mock
    private SprintBacklogRepository sprintBacklogRepository;

    @Mock
    private ProjectRepository projectRepository;

    @InjectMocks
    private SprintService sprintService;

    private Project project;
    private SprintBacklog sprint;
    private LocalDate startDate;
    private LocalDate endDate;

    @BeforeEach
    void setUp() {
        project = new Project();
        project.setId(1L);
        project.setName("Test Project");

        startDate = LocalDate.now();
        endDate = startDate.plusDays(14);

        sprint = new SprintBacklog("Sprint 1", 1, startDate, endDate, "Sprint Goal");
        sprint.setsprintNumber(1L);
        sprint.setProject(project);
    }

    @Test
    void createSprint_WhenValidData_ShouldCreateSprint() {
        // Arrange
        when(projectRepository.findById(1L)).thenReturn(Optional.of(project));
        when(sprintBacklogRepository.existsByProjectIdAndSprintNumber(1L, 1)).thenReturn(false);
        when(sprintBacklogRepository.countByProjectIdAndSprintStatus(1L, SprintStatus.ACTIVE)).thenReturn(0L);
        when(sprintBacklogRepository.save(any(SprintBacklog.class))).thenReturn(sprint);

        // Act
        SprintBacklog result = sprintService.createSprint(1L, 1, startDate, endDate, "Sprint Goal");

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getSprintNumber()).isEqualTo(1);
        assertThat(result.getGoal()).isEqualTo("Sprint Goal");
        verify(sprintBacklogRepository).save(any(SprintBacklog.class));
    }

    @Test
    void createSprint_WhenProjectNotFound_ShouldThrowException() {
        // Arrange
        when(projectRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> sprintService.createSprint(999L, 1, startDate, endDate, "Goal"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Projet non trouvé");
    }

    @Test
    void createSprint_WhenSprintNumberAlreadyExists_ShouldThrowException() {
        // Arrange
        when(projectRepository.findById(1L)).thenReturn(Optional.of(project));
        when(sprintBacklogRepository.existsByProjectIdAndSprintNumber(1L, 1)).thenReturn(true);

        // Act & Assert
        assertThatThrownBy(() -> sprintService.createSprint(1L, 1, startDate, endDate, "Goal"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("existe déjà");
    }

    @Test
    void createSprint_WhenActiveSprintExists_ShouldThrowException() {
        // Arrange
        when(projectRepository.findById(1L)).thenReturn(Optional.of(project));
        when(sprintBacklogRepository.existsByProjectIdAndSprintNumber(1L, 2)).thenReturn(false);
        when(sprintBacklogRepository.countByProjectIdAndSprintStatus(1L, SprintStatus.ACTIVE)).thenReturn(1L);

        // Act & Assert
        assertThatThrownBy(() -> sprintService.createSprint(1L, 2, startDate, endDate, "Goal"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("déjà actif");
    }

    @Test
    void createSprint_WhenEndDateBeforeStartDate_ShouldThrowException() {
        // Arrange
        when(projectRepository.findById(1L)).thenReturn(Optional.of(project));
        when(sprintBacklogRepository.existsByProjectIdAndSprintNumber(1L, 1)).thenReturn(false);
        when(sprintBacklogRepository.countByProjectIdAndSprintStatus(1L, SprintStatus.ACTIVE)).thenReturn(0L);

        // Act & Assert
        assertThatThrownBy(() -> sprintService.createSprint(1L, 1, endDate, startDate, "Goal"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("date de fin");
    }

    @Test
    void getSprintsByProject_ShouldReturnAllSprints() {
        // Arrange
        List<SprintBacklog> sprints = List.of(sprint);
        when(sprintBacklogRepository.findByProjectSprintNumber(1L)).thenReturn(sprints);

        // Act
        List<SprintBacklog> result = sprintService.getSprintsByProject(1L);

        // Assert
        assertThat(result).hasSize(1);
        assertThat(result.get(0)).isEqualTo(sprint);
    }

    @Test
    void getActiveSprint_WhenActiveSprintExists_ShouldReturnSprint() {
        // Arrange
        sprint.startSprint();
        when(sprintBacklogRepository.findByProjectIdAndSprintStatus(1L, SprintStatus.ACTIVE))
                .thenReturn(List.of(sprint));

        // Act
        SprintBacklog result = sprintService.getActiveSprint(1L);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getSprintStatus()).isEqualTo(SprintStatus.ACTIVE);
    }

    @Test
    void getActiveSprint_WhenNoActiveSprint_ShouldThrowException() {
        // Arrange
        when(sprintBacklogRepository.findByProjectIdAndSprintStatus(1L, SprintStatus.ACTIVE))
                .thenReturn(List.of());

        // Act & Assert
        assertThatThrownBy(() -> sprintService.getActiveSprint(1L))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Aucun sprint actif");
    }

    @Test
    void getSprintById_WhenSprintExists_ShouldReturnSprint() {
        // Arrange
        when(sprintBacklogRepository.findById(1L)).thenReturn(Optional.of(sprint));

        // Act
        SprintBacklog result = sprintService.getSprintById(1L);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getSprintNumber()).isEqualTo(1L);
    }

    @Test
    void getSprintById_WhenSprintNotFound_ShouldThrowException() {
        // Arrange
        when(sprintBacklogRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> sprintService.getSprintById(999L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Sprint non trouvé");
    }

    @Test
    void updateSprint_WhenValidData_ShouldUpdateSprint() {
        // Arrange
        LocalDate newEndDate = endDate.plusDays(7);
        when(sprintBacklogRepository.findById(1L)).thenReturn(Optional.of(sprint));
        when(sprintBacklogRepository.save(any(SprintBacklog.class))).thenReturn(sprint);

        // Act
        SprintBacklog result = sprintService.updateSprint(1L, startDate, newEndDate, "New Goal");

        // Assert
        assertThat(result).isNotNull();
        verify(sprintBacklogRepository).save(sprint);
    }

    @Test
    void updateSprint_WhenSprintCompleted_ShouldThrowException() {
        SprintBacklog sprint = new SprintBacklog(
                "Sprint 1",
                1,
                LocalDate.now(),
                LocalDate.now().plusDays(14),
                "Goal"
        );

        sprint.setSprintStatus(SprintStatus.COMPLETED); // 🔥 OBLIGATOIRE

        when(sprintBacklogRepository.findById(1L))
                .thenReturn(Optional.of(sprint));

        assertThrows(IllegalStateException.class, () ->
                sprintService.updateSprint(
                        1L,
                        LocalDate.now(),
                        LocalDate.now().plusDays(10),
                        "New goal"
                )
        );
    }


    @Test
    void startSprint_WhenValidSprint_ShouldStartSprint() {
        // Arrange
        when(sprintBacklogRepository.findById(1L)).thenReturn(Optional.of(sprint));
        when(sprintBacklogRepository.countByProjectIdAndSprintStatus(1L, SprintStatus.ACTIVE)).thenReturn(0L);
        when(sprintBacklogRepository.save(any(SprintBacklog.class))).thenReturn(sprint);

        // Act
        SprintBacklog result = sprintService.startSprint(1L);

        // Assert
        assertThat(result.getSprintStatus()).isEqualTo(SprintStatus.ACTIVE);
        verify(sprintBacklogRepository).save(sprint);
    }

    @Test
    void completeSprint_WhenValidSprint_ShouldCompleteSprint() {
        // Arrange
        sprint.startSprint();
        when(sprintBacklogRepository.findById(1L)).thenReturn(Optional.of(sprint));
        when(sprintBacklogRepository.save(any(SprintBacklog.class))).thenReturn(sprint);

        // Act
        SprintBacklog result = sprintService.completeSprint(1L);

        // Assert
        assertThat(result.getSprintStatus()).isEqualTo(SprintStatus.COMPLETED);
        verify(sprintBacklogRepository).save(sprint);
    }

    @Test
    void cancelSprint_WhenValidSprint_ShouldCancelSprint() {
        // Arrange
        when(sprintBacklogRepository.findById(1L)).thenReturn(Optional.of(sprint));
        when(sprintBacklogRepository.save(any(SprintBacklog.class))).thenReturn(sprint);

        // Act
        SprintBacklog result = sprintService.cancelSprint(1L);

        // Assert
        assertThat(result.getSprintStatus()).isEqualTo(SprintStatus.CANCELLED);
        verify(sprintBacklogRepository).save(sprint);
    }

    @Test
    void deleteSprint_WhenSprintPlanned_ShouldDeleteSprint() {
        // Arrange
        when(sprintBacklogRepository.findById(1L)).thenReturn(Optional.of(sprint));

        // Act
        sprintService.deleteSprint(1L);

        // Assert
        verify(sprintBacklogRepository).delete(sprint);
    }

    @Test
    void deleteSprint_WhenSprintActive_ShouldThrowException() {
        // Arrange
        sprint.startSprint();
        when(sprintBacklogRepository.findById(1L)).thenReturn(Optional.of(sprint));

        // Act & Assert
        assertThatThrownBy(() -> sprintService.deleteSprint(1L))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("planifiés");
    }

    @Test
    void addUserStoryToSprint_WhenValidSprint_ShouldAddUserStory() {
        // Arrange
        UserStory userStory = new UserStory();
        userStory.setId(1L);
        when(sprintBacklogRepository.findById(1L)).thenReturn(Optional.of(sprint));
        when(sprintBacklogRepository.save(any(SprintBacklog.class))).thenReturn(sprint);

        // Act
        SprintBacklog result = sprintService.addUserStoryToSprint(1L, userStory);

        // Assert
        assertThat(result).isNotNull();
        verify(sprintBacklogRepository).save(sprint);
    }

    @Test
    void removeUserStoryFromSprint_WhenValidSprint_ShouldRemoveUserStory() {
        // Arrange
        UserStory userStory = new UserStory();
        userStory.setId(1L);
        when(sprintBacklogRepository.findById(1L)).thenReturn(Optional.of(sprint));
        when(sprintBacklogRepository.save(any(SprintBacklog.class))).thenReturn(sprint);

        // Act
        SprintBacklog result = sprintService.removeUserStoryFromSprint(1L, userStory);

        // Assert
        assertThat(result).isNotNull();
        verify(sprintBacklogRepository).save(sprint);
    }

    @Test
    void getSprintMetrics_ShouldReturnMetrics() {
        // Arrange
        when(sprintBacklogRepository.findById(1L)).thenReturn(Optional.of(sprint));

        // Act
        SprintService.SprintMetrics result = sprintService.getSprintMetrics(1L);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.durationInDays()).isGreaterThan(0);
    }
}