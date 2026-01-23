package com.Agile.demo.planning.service;

import com.Agile.demo.common.exception.ResourceNotFoundException;
import com.Agile.demo.model.AcceptanceCriteria;
import com.Agile.demo.model.ProductBacklog;
import com.Agile.demo.model.UserStory;
import com.Agile.demo.model.UserStoryDescription;
import com.Agile.demo.planning.dto.userstory.*;
import com.Agile.demo.planning.mapper.UserStoryMapper;
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
import java.util.Map;
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
    private UserStoryMapper userStoryMapper;

    @InjectMocks
    private UserStoryService userStoryService;

    private ProductBacklog backlog;
    private UserStory story1;
    private UserStory story2;
    private UserStoryDTO dto1;
    private UserStoryDTO dto2;

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

        dto1 = UserStoryDTO.builder()
                .id(101)
                .title("Story 1")
                .role("Role 1")
                .action("Action 1")
                .purpose("Purpose 1")
                .storyPoints(3)
                .build();

        dto2 = UserStoryDTO.builder()
                .id(102)
                .title("Story 2")
                .role("Role 2")
                .action("Action 2")
                .purpose("Purpose 2")
                .storyPoints(5)
                .build();
    }

    @Test
    void createUserStory_shouldSaveStory() {
        // Arrange
        CreateUserStoryDTO createDto = new CreateUserStoryDTO(1L, "Story 1", "Role 1", "Action 1", "Purpose 1", 3);

        when(productBacklogRepository.findById(1L)).thenReturn(Optional.of(backlog));
        when(userStoryMapper.toEntity(createDto)).thenReturn(story1);
        when(userStoryRepository.save(any(UserStory.class))).thenReturn(story1);
        when(userStoryMapper.toDto(story1)).thenReturn(dto1);

        // Act
        UserStoryDTO result = userStoryService.createUserStory(createDto);

        // Assert
        assertThat(result).isEqualTo(dto1);
        verify(userStoryRepository).save(any(UserStory.class));
        verify(userStoryMapper).toDto(story1);
    }

    @Test
    void createUserStory_shouldThrowException_whenBacklogNotFound() {
        // Arrange
        CreateUserStoryDTO createDto = new CreateUserStoryDTO(1L, "Story 1", "Role 1", "Action 1", "Purpose 1", 3);
        when(productBacklogRepository.findById(1L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> userStoryService.createUserStory(createDto));
    }

    @Test
    void getUserStoryById_shouldReturnDto() {
        // Arrange
        when(userStoryRepository.findById(101L)).thenReturn(Optional.of(story1));
        when(userStoryMapper.toDto(story1)).thenReturn(dto1);

        // Act
        UserStoryDTO result = userStoryService.getUserStoryById(101L);

        // Assert
        assertThat(result).isEqualTo(dto1);
        verify(userStoryMapper).toDto(story1);
    }

    @Test
    void getUserStoryById_shouldThrowException_whenNotFound() {
        // Arrange
        when(userStoryRepository.findById(101L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> userStoryService.getUserStoryById(101L));
    }

    @Test
    void getUserStoriesByProductBacklog_shouldReturnDtoList() {
        // Arrange
        when(userStoryRepository.findByProductBacklogId(1L)).thenReturn(Arrays.asList(story1, story2));
        when(userStoryMapper.toDtoList(Arrays.asList(story1, story2))).thenReturn(Arrays.asList(dto1, dto2));

        // Act
        List<UserStoryDTO> result = userStoryService.getUserStoriesByProductBacklog(1L);

        // Assert
        assertThat(result).containsExactly(dto1, dto2);
        verify(userStoryMapper).toDtoList(Arrays.asList(story1, story2));
    }

    @Test
    void getUnassignedStories_shouldReturnDtoList() {
        // Arrange
        when(userStoryRepository.findByProductBacklogIdAndEpicIsNull(1L)).thenReturn(List.of(story1));
        when(userStoryMapper.toDtoList(List.of(story1))).thenReturn(List.of(dto1));

        // Act
        List<UserStoryDTO> result = userStoryService.getUnassignedStories(1L);

        // Assert
        assertThat(result).containsExactly(dto1);
    }

    @Test
    void getStoriesOrderedByPriority_shouldReturnDtoList() {
        // Arrange
        when(userStoryRepository.findByProductBacklogIdOrderedByPriority(1L))
                .thenReturn(Arrays.asList(story2, story1));
        when(userStoryMapper.toDtoList(Arrays.asList(story2, story1)))
                .thenReturn(Arrays.asList(dto2, dto1));

        // Act
        List<UserStoryDTO> result = userStoryService.getStoriesOrderedByPriority(1L);

        // Assert
        assertThat(result).containsExactly(dto2, dto1);
    }

    @Test
    void updateUserStory_shouldUpdateAndReturnDto() {
        // Arrange
        UpdateUserStoryDTO updateDto = new UpdateUserStoryDTO("Updated Story", "Role U", "Action U", "Purpose U", 8);
        UserStoryDTO updatedDto = UserStoryDTO.builder()
                .id(101)
                .title("Updated Story")
                .role("Role U")
                .action("Action U")
                .purpose("Purpose U")
                .storyPoints(8)
                .build();

        when(userStoryRepository.findById(101L)).thenReturn(Optional.of(story1));
        doNothing().when(userStoryMapper).updateEntityFromDto(updateDto, story1);
        when(userStoryRepository.save(story1)).thenReturn(story1);
        when(userStoryMapper.toDto(story1)).thenReturn(updatedDto);

        // Act
        UserStoryDTO result = userStoryService.updateUserStory(101L, updateDto);

        // Assert
        assertThat(result.getTitle()).isEqualTo("Updated Story");
        assertThat(result.getStoryPoints()).isEqualTo(8);
        verify(userStoryMapper).updateEntityFromDto(updateDto, story1);
        verify(userStoryRepository).save(story1);
    }

    @Test
    void updatePriority_shouldSetPriority() {
        // Arrange
        UpdatePriorityDTO priorityDto = new UpdatePriorityDTO(5);
        story1.setPriority(1);

        when(userStoryRepository.findById(101L)).thenReturn(Optional.of(story1));
        when(userStoryRepository.save(story1)).thenReturn(story1);

        // Act
        userStoryService.updatePriority(101L, priorityDto);

        // Assert
        assertThat(story1.getPriority()).isEqualTo(5);
        verify(userStoryRepository).save(story1);
    }

    @Test
    void deleteUserStory_shouldDeleteStory() {
        // Arrange
        when(userStoryRepository.findById(101L)).thenReturn(Optional.of(story1));

        // Act
        userStoryService.deleteUserStory(101L);

        // Assert
        verify(userStoryRepository).delete(story1);
    }

    @Test
    void createUserStoryWithCriteria_shouldSaveWithCriteria() {
        // Arrange
        List<String> given = List.of("I am on the login page");
        List<String> when = List.of("I enter valid credentials", "I click login");
        List<String> then = List.of("I should be redirected to dashboard");

        CreateUserStoryWithCriteriaDTO createDto = new CreateUserStoryWithCriteriaDTO(
                1L, "Login", "user", "login", "access account",
                given, when, then, 5
        );

        UserStory storyWithCriteria = UserStory.builder()
                .title("Login")
                .description(new UserStoryDescription("user", "login", "access account"))
                .storyPoints(5)
                .build();
        storyWithCriteria.setId(101L);

        AcceptanceCriteria criteria = new AcceptanceCriteria();
        given.forEach(criteria::addGiven);
        when.forEach(criteria::addWhen);
        then.forEach(criteria::addThen);
        storyWithCriteria.setAcceptanceCriteria(criteria);

        UserStoryDTO dtoWithCriteria = UserStoryDTO.builder()
                .id(101)
                .title("Login")
                .givenClauses(given)
                .whenClauses(when)
                .thenClauses(then)
                .build();

        when(productBacklogRepository.findById(1L)).thenReturn(Optional.of(backlog));
        when(userStoryMapper.toEntityWithCriteria(createDto)).thenReturn(storyWithCriteria);
        when(userStoryRepository.save(any(UserStory.class))).thenReturn(storyWithCriteria);
        when(userStoryMapper.toDto(storyWithCriteria)).thenReturn(dtoWithCriteria);

        // Act
        UserStoryDTO result = userStoryService.createUserStoryWithCriteria(createDto);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getGivenClauses()).hasSize(1);
        assertThat(result.getWhenClauses()).hasSize(2);
        assertThat(result.getThenClauses()).hasSize(1);
        verify(userStoryRepository).save(any(UserStory.class));
    }

    @Test
    void updateAcceptanceCriteria_shouldUpdateCriteria() {
        // Arrange
        List<String> given = List.of("Given clause");
        List<String> when = List.of("When clause");
        List<String> then = List.of("Then clause");
        UpdateAcceptanceCriteriaDTO updateDto = new UpdateAcceptanceCriteriaDTO(given, when, then);

        UserStoryDTO updatedDto = UserStoryDTO.builder()
                .id(101)
                .givenClauses(given)
                .whenClauses(when)
                .thenClauses(then)
                .build();

        when(userStoryRepository.findById(101L)).thenReturn(Optional.of(story1));
        when(userStoryRepository.save(story1)).thenReturn(story1);
        when(userStoryMapper.toDto(story1)).thenReturn(updatedDto);

        // Act
        UserStoryDTO result = userStoryService.updateAcceptanceCriteria(101L, updateDto);

        // Assert
        assertThat(result.getGivenClauses()).hasSize(1);
        verify(userStoryRepository).save(story1);
    }

    @Test
    void addGivenClause_shouldAddClause() {
        // Arrange
        when(userStoryRepository.findById(101L)).thenReturn(Optional.of(story1));
        when(userStoryRepository.save(story1)).thenReturn(story1);
        when(userStoryMapper.toDto(story1)).thenReturn(dto1);

        // Act
        UserStoryDTO result = userStoryService.addGivenClause(101L, "I am authenticated");

        // Assert
        assertThat(result).isNotNull();
        verify(userStoryRepository).save(story1);
    }

    @Test
    void getReadyStories_shouldReturnOnlyValidStories() {
        // Arrange
        AcceptanceCriteria criteria = new AcceptanceCriteria();
        criteria.addGiven("I am authenticated");
        criteria.addWhen("I perform action");
        criteria.addThen("I see result");

        UserStory validStory = UserStory.builder()
                .title("Valid")
                .description(new UserStoryDescription("r", "a", "p"))
                .acceptanceCriteria(criteria)
                .storyPoints(5)
                .build();

        UserStoryDTO validDto = UserStoryDTO.builder()
                .title("Valid")
                .isValid(true)
                .storyPoints(5)
                .build();

        when(userStoryRepository.findByProductBacklogId(1L)).thenReturn(List.of(validStory));
        when(userStoryMapper.toDtoList(anyList())).thenReturn(List.of(validDto));

        // Act
        List<UserStoryDTO> result = userStoryService.getReadyStories(1L);

        // Assert
        assertThat(result).isNotEmpty();
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getTitle()).isEqualTo("Valid");
    }

    @Test
    void updateMetric_shouldSetMetricValue() {
        // Arrange
        UserStoryDTO updatedDto = UserStoryDTO.builder()
                .id(101)
                .customMetrics(Map.of("businessValue", 9))
                .build();

        when(userStoryRepository.findById(101L)).thenReturn(Optional.of(story1));
        when(userStoryRepository.save(story1)).thenReturn(story1);
        when(userStoryMapper.toDto(story1)).thenReturn(updatedDto);

        // Act
        UserStoryDTO result = userStoryService.updateMetric(101L, "businessValue", 9);

        // Assert
        assertThat(story1.getMetric("businessValue")).isEqualTo(9);
        assertThat(result.getCustomMetrics()).containsEntry("businessValue", 9);
        verify(userStoryRepository).save(story1);
    }

    @Test
    void updateMetrics_shouldSetMultipleMetrics() {
        // Arrange
        Map<String, Integer> metrics = Map.of(
                "businessValue", 9,
                "technicalDebt", 3
        );
        UpdateMetricsDTO metricsDto = new UpdateMetricsDTO(metrics);

        UserStoryDTO updatedDto = UserStoryDTO.builder()
                .id(101)
                .customMetrics(metrics)
                .build();

        when(userStoryRepository.findById(101L)).thenReturn(Optional.of(story1));
        when(userStoryRepository.save(story1)).thenReturn(story1);
        when(userStoryMapper.toDto(story1)).thenReturn(updatedDto);

        // Act
        UserStoryDTO result = userStoryService.updateMetrics(101L, metricsDto);

        // Assert
        assertThat(result.getCustomMetrics()).hasSize(2);
        verify(userStoryRepository).save(story1);
    }

    @Test
    void isReadyForSprint_shouldReturnTrue_whenValid() {
        // Arrange
        AcceptanceCriteria criteria = new AcceptanceCriteria();
        criteria.addGiven("G");
        criteria.addWhen("W");
        criteria.addThen("T");

        story1.setAcceptanceCriteria(criteria);
        story1.setStoryPoints(5);

        when(userStoryRepository.findById(101L)).thenReturn(Optional.of(story1));

        // Act
        boolean result = userStoryService.isReadyForSprint(101L);

        // Assert
        assertThat(result).isTrue();
    }

    @Test
    void getAllUserStories_shouldReturnAllDtos() {
        // Arrange
        when(userStoryRepository.findAll()).thenReturn(Arrays.asList(story1, story2));
        when(userStoryMapper.toDtoList(Arrays.asList(story1, story2)))
                .thenReturn(Arrays.asList(dto1, dto2));

        // Act
        List<UserStoryDTO> result = userStoryService.getAllUserStories();

        // Assert
        assertThat(result).hasSize(2);
        assertThat(result).containsExactly(dto1, dto2);
    }

    @Test
    void getGherkinFormat_shouldReturnFormattedString() {
        // Arrange
        AcceptanceCriteria criteria = new AcceptanceCriteria();
        criteria.addGiven("I am logged in");
        criteria.addWhen("I click logout");
        criteria.addThen("I should be logged out");
        story1.setAcceptanceCriteria(criteria);

        when(userStoryRepository.findById(101L)).thenReturn(Optional.of(story1));

        // Act
        String result = userStoryService.getGherkinFormat(101L);

        // Assert
        assertThat(result).contains("Given");
        assertThat(result).contains("When");
        assertThat(result).contains("Then");
    }
}