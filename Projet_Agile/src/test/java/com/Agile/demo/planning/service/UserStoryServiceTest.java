package com.Agile.demo.planning.service;

import com.Agile.demo.common.exception.ResourceNotFoundException;
import com.Agile.demo.model.AcceptanceCriteria;
import com.Agile.demo.model.ProductBacklog;
import com.Agile.demo.model.UserStory;
import com.Agile.demo.model.UserStoryDescription;
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
        story1 = UserStory.builder()
                .title("Story 1")
                .description(new UserStoryDescription("Role 1", "Action 1", "Purpose 1"))
                .storyPoints(3)
                .build();
        story1.setId(101L);

        story2 = UserStory.builder()
                .title("Story 2")
                .description(new UserStoryDescription("Role 2", "Action 2", "Purpose 2"))
                .storyPoints(5)
                .build();
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
        assertThat(result.getDescription().getRole()).isEqualTo("Role U");
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

    @Test
    void createUserStoryWithCriteria_shouldSaveWithCriteria() {
        // Arrange
        when(productBacklogRepository.findById(1L)).thenReturn(Optional.of(backlog));

        // Mock doit capturer l'argument et le retourner
        when(userStoryRepository.save(any(UserStory.class))).thenAnswer(invocation -> {
            UserStory savedStory = invocation.getArgument(0);
            savedStory.setId(101L); // Simuler l'ID généré
            return savedStory;
        });

        List<String> given = List.of("I am on the login page");
        List<String> when = List.of("I enter valid credentials", "I click login");
        List<String> then = List.of("I should be redirected to dashboard");

        // Act
        UserStory result = userStoryService.createUserStoryWithCriteria(
                1L, "Login", "user", "login", "access account",
                given, when, then, 5
        );

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getAcceptanceCriteria()).isNotNull();
        assertThat(result.getAcceptanceCriteria().getGivenClauses()).hasSize(1);
        assertThat(result.getAcceptanceCriteria().getWhenClauses()).hasSize(2);
        assertThat(result.getAcceptanceCriteria().getThenClauses()).hasSize(1);
        verify(userStoryRepository).save(any(UserStory.class));
    }

    @Test
    void updateAcceptanceCriteria_shouldUpdateCriteria() {
        when(userStoryRepository.findById(101L)).thenReturn(Optional.of(story1));
        when(userStoryRepository.save(story1)).thenReturn(story1);

        List<String> given = List.of("Given clause");
        List<String> when = List.of("When clause");
        List<String> then = List.of("Then clause");

        UserStory result = userStoryService.updateAcceptanceCriteria(101L, given, when, then);

        assertThat(result.getAcceptanceCriteria().getGivenClauses()).hasSize(1);
        verify(userStoryRepository).save(story1);
    }

    @Test
    void addGivenClause_shouldAddClause() {
        when(userStoryRepository.findById(101L)).thenReturn(Optional.of(story1));
        when(userStoryRepository.save(story1)).thenReturn(story1);

        userStoryService.addGivenClause(101L, "I am authenticated");

        verify(userStoryRepository).save(story1);
    }

    @Test
    void getReadyStories_shouldReturnOnlyValidStories() {
        //  Créer des critères VALIDES
        AcceptanceCriteria criteria = new AcceptanceCriteria();
        criteria.addGiven("I am authenticated");
        criteria.addWhen("I perform action");
        criteria.addThen("I see result");

        UserStory validStory = UserStory.builder()
                .title("Valid")
                .description(new UserStoryDescription("r", "a", "p"))
                .acceptanceCriteria(criteria)  //  Critères valides
                .storyPoints(5)
                .build();

        when(userStoryRepository.findByProductBacklogId(1L)).thenReturn(List.of(validStory));

        List<UserStory> result = userStoryService.getReadyStories(1L);

        assertThat(result).isNotEmpty();
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getTitle()).isEqualTo("Valid");
    }

    @Test
    void updateMetric_shouldSetMetricValue() {
        when(userStoryRepository.findById(101L)).thenReturn(Optional.of(story1));
        when(userStoryRepository.save(story1)).thenReturn(story1);

        userStoryService.updateMetric(101L, "businessValue", 9);

        assertThat(story1.getMetric("businessValue")).isEqualTo(9);
        verify(userStoryRepository).save(story1);
    }

    @Test
    void isReadyForSprint_shouldReturnTrue_whenValid() {
        AcceptanceCriteria criteria = new AcceptanceCriteria();
        criteria.addGiven("G");
        criteria.addWhen("W");
        criteria.addThen("T");

        story1.setAcceptanceCriteria(criteria);
        story1.setStoryPoints(5);

        when(userStoryRepository.findById(101L)).thenReturn(Optional.of(story1));

        boolean result = userStoryService.isReadyForSprint(101L);

        assertThat(result).isTrue();
    }
}

