package com.Agile.demo.planning.service;

import com.Agile.demo.common.exception.ResourceNotFoundException;
import com.Agile.demo.model.ProductBacklog;
import com.Agile.demo.model.UserStory;
import com.Agile.demo.planning.repository.ProductBacklogRepository;
import com.Agile.demo.planning.repository.UserStoryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductBacklogServiceTest {

    @Mock
    private ProductBacklogRepository productBacklogRepository;

    @Mock
    private UserStoryRepository userStoryRepository;

    @InjectMocks
    private ProductBacklogService productBacklogService;

    private ProductBacklog backlog;
    private UserStory story1;
    private UserStory story2;

    @BeforeEach
    void setUp() {
        backlog = new ProductBacklog();
        backlog.setId(1L);

        story1 = new UserStory();
        story1.setId(101L);
        story2 = new UserStory();
        story2.setId(102L);
    }

    @Test
    void getProductBacklogById_shouldReturnBacklog() {
        when(productBacklogRepository.findById(1L)).thenReturn(Optional.of(backlog));

        ProductBacklog result = productBacklogService.getProductBacklogById(1L);

        assertThat(result).isEqualTo(backlog);
        verify(productBacklogRepository).findById(1L);
    }

    @Test
    void getProductBacklogById_shouldThrowException_whenNotFound() {
        when(productBacklogRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> productBacklogService.getProductBacklogById(1L));
    }

    @Test
    void getProductBacklogByProject_shouldReturnBacklog() {
        when(productBacklogRepository.findByProjectId(10L)).thenReturn(Optional.of(backlog));

        ProductBacklog result = productBacklogService.getProductBacklogByProject(10L);

        assertThat(result).isEqualTo(backlog);
        verify(productBacklogRepository).findByProjectId(10L);
    }

    @Test
    void getProductBacklogByProject_shouldThrowException_whenNotFound() {
        when(productBacklogRepository.findByProjectId(10L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> productBacklogService.getProductBacklogByProject(10L));
    }

    @Test
    void getAllStories_shouldReturnStories() {
        when(userStoryRepository.findByProductBacklogId(1L)).thenReturn(Arrays.asList(story1, story2));

        List<UserStory> result = productBacklogService.getAllStories(1L);

        assertThat(result).containsExactly(story1, story2);
        verify(userStoryRepository).findByProductBacklogId(1L);
    }

    @Test
    void getUnassignedStories_shouldReturnStories() {
        when(userStoryRepository.findUnassignedStoriesByBacklogId(1L)).thenReturn(List.of(story1));

        List<UserStory> result = productBacklogService.getUnassignedStories(1L);

        assertThat(result).containsExactly(story1);
        verify(userStoryRepository).findUnassignedStoriesByBacklogId(1L);
    }

    @Test
    void getTopPriorityStories_shouldReturnLimitedStories() {
        when(userStoryRepository.findByProductBacklogIdOrderedByPriority(1L))
                .thenReturn(Arrays.asList(story1, story2));

        List<UserStory> result = productBacklogService.getTopPriorityStories(1L, 1);

        assertThat(result).containsExactly(story1); // seulement le top 1
        verify(userStoryRepository).findByProductBacklogIdOrderedByPriority(1L);
    }
}
