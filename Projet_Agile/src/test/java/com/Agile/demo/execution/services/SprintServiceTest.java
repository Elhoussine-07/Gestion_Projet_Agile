package com.Agile.demo.execution.services;

import com.Agile.demo.execution.repositories.SprintBacklogRepository;
import com.Agile.demo.execution.repositories.TaskRepository;
import com.Agile.demo.model.*;
import com.Agile.demo.planning.repository.ProjectRepository;
import com.Agile.demo.planning.repository.UserStoryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SprintServiceTest {

    @Mock
    private SprintBacklogRepository sprintBacklogRepository;

    @Mock
    private ProjectRepository projectRepository;

    @Mock
    private UserStoryRepository userStoryRepository;

    @Mock
    private TaskRepository taskRepository;

    @InjectMocks
    private SprintService sprintService;

    private Project testProject;
    private SprintBacklog testSprint;
    private UserStory testUserStory1;
    private UserStory testUserStory2;
    private ProductBacklog testProductBacklog;

    @BeforeEach
    void setUp() {
        testProject = new Project();
        testProject.setId(1L);
        testProject.setName("Test Project");

        testProductBacklog = new ProductBacklog();
        testProductBacklog.setProject(testProject);

        testSprint = new SprintBacklog("Sprint 1", 1,
                LocalDate.now(), LocalDate.now().plusDays(14), "Sprint Goal");
        testSprint.setId(1L);
        testSprint.setProject(testProject);

        testUserStory1 = new UserStory();
        testUserStory1.setId(1L);
        testUserStory1.setTitle("User Story 1");
        testUserStory1.setProductBacklog(testProductBacklog);
        testUserStory1.setStatus(WorkItemStatus.TODO);

        testUserStory2 = new UserStory();
        testUserStory2.setId(2L);
        testUserStory2.setTitle("User Story 2");
        testUserStory2.setProductBacklog(testProductBacklog);
        testUserStory2.setStatus(WorkItemStatus.TODO);
    }

    @Test
    void createSprint_WithValidData_ShouldCreateSprint() {
        Long projectId = 1L;
        Integer sprintNumber = 1;
        LocalDate startDate = LocalDate.now();
        LocalDate endDate = LocalDate.now().plusDays(14);
        String goal = "Sprint Goal";
        List<Long> userStoryIds = Arrays.asList(1L, 2L);

        when(projectRepository.findById(projectId)).thenReturn(Optional.of(testProject));
        when(sprintBacklogRepository.existsByProjectIdAndSprintNumber(projectId, sprintNumber))
                .thenReturn(false);
        when(sprintBacklogRepository.countByProjectIdAndSprintStatus(projectId, SprintStatus.ACTIVE))
                .thenReturn(0L);
        when(userStoryRepository.findAllById(userStoryIds))
                .thenReturn(Arrays.asList(testUserStory1, testUserStory2));
        when(sprintBacklogRepository.save(any(SprintBacklog.class))).thenReturn(testSprint);

        SprintBacklog result = sprintService.createSprint(projectId, sprintNumber,
                startDate, endDate, goal, userStoryIds);

        assertThat(result).isNotNull();
        verify(sprintBacklogRepository, times(1)).save(any(SprintBacklog.class));
    }

    @Test
    void createSprint_WithNonExistentProject_ShouldThrowException() {
        Long projectId = 999L;
        when(projectRepository.findById(projectId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> sprintService.createSprint(projectId, 1,
                LocalDate.now(), LocalDate.now().plusDays(14), "Goal", null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Projet non trouvé");
    }

    @Test
    void createSprint_WithDuplicateSprintNumber_ShouldThrowException() {
        Long projectId = 1L;
        Integer sprintNumber = 1;

        when(projectRepository.findById(projectId)).thenReturn(Optional.of(testProject));
        when(sprintBacklogRepository.existsByProjectIdAndSprintNumber(projectId, sprintNumber))
                .thenReturn(true);

        assertThatThrownBy(() -> sprintService.createSprint(projectId, sprintNumber,
                LocalDate.now(), LocalDate.now().plusDays(14), "Goal", null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("existe déjà");
    }

    @Test
    void createSprint_WithActiveSprintExists_ShouldThrowException() {
        Long projectId = 1L;
        Integer sprintNumber = 2;

        when(projectRepository.findById(projectId)).thenReturn(Optional.of(testProject));
        when(sprintBacklogRepository.existsByProjectIdAndSprintNumber(projectId, sprintNumber))
                .thenReturn(false);
        when(sprintBacklogRepository.countByProjectIdAndSprintStatus(projectId, SprintStatus.ACTIVE))
                .thenReturn(1L);

        assertThatThrownBy(() -> sprintService.createSprint(projectId, sprintNumber,
                LocalDate.now(), LocalDate.now().plusDays(14), "Goal", null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("déjà actif");
    }

    @Test
    void createSprint_WithEndDateBeforeStartDate_ShouldThrowException() {
        Long projectId = 1L;
        LocalDate startDate = LocalDate.now();
        LocalDate endDate = LocalDate.now().minusDays(1);

        when(projectRepository.findById(projectId)).thenReturn(Optional.of(testProject));
        when(sprintBacklogRepository.existsByProjectIdAndSprintNumber(anyLong(), anyInt()))
                .thenReturn(false);
        when(sprintBacklogRepository.countByProjectIdAndSprintStatus(anyLong(), any()))
                .thenReturn(0L);

        assertThatThrownBy(() -> sprintService.createSprint(projectId, 1,
                startDate, endDate, "Goal", null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("après la date de début");
    }

    @Test
    void getSprintsByProject_ShouldReturnAllSprints() {
        Long projectId = 1L;
        List<SprintBacklog> sprints = Arrays.asList(testSprint);
        when(sprintBacklogRepository.findByProjectId(projectId)).thenReturn(sprints);

        List<SprintBacklog> result = sprintService.getSprintsByProject(projectId);

        assertThat(result).hasSize(1);
        assertThat(result.get(0)).isEqualTo(testSprint);
        verify(sprintBacklogRepository, times(1)).findByProjectId(projectId);
    }

    @Test
    void getActiveSprint_WithActiveSprint_ShouldReturnSprint() {
        Long projectId = 1L;
        testSprint.setSprintStatus(SprintStatus.ACTIVE);
        when(sprintBacklogRepository.findByProjectIdAndSprintStatus(projectId, SprintStatus.ACTIVE))
                .thenReturn(Arrays.asList(testSprint));

        SprintBacklog result = sprintService.getActiveSprint(projectId);

        assertThat(result).isNotNull();
        assertThat(result.getSprintStatus()).isEqualTo(SprintStatus.ACTIVE);
    }

    @Test
    void getActiveSprint_WithNoActiveSprint_ShouldThrowException() {
        Long projectId = 1L;
        when(sprintBacklogRepository.findByProjectIdAndSprintStatus(projectId, SprintStatus.ACTIVE))
                .thenReturn(Collections.emptyList());

        assertThatThrownBy(() -> sprintService.getActiveSprint(projectId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Aucun sprint actif");
    }

    @Test
    void getSprintById_WithValidId_ShouldReturnSprint() {
        Long sprintId = 1L;
        when(sprintBacklogRepository.findById(sprintId)).thenReturn(Optional.of(testSprint));

        SprintBacklog result = sprintService.getSprintById(sprintId);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(sprintId);
    }

    @Test
    void getSprintById_WithInvalidId_ShouldThrowException() {
        Long sprintId = 999L;
        when(sprintBacklogRepository.findById(sprintId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> sprintService.getSprintById(sprintId))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Sprint non trouvé");
    }

    @Test
    void updateSprint_WithValidData_ShouldUpdateSprint() {
        Long sprintId = 1L;
        LocalDate newStartDate = LocalDate.now().plusDays(1);
        LocalDate newEndDate = LocalDate.now().plusDays(15);
        String newGoal = "Updated Goal";

        testSprint.setSprintStatus(SprintStatus.PLANNED);
        when(sprintBacklogRepository.findById(sprintId)).thenReturn(Optional.of(testSprint));
        when(sprintBacklogRepository.save(any(SprintBacklog.class))).thenReturn(testSprint);

        SprintBacklog result = sprintService.updateSprint(sprintId, newStartDate,
                newEndDate, newGoal);

        assertThat(result).isNotNull();
        verify(sprintBacklogRepository, times(1)).save(testSprint);
    }

    @Test
    void updateSprint_WithCompletedStatus_ShouldThrowException() {
        Long sprintId = 1L;
        testSprint.setSprintStatus(SprintStatus.COMPLETED);
        when(sprintBacklogRepository.findById(sprintId)).thenReturn(Optional.of(testSprint));

        assertThatThrownBy(() -> sprintService.updateSprint(sprintId,
                LocalDate.now(), LocalDate.now().plusDays(14), "Goal"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("terminé ou annulé");
    }

    @Test
    void startSprint_WithValidSprint_ShouldStartSprint() {
        Long sprintId = 1L;
        testSprint.setSprintStatus(SprintStatus.PLANNED);
        testSprint.addUserStory(testUserStory1);

        when(sprintBacklogRepository.findById(sprintId)).thenReturn(Optional.of(testSprint));
        when(sprintBacklogRepository.countByProjectIdAndSprintStatus(anyLong(), eq(SprintStatus.ACTIVE)))
                .thenReturn(0L);
        when(sprintBacklogRepository.save(any(SprintBacklog.class))).thenReturn(testSprint);

        SprintBacklog result = sprintService.startSprint(sprintId);

        assertThat(result).isNotNull();
        verify(sprintBacklogRepository, times(1)).save(testSprint);
    }

    @Test
    void startSprint_WithNoUserStories_ShouldThrowException() {
        Long sprintId = 1L;
        testSprint.setSprintStatus(SprintStatus.PLANNED);

        when(sprintBacklogRepository.findById(sprintId)).thenReturn(Optional.of(testSprint));
        when(sprintBacklogRepository.countByProjectIdAndSprintStatus(anyLong(), eq(SprintStatus.ACTIVE)))
                .thenReturn(0L);

        assertThatThrownBy(() -> sprintService.startSprint(sprintId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("au moins une User Story");
    }

    @Test
    void completeSprint_ShouldCompleteSprint() {
        Long sprintId = 1L;
        testSprint.setSprintStatus(SprintStatus.ACTIVE);
        when(sprintBacklogRepository.findById(sprintId)).thenReturn(Optional.of(testSprint));
        when(sprintBacklogRepository.save(any(SprintBacklog.class))).thenReturn(testSprint);

        SprintBacklog result = sprintService.completeSprint(sprintId);

        assertThat(result).isNotNull();
        verify(sprintBacklogRepository, times(1)).save(testSprint);
    }

    @Test
    void cancelSprint_ShouldCancelSprint() {
        Long sprintId = 1L;
        testSprint.setSprintStatus(SprintStatus.ACTIVE);
        when(sprintBacklogRepository.findById(sprintId)).thenReturn(Optional.of(testSprint));
        when(sprintBacklogRepository.save(any(SprintBacklog.class))).thenReturn(testSprint);

        SprintBacklog result = sprintService.cancelSprint(sprintId);

        assertThat(result).isNotNull();
        verify(sprintBacklogRepository, times(1)).save(testSprint);
    }

    @Test
    void deleteSprint_WithPlannedSprint_ShouldDeleteSprint() {
        Long sprintId = 1L;
        testSprint.setSprintStatus(SprintStatus.PLANNED);
        when(sprintBacklogRepository.findById(sprintId)).thenReturn(Optional.of(testSprint));

        sprintService.deleteSprint(sprintId);

        verify(sprintBacklogRepository, times(1)).delete(testSprint);
    }

    @Test
    void deleteSprint_WithActiveSprint_ShouldThrowException() {
        Long sprintId = 1L;
        testSprint.setSprintStatus(SprintStatus.ACTIVE);
        when(sprintBacklogRepository.findById(sprintId)).thenReturn(Optional.of(testSprint));

        assertThatThrownBy(() -> sprintService.deleteSprint(sprintId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("planifiés peuvent être supprimés");
    }

    @Test
    void addUserStoryToSprint_WithValidData_ShouldAddUserStory() {
        Long sprintId = 1L;
        Long userStoryId = 1L;
        testSprint.setSprintStatus(SprintStatus.PLANNED);

        when(sprintBacklogRepository.findById(sprintId)).thenReturn(Optional.of(testSprint));
        when(userStoryRepository.findById(userStoryId)).thenReturn(Optional.of(testUserStory1));
        when(sprintBacklogRepository.save(any(SprintBacklog.class))).thenReturn(testSprint);

        SprintBacklog result = sprintService.addUserStoryToSprint(sprintId, userStoryId);

        assertThat(result).isNotNull();
        verify(sprintBacklogRepository, times(1)).save(testSprint);
    }

    @Test
    void addUserStoryToSprint_WithCompletedSprint_ShouldThrowException() {
        Long sprintId = 1L;
        Long userStoryId = 1L;
        testSprint.setSprintStatus(SprintStatus.COMPLETED);

        when(sprintBacklogRepository.findById(sprintId)).thenReturn(Optional.of(testSprint));
        when(userStoryRepository.findById(userStoryId)).thenReturn(Optional.of(testUserStory1));

        assertThatThrownBy(() -> sprintService.addUserStoryToSprint(sprintId, userStoryId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("terminé ou annulé");
    }

    @Test
    void addMultipleUserStoriesToSprint_WithValidData_ShouldAddAllStories() {
        Long sprintId = 1L;
        List<Long> userStoryIds = Arrays.asList(1L, 2L);
        testSprint.setSprintStatus(SprintStatus.PLANNED);

        when(sprintBacklogRepository.findById(sprintId)).thenReturn(Optional.of(testSprint));
        when(userStoryRepository.findAllById(userStoryIds))
                .thenReturn(Arrays.asList(testUserStory1, testUserStory2));
        when(sprintBacklogRepository.save(any(SprintBacklog.class))).thenReturn(testSprint);

        SprintBacklog result = sprintService.addMultipleUserStoriesToSprint(sprintId, userStoryIds);

        assertThat(result).isNotNull();
        verify(sprintBacklogRepository, times(1)).save(testSprint);
    }

    @Test
    void addMultipleUserStoriesToSprint_WithEmptyList_ShouldThrowException() {
        Long sprintId = 1L;
        List<Long> emptyList = Collections.emptyList();

        when(sprintBacklogRepository.findById(sprintId)).thenReturn(Optional.of(testSprint));

        assertThatThrownBy(() -> sprintService.addMultipleUserStoriesToSprint(sprintId, emptyList))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("ne peut pas être vide");
    }

    @Test
    void removeUserStoryFromSprint_WithValidData_ShouldRemoveUserStory() {
        Long sprintId = 1L;
        Long userStoryId = 1L;

        when(sprintBacklogRepository.findById(sprintId)).thenReturn(Optional.of(testSprint));
        when(userStoryRepository.findById(userStoryId)).thenReturn(Optional.of(testUserStory1));
        when(sprintBacklogRepository.save(any(SprintBacklog.class))).thenReturn(testSprint);

        SprintBacklog result = sprintService.removeUserStoryFromSprint(sprintId, userStoryId);

        assertThat(result).isNotNull();
        verify(sprintBacklogRepository, times(1)).save(testSprint);
    }

    @Test
    void canStartSprint_WithValidConditions_ShouldReturnTrue() {
        Long sprintId = 1L;
        testSprint.setSprintStatus(SprintStatus.PLANNED);
        testSprint.addUserStory(testUserStory1);

        when(sprintBacklogRepository.findById(sprintId)).thenReturn(Optional.of(testSprint));
        when(sprintBacklogRepository.countByProjectIdAndSprintStatus(anyLong(), eq(SprintStatus.ACTIVE)))
                .thenReturn(0L);

        boolean result = sprintService.canStartSprint(sprintId);

        assertThat(result).isTrue();
    }

    @Test
    void canStartSprint_WithNoUserStories_ShouldReturnFalse() {
        Long sprintId = 1L;
        testSprint.setSprintStatus(SprintStatus.PLANNED);

        when(sprintBacklogRepository.findById(sprintId)).thenReturn(Optional.of(testSprint));

        boolean result = sprintService.canStartSprint(sprintId);

        assertThat(result).isFalse();
    }

    @Test
    void cloneSprint_WithValidData_ShouldCloneSprint() {
        Long sprintId = 1L;
        Integer newSprintNumber = 2;
        LocalDate newStartDate = LocalDate.now().plusDays(15);
        LocalDate newEndDate = LocalDate.now().plusDays(29);

        when(sprintBacklogRepository.findById(sprintId)).thenReturn(Optional.of(testSprint));
        when(sprintBacklogRepository.existsByProjectIdAndSprintNumber(anyLong(), eq(newSprintNumber)))
                .thenReturn(false);
        when(sprintBacklogRepository.save(any(SprintBacklog.class))).thenReturn(testSprint);

        SprintBacklog result = sprintService.cloneSprint(sprintId, newSprintNumber,
                newStartDate, newEndDate);

        assertThat(result).isNotNull();
        verify(sprintBacklogRepository, times(1)).save(any(SprintBacklog.class));
    }

    @Test
    void getSprintMetrics_ShouldReturnCompleteMetrics() {
        Long sprintId = 1L;
        testSprint.addUserStory(testUserStory1);
        testUserStory1.setStatus(WorkItemStatus.DONE);
        testUserStory1.setStoryPoints(5);

        when(sprintBacklogRepository.findById(sprintId)).thenReturn(Optional.of(testSprint));
        when(taskRepository.findBySprintBacklogId(Math.toIntExact(sprintId)))
                .thenReturn(Collections.emptyList());

        SprintService.SprintMetrics result = sprintService.getSprintMetrics(sprintId);

        assertThat(result).isNotNull();
        assertThat(result.velocity()).isEqualTo(5);
        assertThat(result.status()).isEqualTo(SprintStatus.PLANNED);
    }

    @Test
    void getSprintBurndown_ShouldReturnBurndownData() {
        Long sprintId = 1L;
        testUserStory1.setStoryPoints(5);
        testUserStory2.setStoryPoints(3);
        testUserStory1.setStatus(WorkItemStatus.DONE);
        testUserStory2.setStatus(WorkItemStatus.TODO);
        testSprint.addUserStory(testUserStory1);
        testSprint.addUserStory(testUserStory2);

        when(sprintBacklogRepository.findById(sprintId)).thenReturn(Optional.of(testSprint));

        SprintService.SprintBurndown result = sprintService.getSprintBurndown(sprintId);

        assertThat(result).isNotNull();
        assertThat(result.totalStoryPoints()).isEqualTo(8);
        assertThat(result.completedStoryPoints()).isEqualTo(5);
        assertThat(result.remainingStoryPoints()).isEqualTo(3);
    }
}