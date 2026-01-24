package com.Agile.demo.execution.services;

import com.Agile.demo.execution.dto.task.TaskResponseDTO;
import com.Agile.demo.execution.dto.mapper.TaskMapper;
import com.Agile.demo.execution.dto.task.BlockTaskRequest;
import com.Agile.demo.execution.repositories.TaskRepository;
import com.Agile.demo.execution.repositories.UserRepository;
import com.Agile.demo.planning.repository.UserStoryRepository;
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

    @Mock
    private UserStoryRepository userStoryRepository;

    @Mock
    private TaskMapper taskMapper;

    @InjectMocks
    private TaskService taskService;

    private Task testTask;
    private UserStory testUserStory;
    private User testUser;
    private TaskResponseDTO testResponseDTO;

    @BeforeEach
    void setUp() {
        testUserStory = new UserStory();
        testUserStory.setId(1L);
        testUserStory.setTitle("Test User Story");
        testUserStory.setStatus(WorkItemStatus.TODO);

        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("testuser");
        testUser.setEmail("test@test.com");

        testTask = new Task("Test Task", 8);
        testTask.setId(1L);
        testTask.setUserStory(testUserStory);
        testTask.setStatus(WorkItemStatus.TODO);

        testResponseDTO = new TaskResponseDTO();
        testResponseDTO.setId(1L);
        testResponseDTO.setTitle("Test Task");
    }

    // ==================== START TASK ====================

    @Test
    void startTask_WithValidData_ShouldStartTask() {
        Long taskId = 1L;
        Long userId = 1L;
        testTask.setAssignedUser(null);

        when(taskRepository.findById(taskId)).thenReturn(Optional.of(testTask));
        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
        when(taskRepository.save(any(Task.class))).thenReturn(testTask);
        when(taskMapper.toResponseDTO(any(Task.class))).thenReturn(testResponseDTO);

        taskService.startTask(taskId, userId);

        verify(taskRepository, times(1)).save(testTask);
    }

    @Test
    void startTask_WithBlockedTask_ShouldThrowException() {
        Long taskId = 1L;
        Long userId = 1L;

        // La tâche doit être IN_PROGRESS pour pouvoir être bloquée
        testTask.setStatus(WorkItemStatus.IN_PROGRESS);
        testTask.setAssignedUser(testUser);

        BlockTaskRequest blockRequest = new BlockTaskRequest();
        blockRequest.setReason("Blocage test");

        when(taskRepository.findById(taskId)).thenReturn(Optional.of(testTask));
        when(taskRepository.save(any(Task.class))).thenReturn(testTask);
        when(taskMapper.toResponseDTO(any(Task.class))).thenReturn(testResponseDTO);

        // Bloquer la tâche
        taskService.blockTask(taskId, blockRequest);

        // Réinitialiser le statut à TODO pour tester le démarrage
        testTask.setStatus(WorkItemStatus.TODO);
        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));

        // Vérifier que le démarrage échoue à cause du blocage
        assertThatThrownBy(() -> taskService.startTask(taskId, userId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("bloquée");
    }

    // ==================== WORKFLOW MOVEMENT ====================

    @Test
    void moveTaskToReview_WithInProgressTask_ShouldMoveToReview() {
        Long taskId = 1L;
        testTask.setStatus(WorkItemStatus.IN_PROGRESS);
        testTask.setActualHours(2);

        when(taskRepository.findById(taskId)).thenReturn(Optional.of(testTask));
        when(taskRepository.save(any(Task.class))).thenReturn(testTask);
        when(taskMapper.toResponseDTO(any(Task.class))).thenReturn(testResponseDTO);

        taskService.moveTaskToReview(taskId);

        verify(taskRepository, times(1)).save(testTask);
        assertThat(testTask.getStatus()).isEqualTo(WorkItemStatus.IN_REVIEW);
    }

    @Test
    void completeTask_WithBlockedTask_ShouldThrowException() {
        Long taskId = 1L;
        testTask.setStatus(WorkItemStatus.IN_PROGRESS);
        testTask.setAssignedUser(testUser);

        BlockTaskRequest request = new BlockTaskRequest();
        request.setReason("Blocage critique");

        when(taskRepository.findById(taskId)).thenReturn(Optional.of(testTask));
        when(taskRepository.save(any(Task.class))).thenReturn(testTask);
        when(taskMapper.toResponseDTO(any(Task.class))).thenReturn(testResponseDTO);

        // Bloquer la tâche
        taskService.blockTask(taskId, request);

        // Tenter de compléter la tâche bloquée
        assertThatThrownBy(() -> taskService.completeTask(taskId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("bloquée");
    }

    // ==================== BLOCK TASK ====================

    @Test
    void blockTask_WithInProgressTask_ShouldBlockTask() {
        Long taskId = 1L;
        testTask.setStatus(WorkItemStatus.IN_PROGRESS);
        testTask.setAssignedUser(testUser);
        testTask.setBlocked(false); // Pas encore bloquée

        BlockTaskRequest request = new BlockTaskRequest();
        request.setReason("Waiting for dependencies");

        when(taskRepository.findById(taskId)).thenReturn(Optional.of(testTask));
        when(taskRepository.save(any(Task.class))).thenReturn(testTask);
        when(taskMapper.toResponseDTO(any(Task.class))).thenReturn(testResponseDTO);

        taskService.blockTask(taskId, request);

        verify(taskRepository, times(1)).save(testTask);
        assertThat(taskService.isTaskBlocked(taskId)).isTrue();
        assertThat(testTask.isBlocked()).isTrue();
        assertThat(testTask.getBlockReason()).isEqualTo("Waiting for dependencies");
    }

    @Test
    void blockTask_WithDoneTask_ShouldThrowException() {
        Long taskId = 1L;
        testTask.setStatus(WorkItemStatus.DONE);

        BlockTaskRequest request = new BlockTaskRequest();
        request.setReason("Raison quelconque");

        when(taskRepository.findById(taskId)).thenReturn(Optional.of(testTask));

        assertThatThrownBy(() -> taskService.blockTask(taskId, request))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("terminée");
    }

    @Test
    void blockTask_WithTodoTask_ShouldThrowException() {
        Long taskId = 1L;
        testTask.setStatus(WorkItemStatus.TODO);

        BlockTaskRequest request = new BlockTaskRequest();
        request.setReason("Raison");

        when(taskRepository.findById(taskId)).thenReturn(Optional.of(testTask));

        assertThatThrownBy(() -> taskService.blockTask(taskId, request))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("non démarrée");
    }

    @Test
    void blockTask_WithAlreadyBlockedTask_ShouldThrowException() {
        Long taskId = 1L;
        testTask.setStatus(WorkItemStatus.IN_PROGRESS);
        testTask.setBlocked(true); // Déjà marquée bloquée

        BlockTaskRequest request = new BlockTaskRequest();
        request.setReason("Raison");

        when(taskRepository.findById(taskId)).thenReturn(Optional.of(testTask));

        assertThatThrownBy(() -> taskService.blockTask(taskId, request))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("déjà bloquée");
    }

    @Test
    void unblockTask_WithBlockedTask_ShouldUnblockTask() {
        Long taskId = 1L;
        testTask.setStatus(WorkItemStatus.IN_PROGRESS);
        testTask.setBlocked(false);
        testTask.setAssignedUser(testUser);

        BlockTaskRequest request = new BlockTaskRequest();
        request.setReason("Temp block");

        when(taskRepository.findById(taskId)).thenReturn(Optional.of(testTask));
        when(taskRepository.save(any(Task.class))).thenReturn(testTask);
        when(taskMapper.toResponseDTO(any(Task.class))).thenReturn(testResponseDTO);

        // Bloquer la tâche
        taskService.blockTask(taskId, request);

        // Vérifier qu'elle est bien bloquée
        assertThat(taskService.isTaskBlocked(taskId)).isTrue();

        // Débloquer la tâche
        taskService.unblockTask(taskId);

        // Vérifications
        verify(taskRepository, times(2)).save(testTask);
        assertThat(taskService.isTaskBlocked(taskId)).isFalse();
        assertThat(testTask.isBlocked()).isFalse();
        assertThat(testTask.getBlockReason()).isNull();
    }

    @Test
    void isTaskBlocked_WithBlockedTask_ShouldReturnTrue() {
        Long taskId = 1L;
        testTask.setStatus(WorkItemStatus.IN_PROGRESS);
        testTask.setBlocked(false);
        testTask.setAssignedUser(testUser);

        BlockTaskRequest request = new BlockTaskRequest();
        request.setReason("Test reason");

        when(taskRepository.findById(taskId)).thenReturn(Optional.of(testTask));
        when(taskRepository.save(any(Task.class))).thenReturn(testTask);
        when(taskMapper.toResponseDTO(any(Task.class))).thenReturn(testResponseDTO);

        taskService.blockTask(taskId, request);

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
        String reasonStr = "Test reason";
        testTask.setStatus(WorkItemStatus.IN_PROGRESS);
        testTask.setAssignedUser(testUser);
        testTask.setBlocked(false);

        BlockTaskRequest request = new BlockTaskRequest();
        request.setReason(reasonStr);

        when(taskRepository.findById(taskId)).thenReturn(Optional.of(testTask));
        when(taskRepository.save(any(Task.class))).thenReturn(testTask);
        when(taskMapper.toResponseDTO(any(Task.class))).thenReturn(testResponseDTO);

        taskService.blockTask(taskId, request);

        TaskService.TaskBlockInfo info = taskService.getTaskBlockInfo(taskId);

        assertThat(info).isNotNull();
        assertThat(info.reason()).isEqualTo(reasonStr);
        assertThat(info.blockedBy()).isEqualTo("testuser");
        assertThat(info.blockedAt()).isNotNull();
    }

    @Test
    void getTaskBlockInfo_WithNonBlockedTask_ShouldReturnNull() {
        Long taskId = 1L;

        TaskService.TaskBlockInfo info = taskService.getTaskBlockInfo(taskId);

        assertThat(info).isNull();
    }

    // ==================== ADDITIONAL VALIDATION TESTS ====================

    @Test
    void blockTask_WithInReviewTask_ShouldBlockTask() {
        Long taskId = 1L;
        testTask.setStatus(WorkItemStatus.IN_REVIEW);
        testTask.setBlocked(false);
        testTask.setAssignedUser(testUser);

        BlockTaskRequest request = new BlockTaskRequest();
        request.setReason("Found issues in review");

        when(taskRepository.findById(taskId)).thenReturn(Optional.of(testTask));
        when(taskRepository.save(any(Task.class))).thenReturn(testTask);
        when(taskMapper.toResponseDTO(any(Task.class))).thenReturn(testResponseDTO);

        taskService.blockTask(taskId, request);

        verify(taskRepository, times(1)).save(testTask);
        assertThat(taskService.isTaskBlocked(taskId)).isTrue();
    }

    @Test
    void unblockTask_WithNonBlockedTask_ShouldThrowException() {
        Long taskId = 1L;
        testTask.setStatus(WorkItemStatus.IN_PROGRESS);
        testTask.setBlocked(false);

        when(taskRepository.findById(taskId)).thenReturn(Optional.of(testTask));

        assertThatThrownBy(() -> taskService.unblockTask(taskId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("n'est pas bloquée");
    }
}