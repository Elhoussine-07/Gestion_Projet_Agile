package com.Agile.demo.planning.service;

import com.Agile.demo.common.exception.ResourceNotFoundException;
import com.Agile.demo.common.exception.ValidationException;
import com.Agile.demo.model.*;
import com.Agile.demo.planning.prioritization.PrioritizationStrategyProvider;
import com.Agile.demo.planning.repository.ProductBacklogRepository;
import com.Agile.demo.execution.repositories.SprintBacklogRepository;
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductBacklogServiceTest {

    @Mock
    private ProductBacklogRepository productBacklogRepository;

    @Mock
    private UserStoryRepository userStoryRepository;

    @Mock
    private SprintBacklogRepository sprintRepository;

    @Mock
    private PrioritizationStrategyProvider prioritizationStrategyProvider;

    @Mock
    private IPrioritizationStrategy prioritizationStrategy;

    @InjectMocks
    private ProductBacklogService productBacklogService;

    private ProductBacklog backlog;
    private UserStory story1;
    private UserStory story2;
    private SprintBacklog sprint;
    private UserStoryDescription validDescription;

    @BeforeEach
    void setUp() {
        backlog = new ProductBacklog();
        backlog.setId(1L);
        backlog.setTotalBusinessValue(0);

        // Description valide avec méthodes isEmpty() et isComplete()
        validDescription = new UserStoryDescription("User","login","access the system");

        story1 = new UserStory();
        story1.setId(101L);
        story1.setDescription(validDescription);
        story1.setStoryPoints(5);
        story1.setBusinessValue(50);
        story1.setProductBacklog(backlog);
        story1.setStatus(WorkItemStatus.TODO);

        story2 = new UserStory();
        story2.setId(102L);
        story2.setDescription(validDescription);
        story2.setStoryPoints(8);
        story2.setBusinessValue(80);
        story2.setProductBacklog(backlog);
        story2.setStatus(WorkItemStatus.TODO);

        sprint = new SprintBacklog();
        sprint.setId(1L);
        sprint.setCapacity(50);
        sprint.setSprintStatus(SprintStatus.ACTIVE);
    }

    // ========== Tests existants ==========

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

        assertThat(result).containsExactly(story1);
        verify(userStoryRepository).findByProductBacklogIdOrderedByPriority(1L);
    }

    // ========== Tests pour applyPrioritization ==========

    @Test
    void applyPrioritization_shouldPrioritizeStories_whenValid() {
        // Given
        List<UserStory> stories = Arrays.asList(story1, story2);
        List<UserStory> prioritizedStories = Arrays.asList(story2, story1);

        when(productBacklogRepository.findById(1L))
                .thenReturn(Optional.of(backlog));

        when(userStoryRepository.findByProductBacklogId(1L))
                .thenReturn(stories);

        when(prioritizationStrategyProvider.getStrategy(PrioritizationMethod.WSJF))
                .thenReturn(prioritizationStrategy);

        when(prioritizationStrategy.prioritizeBacklog(stories))
                .thenReturn(prioritizedStories);

        when(userStoryRepository.saveAll(prioritizedStories))
                .thenReturn(prioritizedStories);

        when(productBacklogRepository.save(any(ProductBacklog.class)))
                .thenReturn(backlog);

        // When
        productBacklogService.applyPrioritization(1L, PrioritizationMethod.WSJF);

        // Then
        verify(prioritizationStrategyProvider)
                .getStrategy(PrioritizationMethod.WSJF);

        verify(prioritizationStrategy)
                .prioritizeBacklog(stories);

        verify(userStoryRepository)
                .saveAll(prioritizedStories);

        verify(productBacklogRepository)
                .save(backlog);
    }

    @Test
    void applyPrioritization_shouldThrowException_whenBacklogNotFound() {
        when(productBacklogRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> productBacklogService.applyPrioritization(1L, PrioritizationMethod.MOSCOW));
    }

    @Test
    void applyPrioritization_shouldDoNothing_whenNoStories() {
        when(productBacklogRepository.findById(1L)).thenReturn(Optional.of(backlog));
        when(userStoryRepository.findByProductBacklogId(1L)).thenReturn(List.of());

        productBacklogService.applyPrioritization(1L, PrioritizationMethod.VALUE_EFFORT);

        //verify(prioritizationStrategyFactory, never()).getStrategy(any());
        verify(userStoryRepository, never()).saveAll(anyList());
    }

    @Test
    void applyPrioritization_shouldThrowException_whenStoryInvalid() {
        UserStory invalidStory = new UserStory();
        invalidStory.setId(103L);
        invalidStory.setDescription(null); // Invalid
        invalidStory.setStoryPoints(5);

        when(productBacklogRepository.findById(1L)).thenReturn(Optional.of(backlog));
        when(userStoryRepository.findByProductBacklogId(1L)).thenReturn(List.of(invalidStory));

        assertThrows(ValidationException.class,
                () -> productBacklogService.applyPrioritization(1L, PrioritizationMethod.MOSCOW));
    }

    // ========== Tests pour moveStoryToSprint ==========

    @Test
    void moveStoryToSprint_shouldMoveStory_whenValid() {
        when(userStoryRepository.findById(101L)).thenReturn(Optional.of(story1));
        when(sprintRepository.findById(1L)).thenReturn(Optional.of(sprint));
        when(userStoryRepository.findBySprintId(1L)).thenReturn(List.of());
        when(userStoryRepository.save(story1)).thenReturn(story1);

        productBacklogService.moveStoryToSprint(101L, 1L);

        assertThat(story1.getSprintBacklog()).isEqualTo(sprint);
        verify(userStoryRepository).save(story1);
    }

    @Test
    void moveStoryToSprint_shouldThrowException_whenStoryNotFound() {
        when(userStoryRepository.findById(101L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> productBacklogService.moveStoryToSprint(101L, 1L));
    }

    @Test
    void moveStoryToSprint_shouldThrowException_whenSprintNotFound() {
        when(userStoryRepository.findById(101L)).thenReturn(Optional.of(story1));
        when(sprintRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> productBacklogService.moveStoryToSprint(101L, 1L));
    }

    @Test
    void moveStoryToSprint_shouldThrowException_whenSprintCompleted() {
        sprint.setSprintStatus(SprintStatus.COMPLETED);

        when(userStoryRepository.findById(101L)).thenReturn(Optional.of(story1));
        when(sprintRepository.findById(1L)).thenReturn(Optional.of(sprint));

        assertThrows(ValidationException.class,
                () -> productBacklogService.moveStoryToSprint(101L, 1L));
    }

    @Test
    void moveStoryToSprint_shouldThrowException_whenStoryDone() {
        story1.setStatus(WorkItemStatus.DONE);

        when(userStoryRepository.findById(101L)).thenReturn(Optional.of(story1));
        //when(sprintRepository.findById(1L)).thenReturn(Optional.of(sprint));

        assertThrows(ValidationException.class,
                () -> productBacklogService.moveStoryToSprint(101L, 1L));
    }

    @Test
    void moveStoryToSprint_shouldThrowException_whenCapacityExceeded() {
        sprint.setCapacity(10);
        story1.setStoryPoints(15);

        when(userStoryRepository.findById(101L)).thenReturn(Optional.of(story1));
        when(sprintRepository.findById(1L)).thenReturn(Optional.of(sprint));
        when(userStoryRepository.findBySprintId(1L)).thenReturn(List.of());

        assertThrows(ValidationException.class,
                () -> productBacklogService.moveStoryToSprint(101L, 1L));
    }

    @Test
    void moveStoryToSprint_shouldThrowException_whenStoryHasNoStoryPoints() {
        story1.setStoryPoints(null);

        when(userStoryRepository.findById(101L)).thenReturn(Optional.of(story1));

        assertThrows(ValidationException.class,
                () -> productBacklogService.moveStoryToSprint(101L, 1L));
    }

    // ========== Tests pour reorderStories ==========

    @Test
    void reorderStories_shouldReorderSuccessfully() {
        List<Long> newOrder = Arrays.asList(102L, 101L);

        when(productBacklogRepository.findById(1L)).thenReturn(Optional.of(backlog));
        when(userStoryRepository.findAllById(newOrder)).thenReturn(Arrays.asList(story2, story1));
        when(userStoryRepository.saveAll(anyList())).thenReturn(Arrays.asList(story2, story1));

        productBacklogService.reorderStories(1L, newOrder);

        assertThat(story2.getPriority()).isEqualTo(1);
        assertThat(story1.getPriority()).isEqualTo(2);
        verify(userStoryRepository).saveAll(anyList());
    }

    @Test
    void reorderStories_shouldThrowException_whenBacklogNotFound() {
        when(productBacklogRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> productBacklogService.reorderStories(1L, List.of(101L)));
    }

    @Test
    void reorderStories_shouldThrowException_whenInvalidStoryId() {
        List<Long> storyIds = Arrays.asList(101L, 999L);

        when(productBacklogRepository.findById(1L)).thenReturn(Optional.of(backlog));
        when(userStoryRepository.findAllById(storyIds)).thenReturn(List.of(story1));

        assertThrows(ValidationException.class,
                () -> productBacklogService.reorderStories(1L, storyIds));
    }

    @Test
    void reorderStories_shouldThrowException_whenStoryNotInBacklog() {
        ProductBacklog otherBacklog = new ProductBacklog();
        otherBacklog.setId(2L);
        story2.setProductBacklog(otherBacklog);

        List<Long> storyIds = Arrays.asList(101L, 102L);

        when(productBacklogRepository.findById(1L)).thenReturn(Optional.of(backlog));
        when(userStoryRepository.findAllById(storyIds)).thenReturn(Arrays.asList(story1, story2));

        assertThrows(ValidationException.class,
                () -> productBacklogService.reorderStories(1L, storyIds));
    }

    // ========== Tests pour calculateTotalBusinessValue ==========

    @Test
    void calculateTotalBusinessValue_shouldCalculateCorrectly() {
        when(userStoryRepository.findByProductBacklogId(1L))
                .thenReturn(Arrays.asList(story1, story2));

        Integer total = productBacklogService.calculateTotalBusinessValue(1L);

        assertThat(total).isEqualTo(130); // 50 + 80
    }

    @Test
    void calculateTotalBusinessValue_shouldReturnZero_whenNoStories() {
        when(userStoryRepository.findByProductBacklogId(1L)).thenReturn(List.of());

        Integer total = productBacklogService.calculateTotalBusinessValue(1L);

        assertThat(total).isEqualTo(0);
    }

    @Test
    void calculateTotalBusinessValue_shouldIgnoreNullValues() {
        story2.setBusinessValue(null);
        when(userStoryRepository.findByProductBacklogId(1L))
                .thenReturn(Arrays.asList(story1, story2));

        Integer total = productBacklogService.calculateTotalBusinessValue(1L);

        assertThat(total).isEqualTo(50); // Only story1
    }
}