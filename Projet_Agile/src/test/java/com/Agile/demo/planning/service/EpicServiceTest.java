package com.Agile.demo.planning.service;

import com.Agile.demo.common.exception.BusinessException;
import com.Agile.demo.common.exception.ResourceNotFoundException;
import com.Agile.demo.model.Epic;
import com.Agile.demo.model.ProductBacklog;
import com.Agile.demo.model.UserStory;
import com.Agile.demo.planning.repository.EpicRepository;
import com.Agile.demo.planning.repository.ProductBacklogRepository;
import com.Agile.demo.planning.repository.UserStoryRepository;
import org.junit.jupiter.api.BeforeEach;
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
class EpicServiceTest {

    @Mock
    private EpicRepository epicRepository;

    @Mock
    private ProductBacklogRepository productBacklogRepository;

    @Mock
    private UserStoryRepository userStoryRepository;

    @InjectMocks
    private EpicService epicService;

    private Epic epic;
    private ProductBacklog backlog;
    private UserStory story;

    @BeforeEach
    void setUp() {
        backlog = new ProductBacklog();
        backlog.setId(1L);

        epic = new Epic();
        epic.setId(1L);
        epic.setTitle("Epic 1");
        epic.setDescription("Description");
        epic.setProductBacklog(backlog);
        epic.setUserStories(new ArrayList<>()); // liste modifiable

        story = new UserStory();
        story.setId(1L);
    }

    @Test
    void shouldCreateEpic() {
        when(productBacklogRepository.findById(1L)).thenReturn(Optional.of(backlog));
        when(epicRepository.existsByTitleAndProductBacklogId("Epic 1", 1L)).thenReturn(false);
        when(epicRepository.save(any(Epic.class))).thenReturn(epic);

        Epic created = epicService.createEpic(1L, "Epic 1", "Description");

        assertThat(created).isNotNull();
        assertThat(created.getTitle()).isEqualTo("Epic 1");
        verify(epicRepository).save(any(Epic.class));
    }

    @Test
    void shouldThrowExceptionWhenEpicTitleExists() {
        when(productBacklogRepository.findById(1L)).thenReturn(Optional.of(backlog));
        when(epicRepository.existsByTitleAndProductBacklogId("Epic 1", 1L)).thenReturn(true);

        assertThatThrownBy(() ->
                epicService.createEpic(1L, "Epic 1", "Description"))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("already exists");

        verify(epicRepository, never()).save(any());
    }

    @Test
    void shouldThrowExceptionWhenBacklogNotFound() {
        when(productBacklogRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() ->
                epicService.createEpic(999L, "Epic X", "Desc"))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void shouldGetEpicById() {
        when(epicRepository.findById(1L)).thenReturn(Optional.of(epic));

        Epic found = epicService.getEpicById(1L);

        assertThat(found).isNotNull();
        assertThat(found.getId()).isEqualTo(1L);
    }

    @Test
    void shouldThrowExceptionWhenEpicNotFound() {
        when(epicRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> epicService.getEpicById(999L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void shouldGetEpicsByProductBacklog() {
        when(epicRepository.findByProductBacklogId(1L))
                .thenReturn(new ArrayList<>(List.of(epic)));

        List<Epic> epics = epicService.getEpicsByProductBacklog(1L);

        assertThat(epics).hasSize(1);
        assertThat(epics.get(0).getTitle()).isEqualTo("Epic 1");
    }

    @Test
    void shouldUpdateEpic() {
        when(epicRepository.findById(1L)).thenReturn(Optional.of(epic));
        when(epicRepository.existsByTitleAndProductBacklogId("Epic Updated", 1L)).thenReturn(false);
        when(epicRepository.save(any(Epic.class))).thenReturn(epic);

        Epic updated = epicService.updateEpic(1L, "Epic Updated", "New Desc");

        assertThat(updated.getTitle()).isEqualTo("Epic Updated");
        assertThat(updated.getDescription()).isEqualTo("New Desc");
    }

    @Test
    void shouldThrowExceptionWhenUpdatingWithExistingTitle() {
        Epic anotherEpic = new Epic();
        anotherEpic.setId(2L);
        anotherEpic.setTitle("Epic Updated");
        anotherEpic.setProductBacklog(backlog);

        epic.setUserStories(new ArrayList<>());

        when(epicRepository.findById(1L)).thenReturn(Optional.of(epic));
        when(epicRepository.existsByTitleAndProductBacklogId("Epic Updated", 1L)).thenReturn(true);

        assertThatThrownBy(() -> epicService.updateEpic(1L, "Epic Updated", "Desc"))
                .isInstanceOf(BusinessException.class);
    }

    @Test
    void shouldDeleteEpic() {
        // Ajouter une user story à l'epic
        story.setEpic(epic);
        epic.setUserStories(new ArrayList<>(List.of(story)));

        when(epicRepository.findById(1L)).thenReturn(Optional.of(epic));

        epicService.deleteEpic(1L);

        assertThat(story.getEpic()).isNull();
        verify(epicRepository).delete(epic);
    }

    @Test
    void shouldAddUserStoryToEpic() {
        when(epicRepository.findById(1L)).thenReturn(Optional.of(epic));
        when(userStoryRepository.findById(1L)).thenReturn(Optional.of(story));
        when(userStoryRepository.save(any(UserStory.class))).thenReturn(story);

        epicService.addUserStoryToEpic(1L, 1L);

        assertThat(story.getEpic()).isEqualTo(epic);
        verify(userStoryRepository).save(story);
    }

    @Test
    void shouldThrowExceptionWhenUserStoryAlreadyAssigned() {
        story.setEpic(epic);
        when(epicRepository.findById(1L)).thenReturn(Optional.of(epic));
        when(userStoryRepository.findById(1L)).thenReturn(Optional.of(story));

        assertThatThrownBy(() -> epicService.addUserStoryToEpic(1L, 1L))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("already assigned");
    }
}
