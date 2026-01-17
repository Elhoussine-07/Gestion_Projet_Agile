package com.Agile.demo.execution.services;

import com.Agile.demo.execution.repositories.TaskRepository;
import com.Agile.demo.execution.repositories.UserRepository;
import com.Agile.demo.model.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TaskServiceTest {

    @Mock
    private TaskRepository taskRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private TaskService taskService;

    private Task testTask;
    private UserStory testUserStory;
    private User testUser;

    @BeforeEach
    void setUp() {
        testUserStory = new UserStory();
        testUserStory.setId(1L);
        testUserStory.setTitle("Test User Story");

        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("testuser");
        testUser.setEmail("test@test.com");

        testTask = new Task("Test Task", 8);
        testTask.setId(1L);
        testTask.setUserStory(testUserStory);
        testTask.setStatus(WorkItemStatus.TODO);
    }

    @Test
    void startTask_WithValidData_ShouldStartTask() {
        Long taskId = 1L;
        Long userId = 1L;
        testTask.setAssignedUser(null);

        when(taskRepository.findById(taskId)).thenReturn(Optional.of(testTask));
        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
        when(taskRepository.save(any(Task.class))).thenReturn(testTask);

        taskService.startTask(taskId, userId);

        verify(taskRepository, times(1)).save(testTask);
        verify(userRepository, times(1)).findById(userId);
    }

    @Test
    void startTask_WithNonTodoStatus_ShouldThrowException() {
        Long taskId = 1L;
        Long userId = 1L;
        testTask.setStatus(WorkItemStatus.IN_PROGRESS);

        when(taskRepository.findById(taskId)).thenReturn(Optional.of(testTask));
        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));

        assertThatThrownBy(() -> taskService.startTask(taskId, userId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("TODO");
    }

    @Test
    void startTask_WithBlockedTask_ShouldThrowException() {
        Long taskId = 1L;
        Long userId = 1L;
        testTask.setStatus(WorkItemStatus.IN_PROGRESS);

        when(taskRepository.findById(taskId)).thenReturn(Optional.of(testTask));
        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
        when(taskRepository.save(any(Task.class))).thenReturn(testTask);

        taskService.blockTask(taskId, "Test block reason");

        testTask.setStatus(WorkItemStatus.TODO);

        assertThatThrownBy(() -> taskService.startTask(taskId, userId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("bloquée");
    }

    @Test
    void startTask_WithDifferentAssignedUser_ShouldThrowException() {
        Long taskId = 1L;
        Long userId = 2L;
        User anotherUser = new User();
        anotherUser.setId(3L);
        anotherUser.setUsername("anotheruser");
        testTask.setAssignedUser(anotherUser);

        when(taskRepository.findById(taskId)).thenReturn(Optional.of(testTask));
        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));

        assertThatThrownBy(() -> taskService.startTask(taskId, userId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("déjà assignée");
    }

    @Test
    void moveToReview_WithInProgressTask_ShouldMoveToReview() {
        Long taskId = 1L;
        testTask.setStatus(WorkItemStatus.IN_PROGRESS);
        testTask.setActualHours(2);

        when(taskRepository.findById(taskId)).thenReturn(Optional.of(testTask));
        when(taskRepository.save(any(Task.class))).thenReturn(testTask);

        taskService.moveToReview(taskId);

        verify(taskRepository, times(1)).save(testTask);
    }

    @Test
    void moveToReview_WithNoHoursLogged_ShouldThrowException() {
        Long taskId = 1L;
        testTask.setStatus(WorkItemStatus.IN_PROGRESS);
        testTask.setActualHours(0);

        when(taskRepository.findById(taskId)).thenReturn(Optional.of(testTask));

        assertThatThrownBy(() -> taskService.moveToReview(taskId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("heure");
    }

    @Test
    void moveToReview_WithWrongStatus_ShouldThrowException() {
        Long taskId = 1L;
        testTask.setStatus(WorkItemStatus.TODO);

        when(taskRepository.findById(taskId)).thenReturn(Optional.of(testTask));

        assertThatThrownBy(() -> taskService.moveToReview(taskId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("en cours");
    }

    @Test
    void moveToTesting_WithInReviewStatus_ShouldMoveToTesting() {
        Long taskId = 1L;
        testTask.setStatus(WorkItemStatus.IN_REVIEW);

        when(taskRepository.findById(taskId)).thenReturn(Optional.of(testTask));
        when(taskRepository.save(any(Task.class))).thenReturn(testTask);

        taskService.moveToTesting(taskId);

        verify(taskRepository, times(1)).save(testTask);
    }

    @Test
    void moveToTesting_WithWrongStatus_ShouldThrowException() {
        Long taskId = 1L;
        testTask.setStatus(WorkItemStatus.IN_PROGRESS);

        when(taskRepository.findById(taskId)).thenReturn(Optional.of(testTask));

        assertThatThrownBy(() -> taskService.moveToTesting(taskId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("revue");
    }

    @Test
    void completeTask_WithValidStatus_ShouldCompleteTask() {
        Long taskId = 1L;
        testTask.setStatus(WorkItemStatus.TESTING);

        when(taskRepository.findById(taskId)).thenReturn(Optional.of(testTask));
        when(taskRepository.save(any(Task.class))).thenReturn(testTask);

        taskService.completeTask(taskId);

        verify(taskRepository, times(1)).save(testTask);
    }

    @Test
    void completeTask_WithBlockedTask_ShouldThrowException() {
        Long taskId = 1L;
        testTask.setStatus(WorkItemStatus.IN_PROGRESS);

        when(taskRepository.findById(taskId)).thenReturn(Optional.of(testTask));
        when(taskRepository.save(any(Task.class))).thenReturn(testTask);

        taskService.blockTask(taskId, "Test reason");

        assertThatThrownBy(() -> taskService.completeTaskWorkflow(taskId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("bloquée");
    }

    @Test
    void completeTask_WithInvalidStatus_ShouldThrowException() {
        Long taskId = 1L;
        testTask.setStatus(WorkItemStatus.TODO);

        when(taskRepository.findById(taskId)).thenReturn(Optional.of(testTask));

        assertThatThrownBy(() -> taskService.completeTask(taskId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("testée avant");
    }

    @Test
    void blockTask_WithInProgressTask_ShouldBlockTask() {
        Long taskId = 1L;
        String reason = "Waiting for dependencies";
        testTask.setStatus(WorkItemStatus.IN_PROGRESS);
        testTask.setAssignedUser(testUser);

        when(taskRepository.findById(taskId)).thenReturn(Optional.of(testTask));
        when(taskRepository.save(any(Task.class))).thenReturn(testTask);

        taskService.blockTask(taskId, reason);

        verify(taskRepository, times(1)).save(testTask);
        assertThat(taskService.isTaskBlocked(taskId)).isTrue();
    }

    @Test
    void blockTask_WithDoneTask_ShouldThrowException() {
        Long taskId = 1L;
        testTask.setStatus(WorkItemStatus.DONE);

        when(taskRepository.findById(taskId)).thenReturn(Optional.of(testTask));

        assertThatThrownBy(() -> taskService.blockTask(taskId, "Reason"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("terminée");
    }

    @Test
    void blockTask_WithTodoTask_ShouldThrowException() {
        Long taskId = 1L;
        testTask.setStatus(WorkItemStatus.TODO);

        when(taskRepository.findById(taskId)).thenReturn(Optional.of(testTask));

        assertThatThrownBy(() -> taskService.blockTask(taskId, "Reason"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("pas encore démarré");
    }

    @Test
    void blockTask_WithEmptyReason_ShouldThrowException() {
        Long taskId = 1L;
        testTask.setStatus(WorkItemStatus.IN_PROGRESS);

        when(taskRepository.findById(taskId)).thenReturn(Optional.of(testTask));

        assertThatThrownBy(() -> taskService.blockTask(taskId, "   "))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("raison de blocage");
    }

    @Test
    void blockTask_WithAlreadyBlockedTask_ShouldThrowException() {
        Long taskId = 1L;
        testTask.setStatus(WorkItemStatus.IN_PROGRESS);
        testTask.setBlocked(true);

        when(taskRepository.findById(taskId)).thenReturn(Optional.of(testTask));

        assertThatThrownBy(() -> taskService.blockTask(taskId, "Reason"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("déjà bloquée");
    }

    @Test
    void unblockTask_WithBlockedTask_ShouldUnblockTask() {
        Long taskId = 1L;
        testTask.setStatus(WorkItemStatus.IN_PROGRESS);

        when(taskRepository.findById(taskId)).thenReturn(Optional.of(testTask));
        when(taskRepository.save(any(Task.class))).thenReturn(testTask);

        taskService.blockTask(taskId, "Test reason");
        taskService.unblockTask(taskId);

        verify(taskRepository, times(2)).save(testTask);
        assertThat(taskService.isTaskBlocked(taskId)).isFalse();
    }

    @Test
    void unblockTask_WithNotBlockedTask_ShouldThrowException() {
        Long taskId = 1L;
        testTask.setBlocked(false);

        when(taskRepository.findById(taskId)).thenReturn(Optional.of(testTask));

        assertThatThrownBy(() -> taskService.unblockTask(taskId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("n'est pas bloquée");
    }

    @Test
    void isTaskBlocked_WithBlockedTask_ShouldReturnTrue() {
        Long taskId = 1L;
        testTask.setStatus(WorkItemStatus.IN_PROGRESS);

        when(taskRepository.findById(taskId)).thenReturn(Optional.of(testTask));
        when(taskRepository.save(any(Task.class))).thenReturn(testTask);

        taskService.blockTask(taskId, "Test reason");

        assertThat(taskService.isTaskBlocked(taskId)).isTrue();
    }

    @Test
    void isTaskBlocked_WithNonBlockedTask_ShouldReturnFalse() {
        Long taskId = 1L;

        assertThat(taskService.isTaskBlocked(taskId)).isFalse();
    }

    @Test
    void getTaskBlockInfo_WithBlockedTask_ShouldReturnInfo() {
        Long taskId = 1L;
        String reason = "Test reason";
        testTask.setStatus(WorkItemStatus.IN_PROGRESS);
        testTask.setAssignedUser(testUser);

        when(taskRepository.findById(taskId)).thenReturn(Optional.of(testTask));
        when(taskRepository.save(any(Task.class))).thenReturn(testTask);

        taskService.blockTask(taskId, reason);
        TaskService.TaskBlockInfo info = taskService.getTaskBlockInfo(taskId);

        assertThat(info).isNotNull();
        assertThat(info.reason()).isEqualTo(reason);
        assertThat(info.blockedBy()).isEqualTo(testUser.getUsername());
    }

    @Test
    void getTaskBlockInfo_WithNonBlockedTask_ShouldReturnNull() {
        Long taskId = 999L;

        TaskService.TaskBlockInfo info = taskService.getTaskBlockInfo(taskId);

        assertThat(info).isNull();
    }

    @Test
    void reassignTask_WithValidData_ShouldReassignTask() {
        Long taskId = 1L;
        Long newUserId = 2L;
        User newUser = new User();
        newUser.setId(newUserId);
        newUser.setUsername("newuser");
        testTask.setStatus(WorkItemStatus.TODO);

        when(taskRepository.findById(taskId)).thenReturn(Optional.of(testTask));
        when(userRepository.findById(newUserId)).thenReturn(Optional.of(newUser));
        when(taskRepository.save(any(Task.class))).thenReturn(testTask);

        taskService.reassignTask(taskId, newUserId);

        verify(taskRepository, times(1)).save(testTask);
    }

    @Test
    void reassignTask_WithDoneTask_ShouldThrowException() {
        Long taskId = 1L;
        Long newUserId = 2L;
        testTask.setStatus(WorkItemStatus.DONE);

        when(taskRepository.findById(taskId)).thenReturn(Optional.of(testTask));
        when(userRepository.findById(newUserId)).thenReturn(Optional.of(testUser));

        assertThatThrownBy(() -> taskService.reassignTask(taskId, newUserId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("terminée");
    }

    @Test
    void reassignTask_WithNonExistentUser_ShouldThrowException() {
        Long taskId = 1L;
        Long userId = 999L;

        when(taskRepository.findById(taskId)).thenReturn(Optional.of(testTask));
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> taskService.reassignTask(taskId, userId))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Utilisateur non trouvé");
    }

    @Test
    void moveTaskBackward_FromInReviewToInProgress_ShouldMoveBack() {
        Long taskId = 1L;
        String reason = "Review failed";
        testTask.setStatus(WorkItemStatus.IN_REVIEW);

        when(taskRepository.findById(taskId)).thenReturn(Optional.of(testTask));
        when(taskRepository.save(any(Task.class))).thenReturn(testTask);

        taskService.moveTaskBackward(taskId, reason);

        verify(taskRepository, times(1)).save(testTask);
    }

    @Test
    void moveTaskBackward_FromTestingToInReview_ShouldMoveBack() {
        Long taskId = 1L;
        String reason = "Test failed";
        testTask.setStatus(WorkItemStatus.TESTING);

        when(taskRepository.findById(taskId)).thenReturn(Optional.of(testTask));
        when(taskRepository.save(any(Task.class))).thenReturn(testTask);

        taskService.moveTaskBackward(taskId, reason);

        verify(taskRepository, times(1)).save(testTask);
    }

    @Test
    void moveTaskBackward_WithDoneTask_ShouldThrowException() {
        Long taskId = 1L;
        testTask.setStatus(WorkItemStatus.DONE);

        when(taskRepository.findById(taskId)).thenReturn(Optional.of(testTask));

        assertThatThrownBy(() -> taskService.moveTaskBackward(taskId, "Reason"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("terminée");
    }

    @Test
    void moveTaskBackward_WithInvalidStatus_ShouldThrowException() {
        Long taskId = 1L;
        testTask.setStatus(WorkItemStatus.TODO);

        when(taskRepository.findById(taskId)).thenReturn(Optional.of(testTask));

        assertThatThrownBy(() -> taskService.moveTaskBackward(taskId, "Reason"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Impossible de faire reculer");
    }
}