package com.Agile.demo.execution.services;

import com.Agile.demo.execution.dto.SprintBacklogResponseDTO;
import com.Agile.demo.execution.dto.SprintCreateRequest;
import com.Agile.demo.execution.dto.mapper.SprintMapper;
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
import java.util.ArrayList;
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
    @Mock
    private SprintMapper sprintMapper;

    @InjectMocks
    private SprintService sprintService;

    // Entités de test
    private Project testProject;
    private ProductBacklog testProductBacklog;
    private SprintBacklog testSprint;
    private UserStory testUserStory1;
    private UserStory testUserStory2;
    private SprintBacklogResponseDTO testSprintResponseDTO;

    @BeforeEach
    void setUp() {
        // 1. Setup du Project
        testProject = Project.builder()
                .id(1L)
                .name("Agile Project")
                .build();

        // 2. Setup du ProductBacklog
        testProductBacklog = new ProductBacklog("Main Backlog");
        testProductBacklog.setId(1L);
        testProductBacklog.setProject(testProject);

        // 3. Setup des UserStories
        // Note: On suppose que UserStory a aussi un Builder ou des setters
        testUserStory1 = UserStory.builder()
                .id(1L)
                .title("Story 1")
                .status(WorkItemStatus.TODO)
                .storyPoints(5)
                .productBacklog(testProductBacklog)
                .dependencies(new ArrayList<>()) // Important pour éviter NPE
                .tasks(new ArrayList<>())
                .build();

        testUserStory2 = UserStory.builder()
                .id(2L)
                .title("Story 2")
                .status(WorkItemStatus.TODO)
                .storyPoints(3)
                .productBacklog(testProductBacklog)
                .dependencies(new ArrayList<>())
                .tasks(new ArrayList<>())
                .build();

        // 4. Setup du SprintBacklog (COMPATIBILITÉ ENTITÉ)
        // Important : Avec @Builder, les listes par défaut sont nulles.
        // Il faut les initialiser explicitement ici pour que 'addUserStory' fonctionne.
        testSprint = SprintBacklog.builder()
                .id(1L)
                .sprintNumber(1)
                .startDate(LocalDate.now())
                .endDate(LocalDate.now().plusDays(14))
                .goal("Initial Goal")
                .sprintStatus(SprintStatus.PLANNED)
                .project(testProject)
                .userStories(new ArrayList<>()) // Initialisation explicite
                .tasks(new ArrayList<>())       // Initialisation explicite
                .build();

        // 5. Setup du DTO de réponse (Simulé)
        testSprintResponseDTO = new SprintBacklogResponseDTO();
        testSprintResponseDTO.setId(1L);
        testSprintResponseDTO.setProjectId(1L);
        testSprintResponseDTO.setSprintNumber(1);
        testSprintResponseDTO.setStatus(SprintStatus.PLANNED);
    }

    // --- TESTS CRÉATION ---

    @Test
    void createSprint_ShouldPersistAndReturnDto() {
        // Arrange
        SprintCreateRequest request = new SprintCreateRequest();
        request.setProjectId(1L);
        request.setSprintNumber(1);
        request.setStartDate(LocalDate.now());
        request.setEndDate(LocalDate.now().plusDays(14));
        request.setUserStoryIds(List.of(1L));

        when(projectRepository.findById(1L)).thenReturn(Optional.of(testProject));
        when(sprintBacklogRepository.existsByProjectIdAndSprintNumber(1L, 1)).thenReturn(false);
        when(sprintBacklogRepository.countByProjectIdAndSprintStatus(1L, SprintStatus.ACTIVE)).thenReturn(0L);

        when(sprintMapper.toEntity(request)).thenReturn(testSprint);
        when(userStoryRepository.findAllById(anyList())).thenReturn(List.of(testUserStory1));
        when(sprintBacklogRepository.save(any(SprintBacklog.class))).thenReturn(testSprint);
        when(sprintMapper.toDto(testSprint)).thenReturn(testSprintResponseDTO);

        // Act
        SprintBacklogResponseDTO result = sprintService.createSprint(request);

        // Assert
        assertThat(result).isNotNull();
        verify(sprintBacklogRepository).save(testSprint);
        // Vérifie que la méthode utilitaire addUserStory de l'entité a bien fonctionné
        assertThat(testSprint.getUserStories()).contains(testUserStory1);
    }

    // --- TESTS LOGIQUE MÉTIER (Start/Complete) ---

    @Test
    void startSprint_ShouldChangeStatusToActive() {
        // Arrange
        testSprint.setSprintStatus(SprintStatus.PLANNED);
        testSprint.addUserStory(testUserStory1); // Doit avoir au moins une story

        when(sprintBacklogRepository.findById(1L)).thenReturn(Optional.of(testSprint));
        when(sprintBacklogRepository.countByProjectIdAndSprintStatus(anyLong(), eq(SprintStatus.ACTIVE))).thenReturn(0L);
        when(sprintBacklogRepository.save(testSprint)).thenReturn(testSprint);

        // On configure le mapper pour renvoyer un DTO actif pour la vérification
        testSprintResponseDTO.setStatus(SprintStatus.ACTIVE);
        when(sprintMapper.toDto(testSprint)).thenReturn(testSprintResponseDTO);

        // Act
        SprintBacklogResponseDTO result = sprintService.startSprint(1L);

        // Assert
        assertThat(result.getStatus()).isEqualTo(SprintStatus.ACTIVE);
        // Vérifie que la méthode startSprint() de l'entité a été appelée
        assertThat(testSprint.getSprintStatus()).isEqualTo(SprintStatus.ACTIVE);
    }

    @Test
    void startSprint_WithUnmetDependencies_ShouldThrowException() {
        // Arrange : Story 1 dépend de Story 2, et Story 2 n'est pas DONE
        UserStory blockedStory = testUserStory1;
        UserStory dependency = testUserStory2;
        dependency.setStatus(WorkItemStatus.TODO);

        blockedStory.getDependencies().add(dependency);
        testSprint.addUserStory(blockedStory);

        when(sprintBacklogRepository.findById(1L)).thenReturn(Optional.of(testSprint));
        when(sprintBacklogRepository.countByProjectIdAndSprintStatus(anyLong(), eq(SprintStatus.ACTIVE))).thenReturn(0L);

        // Act & Assert
        assertThatThrownBy(() -> sprintService.startSprint(1L))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("dépendances non satisfaites");
    }

    @Test
    void completeSprint_ShouldCalculateMetricsAndClose() {
        // Arrange
        testSprint.setSprintStatus(SprintStatus.ACTIVE);
        testSprint.addUserStory(testUserStory1);

        when(sprintBacklogRepository.findById(1L)).thenReturn(Optional.of(testSprint));
        when(sprintBacklogRepository.save(testSprint)).thenReturn(testSprint);

        testSprintResponseDTO.setStatus(SprintStatus.COMPLETED);
        when(sprintMapper.toDto(testSprint)).thenReturn(testSprintResponseDTO);

        // Act
        sprintService.completeSprint(1L);

        // Assert
        // Vérifie que la méthode completeSprint() de l'entité a été appelée
        assertThat(testSprint.getSprintStatus()).isEqualTo(SprintStatus.COMPLETED);
    }

    // --- TESTS GESTION DES STORIES ---

    @Test
    void moveUserStoryBetweenSprints_ShouldUpdateRelationships() {
        // Arrange
        SprintBacklog targetSprint = SprintBacklog.builder()
                .id(2L)
                .sprintStatus(SprintStatus.PLANNED)
                .project(testProject)
                .userStories(new ArrayList<>()) // Liste vide mutable
                .build();

        // La story est actuellement dans le Sprint 1
        testSprint.addUserStory(testUserStory1);

        when(sprintBacklogRepository.findById(1L)).thenReturn(Optional.of(testSprint));
        when(sprintBacklogRepository.findById(2L)).thenReturn(Optional.of(targetSprint));

        // Act
        sprintService.moveUserStoryBetweenSprints(1L, 2L, 1L);

        // Assert
        // Vérifie l'utilisation de removeUserStory de l'entité SprintBacklog
        assertThat(testSprint.getUserStories()).doesNotContain(testUserStory1);
        // Vérifie l'utilisation de addUserStory de l'entité SprintBacklog
        assertThat(targetSprint.getUserStories()).contains(testUserStory1);

        verify(sprintBacklogRepository).save(testSprint);
        verify(sprintBacklogRepository).save(targetSprint);
    }

    @Test
    void removeUserStory_ShouldCheckInProgressTasks() {
        // Arrange
        testSprint.addUserStory(testUserStory1);

        // Ajout d'une tâche en cours (nécessite Task entity)
        Task activeTask = new Task();
        activeTask.setStatus(WorkItemStatus.IN_PROGRESS);
        testUserStory1.getTasks().add(activeTask);

        when(sprintBacklogRepository.findById(1L)).thenReturn(Optional.of(testSprint));
        when(userStoryRepository.findById(1L)).thenReturn(Optional.of(testUserStory1));

        // Act & Assert
        assertThatThrownBy(() -> sprintService.removeUserStoryFromSprint(1L, 1L))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("tâche(s) en cours");
    }

    // --- TESTS MÉTRIQUES (Utilisation des méthodes de l'entité) ---

    @Test
    void getSprintMetrics_ShouldUseEntityCalculationMethods() {
        // Arrange
        // Story complétée (5 points)
        testUserStory1.setStatus(WorkItemStatus.DONE);
        testUserStory1.setStoryPoints(5);
        testSprint.addUserStory(testUserStory1);

        // Story non complétée (3 points)
        testUserStory2.setStatus(WorkItemStatus.TODO);
        testUserStory2.setStoryPoints(3);
        testSprint.addUserStory(testUserStory2);

        when(sprintBacklogRepository.findById(1L)).thenReturn(Optional.of(testSprint));
        when(taskRepository.findBySprintBacklogId(anyInt())).thenReturn(Collections.emptyList());

        // Act
        SprintService.SprintMetrics metrics = sprintService.getSprintMetrics(1L);

        // Assert
        // Vérifie calculateVelocity() de l'entité
        assertThat(metrics.velocity()).isEqualTo(5);
        // Vérifie getTotalStoryPoints() de l'entité (5 + 3)
        assertThat(metrics.totalStoryPoints()).isEqualTo(8);
        // Vérifie getRemainingStoryPoints() de l'entité (3)
        assertThat(metrics.remainingStoryPoints()).isEqualTo(3);
        // Vérifie calculateProgress() de l'entité (1 sur 2 stories = 50%)
        assertThat(metrics.progressPercentage()).isEqualTo(50.0);
    }
}