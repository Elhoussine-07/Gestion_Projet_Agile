package com.Agile.demo.planning.service;

import com.Agile.demo.common.exception.ResourceNotFoundException;
import com.Agile.demo.model.ProductBacklog;
import com.Agile.demo.model.Role;
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

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserStoryServiceTest {

    @Mock
    private UserStoryRepository userStoryRepository;

    @Mock
    private ProductBacklogRepository productBacklogRepository;

    @Mock
    private EpicRepository epicRepository;

    @InjectMocks
    private UserStoryService userStoryService;

    private ProductBacklog backlog;
    private UserStory story1;
    private UserStory story2;

    @BeforeEach
    void setUp() {
        backlog = new ProductBacklog();
        backlog.setId(1L);

        story1 = new UserStory("Story 1", "Role 1", "Action 1", "Purpose 1", 3);
        story1.setId(101L);
        story2 = new UserStory("Story 2", "Role 2", "Action 2", "Purpose 2", 5);
        story2.setId(102L);
    }

    @Test
    void createUserStory_shouldSaveStory() {
        when(productBacklogRepository.findById(1L)).thenReturn(Optional.of(backlog));
        when(userStoryRepository.save(any(UserStory.class))).thenReturn(story1);

        UserStory result = userStoryService.createUserStory(1L, "Story 1", "Role 1", "Action 1", "Purpose 1", 3);

        assertThat(result).isEqualTo(story1);
        verify(userStoryRepository).save(any(UserStory.class));
    }

    @Test
    void createUserStory_shouldThrowException_whenBacklogNotFound() {
        when(productBacklogRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> userStoryService.createUserStory(1L, "Story 1", "Role 1", "Action 1", "Purpose 1", 3));
    }

    @Test
    void getUserStoryById_shouldReturnStory() {
        when(userStoryRepository.findById(101L)).thenReturn(Optional.of(story1));

        UserStory result = userStoryService.getUserStoryById(101L);

        assertThat(result).isEqualTo(story1);
    }

    @Test
    void getUserStoryById_shouldThrowException_whenNotFound() {
        when(userStoryRepository.findById(101L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> userStoryService.getUserStoryById(101L));
    }

    @Test
    void getUserStoriesByProductBacklog_shouldReturnList() {
        when(userStoryRepository.findByProductBacklogId(1L)).thenReturn(Arrays.asList(story1, story2));

        List<UserStory> result = userStoryService.getUserStoriesByProductBacklog(1L);

        assertThat(result).containsExactly(story1, story2);
    }

    @Test
    void getUnassignedStories_shouldReturnList() {
        when(userStoryRepository.findByProductBacklogIdAndEpicIsNull(1L)).thenReturn(List.of(story1));

        List<UserStory> result = userStoryService.getUnassignedStories(1L);

        assertThat(result).containsExactly(story1);
    }

    @Test
    void getStoriesOrderedByPriority_shouldReturnList() {
        when(userStoryRepository.findByProductBacklogIdOrderedByPriority(1L))
                .thenReturn(Arrays.asList(story2, story1));

        List<UserStory> result = userStoryService.getStoriesOrderedByPriority(1L);

        assertThat(result).containsExactly(story2, story1);
    }

    @Test
    void updateUserStory_shouldUpdateAndSave() {
        when(userStoryRepository.findById(101L)).thenReturn(Optional.of(story1));
        when(userStoryRepository.save(story1)).thenReturn(story1);

        UserStory result = userStoryService.updateUserStory(101L, "Updated Story", "Role U", "Action U", "Purpose U", 8);

        assertThat(result.getTitle()).isEqualTo("Updated Story");
        assertThat(result.getDescription()).isEqualTo("Role U");
        assertThat(result.getStoryPoints()).isEqualTo(8);
        verify(userStoryRepository).save(story1);
    }

    @Test
    void updatePriority_shouldSetPriority() {
        story1.setPriority(1);
        when(userStoryRepository.findById(101L)).thenReturn(Optional.of(story1));
        when(userStoryRepository.save(story1)).thenReturn(story1);

        userStoryService.updatePriority(101L, 5);

        assertThat(story1.getPriority()).isEqualTo(5);
        verify(userStoryRepository).save(story1);
    }

    @Test
    void deleteUserStory_shouldDeleteStory() {
        when(userStoryRepository.findById(101L)).thenReturn(Optional.of(story1));

        userStoryService.deleteUserStory(101L);

        verify(userStoryRepository).delete(story1);
    }
}

