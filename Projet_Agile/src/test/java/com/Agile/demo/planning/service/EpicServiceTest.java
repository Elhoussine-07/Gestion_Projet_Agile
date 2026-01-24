package com.Agile.demo.planning.service;

import com.Agile.demo.exception.BusinessException;
import com.Agile.demo.exception.ResourceNotFoundException;
import com.Agile.demo.model.Epic;
import com.Agile.demo.model.ProductBacklog;
import com.Agile.demo.model.UserStory;
import com.Agile.demo.model.WorkItemStatus;
import com.Agile.demo.planning.dto.epic.CreateEpicDTO;
import com.Agile.demo.planning.dto.epic.EpicDTO;
import com.Agile.demo.planning.dto.epic.UpdateEpicDTO;
import com.Agile.demo.planning.mapper.EpicMapper;
import com.Agile.demo.planning.repository.EpicRepository;
import com.Agile.demo.planning.repository.ProductBacklogRepository;
import com.Agile.demo.planning.repository.UserStoryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("EpicService - Tests unitaires")
class EpicServiceTest {

    @Mock
    private EpicRepository epicRepository;

    @Mock
    private ProductBacklogRepository productBacklogRepository;

    @Mock
    private UserStoryRepository userStoryRepository;

    @Mock
    private EpicMapper epicMapper;

    @InjectMocks
    private EpicService epicService;

    private Epic epic;
    private ProductBacklog backlog;
    private UserStory story;
    private CreateEpicDTO createDto;
    private UpdateEpicDTO updateDto;
    private EpicDTO epicDto;

    @BeforeEach
    void setUp() {
        // ProductBacklog
        backlog = new ProductBacklog();
        backlog.setId(1L);

        // Epic Entity
        epic = new Epic();
        epic.setId(1L);
        epic.setTitle("Epic 1");
        epic.setDescription("Description");
        epic.setProductBacklog(backlog);
        epic.setUserStories(new ArrayList<>());

        // UserStory
        story = new UserStory();
        story.setId(1L);
        story.setStatus(WorkItemStatus.TODO);

        // CreateEpicDTO
        createDto = new CreateEpicDTO();
        createDto.setProductBacklogId(1L);
        createDto.setTitle("Epic 1");
        createDto.setDescription("Description");

        // UpdateEpicDTO
        updateDto = new UpdateEpicDTO();
        updateDto.setTitle("Epic Updated");
        updateDto.setDescription("New Description");

        // EpicDTO
        epicDto = EpicDTO.builder()
                .id(1L)
                .title("Epic 1")
                .description("Description")
                .productBacklogId(1L)
                .userStoryCount(0)
                .completedStoryCount(0)
                .progress(0)
                .build();
    }

    // ========================================
    // Tests - CREATE
    // ========================================

    @Test
    @DisplayName("Devrait créer un epic avec succès")
    void shouldCreateEpic() {
        // Given
        when(productBacklogRepository.findById(1L)).thenReturn(Optional.of(backlog));
        when(epicRepository.existsByTitleAndProductBacklogId("Epic 1", 1L)).thenReturn(false);
        when(epicMapper.toEntity(createDto)).thenReturn(epic);
        when(epicRepository.save(any(Epic.class))).thenReturn(epic);
        when(epicMapper.toDto(epic)).thenReturn(epicDto);

        // When
        EpicDTO result = epicService.createEpic(createDto);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getTitle()).isEqualTo("Epic 1");
        assertThat(result.getProductBacklogId()).isEqualTo(1L);

        verify(productBacklogRepository).findById(1L);
        verify(epicRepository).existsByTitleAndProductBacklogId("Epic 1", 1L);
        verify(epicMapper).toEntity(createDto);
        verify(epicRepository).save(any(Epic.class));
        verify(epicMapper).toDto(epic);
    }

    @Test
    @DisplayName("Devrait lancer une exception si le titre de l'epic existe déjà")
    void shouldThrowExceptionWhenEpicTitleExists() {
        // Given
        when(productBacklogRepository.findById(1L)).thenReturn(Optional.of(backlog));
        when(epicRepository.existsByTitleAndProductBacklogId("Epic 1", 1L)).thenReturn(true);

        // When & Then
        assertThatThrownBy(() -> epicService.createEpic(createDto))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("already exists");

        verify(epicRepository, never()).save(any());
        verify(epicMapper, never()).toEntity(any());
    }

    @Test
    @DisplayName("Devrait lancer une exception si le backlog n'existe pas")
    void shouldThrowExceptionWhenBacklogNotFound() {
        // Given
        createDto.setProductBacklogId(999L);
        when(productBacklogRepository.findById(999L)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> epicService.createEpic(createDto))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("ProductBacklog");

        verify(epicRepository, never()).save(any());
    }

    // ========================================
    // Tests - READ
    // ========================================

    @Test
    @DisplayName("Devrait récupérer un epic par son ID")
    void shouldGetEpicById() {
        // Given
        when(epicRepository.findById(1L)).thenReturn(Optional.of(epic));
        when(epicMapper.toDto(epic)).thenReturn(epicDto);

        // When
        EpicDTO result = epicService.getEpicById(1L);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getTitle()).isEqualTo("Epic 1");

        verify(epicRepository).findById(1L);
        verify(epicMapper).toDto(epic);
    }

    @Test
    @DisplayName("Devrait lancer une exception si l'epic n'existe pas")
    void shouldThrowExceptionWhenEpicNotFound() {
        // Given
        when(epicRepository.findById(999L)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> epicService.getEpicById(999L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Epic");

        verify(epicMapper, never()).toDto(any());
    }

    @Test
    @DisplayName("Devrait récupérer tous les epics d'un backlog")
    void shouldGetEpicsByProductBacklog() {
        // Given
        List<Epic> epics = List.of(epic);
        List<EpicDTO> epicDtos = List.of(epicDto);

        when(epicRepository.findByProductBacklogId(1L)).thenReturn(epics);
        when(epicMapper.toDtoList(epics)).thenReturn(epicDtos);

        // When
        List<EpicDTO> result = epicService.getEpicsByProductBacklog(1L);

        // Then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getTitle()).isEqualTo("Epic 1");

        verify(epicRepository).findByProductBacklogId(1L);
        verify(epicMapper).toDtoList(epics);
    }

    @Test
    @DisplayName("Devrait récupérer tous les epics")
    void shouldGetAllEpics() {
        // Given
        List<Epic> epics = List.of(epic);
        List<EpicDTO> epicDtos = List.of(epicDto);

        when(epicRepository.findAll()).thenReturn(epics);
        when(epicMapper.toDtoList(epics)).thenReturn(epicDtos);

        // When
        List<EpicDTO> result = epicService.getAllEpics();

        // Then
        assertThat(result).hasSize(1);
        verify(epicRepository).findAll();
        verify(epicMapper).toDtoList(epics);
    }

    // ========================================
    // Tests - UPDATE
    // ========================================

    @Test
    @DisplayName("Devrait mettre à jour un epic")
    void shouldUpdateEpic() {
        // Given
        when(epicRepository.findById(1L)).thenReturn(Optional.of(epic));
        when(epicRepository.existsByTitleAndProductBacklogId("Epic Updated", 1L)).thenReturn(false);
        doNothing().when(epicMapper).updateEntityFromDto(updateDto, epic);
        when(epicRepository.save(epic)).thenReturn(epic);
        when(epicMapper.toDto(epic)).thenReturn(epicDto);

        // When
        EpicDTO result = epicService.updateEpic(1L, updateDto);

        // Then
        assertThat(result).isNotNull();
        verify(epicMapper).updateEntityFromDto(updateDto, epic);
        verify(epicRepository).save(epic);
    }

    @Test
    @DisplayName("Devrait lancer une exception si le nouveau titre existe déjà")
    void shouldThrowExceptionWhenUpdatingWithExistingTitle() {
        // Given
        when(epicRepository.findById(1L)).thenReturn(Optional.of(epic));
        when(epicRepository.existsByTitleAndProductBacklogId("Epic Updated", 1L)).thenReturn(true);

        // When & Then
        assertThatThrownBy(() -> epicService.updateEpic(1L, updateDto))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("already exists");

        verify(epicMapper, never()).updateEntityFromDto(any(), any());
        verify(epicRepository, never()).save(any());
    }

    @Test
    @DisplayName("Ne devrait pas lancer d'exception si le titre ne change pas")
    void shouldNotThrowExceptionWhenTitleUnchanged() {
        // Given
        updateDto.setTitle("Epic 1"); // Même titre
        when(epicRepository.findById(1L)).thenReturn(Optional.of(epic));
        doNothing().when(epicMapper).updateEntityFromDto(updateDto, epic);
        when(epicRepository.save(epic)).thenReturn(epic);
        when(epicMapper.toDto(epic)).thenReturn(epicDto);

        // When
        EpicDTO result = epicService.updateEpic(1L, updateDto);

        // Then
        assertThat(result).isNotNull();
        verify(epicRepository, never()).existsByTitleAndProductBacklogId(anyString(), anyLong());
    }

    // ========================================
    // Tests - DELETE
    // ========================================

    @Test
    @DisplayName("Devrait supprimer un epic et dissocier les user stories")
    void shouldDeleteEpic() {
        // Given
        story.setEpic(epic);
        epic.setUserStories(new ArrayList<>(List.of(story)));
        when(epicRepository.findById(1L)).thenReturn(Optional.of(epic));

        // When
        epicService.deleteEpic(1L);

        // Then
        assertThat(story.getEpic()).isNull();
        verify(userStoryRepository).save(story);
        verify(epicRepository).delete(epic);
    }

    @Test
    @DisplayName("Devrait supprimer un epic sans user stories")
    void shouldDeleteEpicWithoutUserStories() {
        // Given
        when(epicRepository.findById(1L)).thenReturn(Optional.of(epic));

        // When
        epicService.deleteEpic(1L);

        // Then
        verify(epicRepository).delete(epic);
        verify(userStoryRepository, never()).save(any());
    }

    // ========================================
    // Tests - ASSOCIATIONS
    // ========================================

    @Test
    @DisplayName("Devrait ajouter une user story à un epic")
    void shouldAddUserStoryToEpic() {
        // Given
        when(epicRepository.findById(1L)).thenReturn(Optional.of(epic));
        when(userStoryRepository.findById(1L)).thenReturn(Optional.of(story));
        when(userStoryRepository.save(story)).thenReturn(story);

        // When
        epicService.addUserStoryToEpic(1L, 1L);

        // Then
        assertThat(story.getEpic()).isEqualTo(epic);
        verify(userStoryRepository).save(story);
    }

    @Test
    @DisplayName("Devrait lancer une exception si la user story est déjà assignée")
    void shouldThrowExceptionWhenUserStoryAlreadyAssigned() {
        // Given
        story.setEpic(epic);
        when(epicRepository.findById(1L)).thenReturn(Optional.of(epic));
        when(userStoryRepository.findById(1L)).thenReturn(Optional.of(story));

        // When & Then
        assertThatThrownBy(() -> epicService.addUserStoryToEpic(1L, 1L))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("already assigned");

        verify(userStoryRepository, never()).save(any());
    }

    @Test
    @DisplayName("Devrait retirer une user story d'un epic")
    void shouldRemoveUserStoryFromEpic() {
        // Given
        story.setEpic(epic);
        when(epicRepository.findById(1L)).thenReturn(Optional.of(epic));
        when(userStoryRepository.findById(1L)).thenReturn(Optional.of(story));
        when(userStoryRepository.save(story)).thenReturn(story);

        // When
        epicService.removeUserStoryFromEpic(1L, 1L);

        // Then
        assertThat(story.getEpic()).isNull();
        verify(userStoryRepository).save(story);
    }

    @Test
    @DisplayName("Devrait lancer une exception si la user story n'est pas dans cet epic")
    void shouldThrowExceptionWhenUserStoryNotInEpic() {
        // Given
        when(epicRepository.findById(1L)).thenReturn(Optional.of(epic));
        when(userStoryRepository.findById(1L)).thenReturn(Optional.of(story));

        // When & Then
        assertThatThrownBy(() -> epicService.removeUserStoryFromEpic(1L, 1L))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("not assigned");

        verify(userStoryRepository, never()).save(any());
    }

    // ========================================
    // Tests - CALCULS
    // ========================================

    @Test
    @DisplayName("Devrait calculer la progression d'un epic")
    void shouldCalculateEpicProgress() {
        // Given
        UserStory story1 = new UserStory();
        story1.setStatus(WorkItemStatus.DONE);

        UserStory story2 = new UserStory();
        story2.setStatus(WorkItemStatus.TODO);

        epic.setUserStories(List.of(story1, story2));

        when(epicRepository.findById(1L)).thenReturn(Optional.of(epic));
        when(epicMapper.calculateProgress(epic)).thenReturn(50);

        // When
        int progress = epicService.calculateEpicProgress(1L);

        // Then
        assertThat(progress).isEqualTo(50);
        verify(epicMapper).calculateProgress(epic);
    }

    @Test
    @DisplayName("Devrait retourner 0% si l'epic n'a pas de user stories")
    void shouldReturnZeroProgressWhenNoUserStories() {
        // Given
        when(epicRepository.findById(1L)).thenReturn(Optional.of(epic));
        when(epicMapper.calculateProgress(epic)).thenReturn(0);

        // When
        int progress = epicService.calculateEpicProgress(1L);

        // Then
        assertThat(progress).isEqualTo(0);
    }

    @Test
    @DisplayName("Devrait retourner 100% si toutes les user stories sont DONE")
    void shouldReturnHundredPercentWhenAllStoriesDone() {
        // Given
        UserStory story1 = new UserStory();
        story1.setStatus(WorkItemStatus.DONE);

        UserStory story2 = new UserStory();
        story2.setStatus(WorkItemStatus.DONE);

        epic.setUserStories(List.of(story1, story2));

        when(epicRepository.findById(1L)).thenReturn(Optional.of(epic));
        when(epicMapper.calculateProgress(epic)).thenReturn(100);

        // When
        int progress = epicService.calculateEpicProgress(1L);

        // Then
        assertThat(progress).isEqualTo(100);
    }
}