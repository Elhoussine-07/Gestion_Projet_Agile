package com.Agile.demo.execution.services;

import com.Agile.demo.model.*;
import com.Agile.demo.execution.repositories.TaskRepository;
import com.Agile.demo.execution.repositories.UserRepository;
import com.Agile.demo.execution.repositories.UserStoryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TaskServiceTest {

    @Mock
    private TaskRepository taskRepository;

    @Mock
    private UserStoryRepository userStoryRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private TaskService taskService;

    private UserStory userStory;
    private Task task;
    private User user;
    private SprintBacklog sprint;

    @BeforeEach
    void setUp() {
        userStory = new UserStory();
        userStory.setId(1L);
        userStory.setTitle("Test User Story");

        sprint = new SprintBacklog();
        sprint.setsprintNumber(1L);
        userStory.setSprintBacklog(sprint);

        task = new Task("Test Task", 8);
        task.setId(1L);
        task.setUserStory(userStory);

        user = new User();
        user.setId(1L);
        user.setUsername("testuser");
    }

    @Test
    void createTask_WhenValidData_ShouldCreateTask() {
        // Arrange
        when(userStoryRepository.findById(1L)).thenReturn(Optional.of(userStory));
        when(taskRepository.save(any(Task.class))).thenReturn(task);

        // Act
        Task result = taskService.createTask(1L, "Test Task", 8);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getTitle()).isEqualTo("Test Task");
        assertThat(result.getEstimatedHours()).isEqualTo(8);
        verify(taskRepository).save(any(Task.class));
    }

    @Test
    void createTask_WhenUserStoryNotFound_ShouldThrowException() {
        // Arrange
        when(userStoryRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> taskService.createTask(999L, "Task", 8))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("User Story non trouvée");
    }

    @Test
    void createTask_WhenNegativeEstimatedHours_ShouldThrowException() {
        // Arrange
        when(userStoryRepository.findById(1L)).thenReturn(Optional.of(userStory));

        // Act & Assert
        assertThatThrownBy(() -> taskService.createTask(1L, "Task", -5))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("heures estimées ne peuvent pas être négatives");
    }

    @Test
    void createTaskWithDescription_WhenValidData_ShouldCreateTask() {
        // Arrange
        when(userStoryRepository.findById(1L)).thenReturn(Optional.of(userStory));
        when(taskRepository.save(any(Task.class))).thenReturn(task);

        // Act
        Task result = taskService.createTask(1L, "Task", "Description", 8);

        // Assert
        assertThat(result).isNotNull();
        verify(taskRepository).save(any(Task.class));
    }

    @Test
    void getTaskById_WhenTaskExists_ShouldReturnTask() {
        // Arrange
        when(taskRepository.findById(1L)).thenReturn(Optional.of(task));

        // Act
        Task result = taskService.getTaskById(1L);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
    }

    @Test
    void getTaskById_WhenTaskNotFound_ShouldThrowException() {
        // Arrange
        when(taskRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> taskService.getTaskById(999L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Tâche non trouvée");
    }

    @Test
    void getTasksByUserStory_ShouldReturnTasks() {
        // Arrange
        List<Task> tasks = List.of(task);
        when(taskRepository.findByUserStoryId(1L)).thenReturn(tasks);

        // Act
        List<Task> result = taskService.getTasksByUserStory(1L);

        // Assert
        assertThat(result).hasSize(1);
        assertThat(result.get(0)).isEqualTo(task);
    }

    @Test
    void getTasksBySprint_ShouldReturnTasks() {
        // Arrange
        List<Task> tasks = List.of(task);
        when(taskRepository.findBySprintBacklogId(1L)).thenReturn(tasks);

        // Act
        List<Task> result = taskService.getTasksBySprint(1L);

        // Assert
        assertThat(result).hasSize(1);
    }

    @Test
    void getTasksByUser_ShouldReturnUserTasks() {
        // Arrange
        task.assignTo(user);
        List<Task> tasks = List.of(task);
        when(taskRepository.findByAssignedUserId(1L)).thenReturn(tasks);

        // Act
        List<Task> result = taskService.getTasksByUser(1L);

        // Assert
        assertThat(result).hasSize(1);
    }

    @Test
    void assignTask_WhenValidTaskAndUser_ShouldAssignTask() {
        // Arrange
        when(taskRepository.findById(1L)).thenReturn(Optional.of(task));
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(taskRepository.save(any(Task.class))).thenReturn(task);

        // Act
        Task result = taskService.assignTask(1L, 1L);

        // Assert
        assertThat(result).isNotNull();
        verify(taskRepository).save(task);
    }

    @Test
    void assignTask_WhenUserNotFound_ShouldThrowException() {
        // Arrange
        when(taskRepository.findById(1L)).thenReturn(Optional.of(task));
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> taskService.assignTask(1L, 999L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Utilisateur non trouvé");
    }

    @Test
    void unassignTask_WhenTaskTodo_ShouldUnassignTask() {
        // Arrange
        task.assignTo(user);
        when(taskRepository.findById(1L)).thenReturn(Optional.of(task));
        when(taskRepository.save(any(Task.class))).thenReturn(task);

        // Act
        Task result = taskService.unassignTask(1L);

        // Assert
        assertThat(result).isNotNull();
        verify(taskRepository).save(task);
    }

    @Test
    void unassignTask_WhenTaskInProgress_ShouldThrowException() {
        // Arrange
        task.assignTo(user);
        task.start();
        when(taskRepository.findById(1L)).thenReturn(Optional.of(task));

        // Act & Assert
        assertThatThrownBy(() -> taskService.unassignTask(1L))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("en cours");
    }

    @Test
    void logHours_WhenPositiveHours_ShouldLogHours() {
        // Arrange
        when(taskRepository.findById(1L)).thenReturn(Optional.of(task));
        when(taskRepository.save(any(Task.class))).thenReturn(task);

        // Act
        Task result = taskService.logHours(1L, 4);

        // Assert
        assertThat(result).isNotNull();
        verify(taskRepository).save(task);
    }

    @Test
    void logHours_WhenNegativeHours_ShouldThrowException() {
        // Arrange
        when(taskRepository.findById(1L)).thenReturn(Optional.of(task));

        // Act & Assert
        assertThatThrownBy(() -> taskService.logHours(1L, -2))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("positif");
    }

    @Test
    void updateEstimatedHours_WhenValidHours_ShouldUpdateHours() {
        // Arrange
        when(taskRepository.findById(1L)).thenReturn(Optional.of(task));
        when(taskRepository.save(any(Task.class))).thenReturn(task);

        // Act
        Task result = taskService.updateEstimatedHours(1L, 10);

        // Assert
        assertThat(result).isNotNull();
        verify(taskRepository).save(task);
    }

    @Test
    void updateEstimatedHours_WhenNegativeHours_ShouldThrowException() {
        // Arrange
        when(taskRepository.findById(1L)).thenReturn(Optional.of(task));

        // Act & Assert
        assertThatThrownBy(() -> taskService.updateEstimatedHours(1L, -5))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("négatives");
    }

    @Test
    void startTask_WhenTaskAssigned_ShouldStartTask() {
        // Arrange
        task.assignTo(user);
        when(taskRepository.findById(1L)).thenReturn(Optional.of(task));
        when(taskRepository.save(any(Task.class))).thenReturn(task);

        // Act
        Task result = taskService.startTask(1L);

        // Assert
        assertThat(result.getStatus()).isEqualTo(WorkItemStatus.IN_PROGRESS);
        verify(taskRepository).save(task);
    }

    @Test
    void startTask_WhenTaskNotAssigned_ShouldThrowException() {
        // Arrange
        when(taskRepository.findById(1L)).thenReturn(Optional.of(task));

        // Act & Assert
        assertThatThrownBy(() -> taskService.startTask(1L))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("assignée");
    }


    @Test
    void moveTaskToTesting_ShouldMoveToTesting() {
        // Arrange
        task.assignTo(user);
        task.start();
        when(taskRepository.findById(1L)).thenReturn(Optional.of(task));
        when(taskRepository.save(any(Task.class))).thenReturn(task);

        // Act
        Task result = taskService.moveTaskToTesting(1L);

        // Assert
        assertThat(result.getStatus()).isEqualTo(WorkItemStatus.TESTING);
        verify(taskRepository).save(task);
    }

    @Test
    void completeTask_ShouldCompleteTask() {
        // Arrange
        task.assignTo(user);
        task.start();
        when(taskRepository.findById(1L)).thenReturn(Optional.of(task));
        when(taskRepository.save(any(Task.class))).thenReturn(task);

        // Act
        Task result = taskService.completeTask(1L);

        // Assert
        assertThat(result.getStatus()).isEqualTo(WorkItemStatus.DONE);
        verify(taskRepository).save(task);
    }

    @Test
    void updateTaskStatus_WhenValidStatus_ShouldUpdateStatus() {
        // Arrange
        when(taskRepository.findById(1L)).thenReturn(Optional.of(task));
        when(taskRepository.save(any(Task.class))).thenReturn(task);

        // Act
        Task result = taskService.updateTaskStatus(1L, WorkItemStatus.IN_PROGRESS);

        // Assert
        assertThat(result).isNotNull();
        verify(taskRepository).save(task);
    }

    @Test
    void updateTaskDescription_ShouldUpdateDescription() {
        // Arrange
        when(taskRepository.findById(1L)).thenReturn(Optional.of(task));
        when(taskRepository.save(any(Task.class))).thenReturn(task);

        // Act
        Task result = taskService.updateTaskDescription(1L, "New description");

        // Assert
        assertThat(result).isNotNull();
        verify(taskRepository).save(task);
    }

    @Test
    void deleteTask_WhenTaskNotDone_ShouldDeleteTask() {
        // Arrange
        when(taskRepository.findById(1L)).thenReturn(Optional.of(task));

        // Act
        taskService.deleteTask(1L);

        // Assert
        verify(taskRepository).delete(task);
    }

    @Test
    void deleteTask_WhenTaskDone_ShouldThrowException() {
        // Arrange
        task.assignTo(user);
        task.start();
        task.complete();
        when(taskRepository.findById(1L)).thenReturn(Optional.of(task));

        // Act & Assert
        assertThatThrownBy(() -> taskService.deleteTask(1L))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("terminée");
    }

    @Test
    void getUnassignedTasksBySprint_ShouldReturnUnassignedTasks() {
        // Arrange
        List<Task> tasks = List.of(task);
        when(taskRepository.findBySprintBacklogIdAndAssignedUserIsNull(1L)).thenReturn(tasks);

        // Act
        List<Task> result = taskService.getUnassignedTasksBySprint(1L);

        // Assert
        assertThat(result).hasSize(1);
    }

    @Test
    void getOverEstimatedTasksBySprint_ShouldReturnOverEstimatedTasks() {
        // Arrange
        List<Task> tasks = List.of(task);
        when(taskRepository.findOverEstimatedTasksBySprint(1L)).thenReturn(tasks);

        // Act
        List<Task> result = taskService.getOverEstimatedTasksBySprint(1L);

        // Assert
        assertThat(result).hasSize(1);
    }

    @Test
    void getUserStoryTaskMetrics_ShouldReturnMetrics() {
        // Arrange
        Task task1 = new Task("Task 1", 8);
        task1.setStatus(WorkItemStatus.DONE);

        Task task2 = new Task("Task 2", 5);
        task2.logHours(3);

        List<Task> tasks = List.of(task1, task2);
        when(taskRepository.findByUserStoryId(1L)).thenReturn(tasks);

        // Act
        TaskService.UserStoryTaskMetrics result = taskService.getUserStoryTaskMetrics(1L);

        // Assert
        assertThat(result.totalTasks()).isEqualTo(2);
        assertThat(result.completedTasks()).isEqualTo(1);
        assertThat(result.totalEstimatedHours()).isEqualTo(13);
        assertThat(result.progressPercentage()).isEqualTo(50.0);
    }

    @Test
    void getUserStoryTaskMetrics_WhenNoTasks_ShouldReturnZeroMetrics() {
        // Arrange
        when(taskRepository.findByUserStoryId(1L)).thenReturn(List.of());

        // Act
        TaskService.UserStoryTaskMetrics result = taskService.getUserStoryTaskMetrics(1L);

        // Assert
        assertThat(result.totalTasks()).isEqualTo(0);
        assertThat(result.progressPercentage()).isEqualTo(0.0);
    }
}