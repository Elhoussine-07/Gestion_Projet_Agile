package com.Agile.demo.execution.services;

import com.Agile.demo.execution.repositories.UserRepository;
import com.Agile.demo.model.Role;
import com.Agile.demo.model.User;
import com.Agile.demo.model.WorkItemStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService userService;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("testuser");
        testUser.setEmail("test@example.com");
        testUser.setPassword("encodedPassword");
        testUser.setRole(Role.DEVELOPER);
        testUser.setFirstName("Test");
        testUser.setLastName("User");
        testUser.setPhoneNumber("1234567890");
        testUser.setisActive(true);
    }

    @Test
    void createUser_WithValidData_ShouldCreateUser() {
        // Arrange
        String username = "newuser";
        String email = "newuser@example.com";
        String password = "Password123";
        Role role = Role.DEVELOPER;

        when(userRepository.existsByUsername(username)).thenReturn(false);
        when(userRepository.existsByEmail(email)).thenReturn(false);
        when(passwordEncoder.encode(password)).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        // Act
        User result = userService.createUser(username, email, password, role,
                "First", "Last", "1234567890");

        // Assert
        assertThat(result).isNotNull();
        verify(passwordEncoder, times(1)).encode(password);
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    void createUser_WithExistingUsername_ShouldThrowException() {
        // Arrange
        String username = "existinguser";
        when(userRepository.existsByUsername(username)).thenReturn(true);

        // Act & Assert
        assertThatThrownBy(() -> userService.createUser(username, "email@test.com",
                "password", Role.DEVELOPER, "First", "Last", "1234567890"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("existe déjà");
    }

    @Test
    void createUser_WithExistingEmail_ShouldThrowException() {
        // Arrange
        String email = "existing@example.com";
        when(userRepository.existsByUsername(anyString())).thenReturn(false);
        when(userRepository.existsByEmail(email)).thenReturn(true);

        // Act & Assert
        assertThatThrownBy(() -> userService.createUser("newuser", email,
                "password", Role.DEVELOPER, "First", "Last", "1234567890"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("existe déjà");
    }

    @Test
    void createUser_WithInvalidEmail_ShouldThrowException() {
        // Arrange
        String invalidEmail = "invalid-email";
        when(userRepository.existsByUsername(anyString())).thenReturn(false);
        when(userRepository.existsByEmail(anyString())).thenReturn(false);

        // Act & Assert
        assertThatThrownBy(() -> userService.createUser("newuser", invalidEmail,
                "password", Role.DEVELOPER, "First", "Last", "1234567890"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("invalide");
    }

    @Test
    void getUserById_WithValidId_ShouldReturnUser() {
        // Arrange
        Long userId = 1L;
        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));

        // Act
        User result = userService.getUserById(userId);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(userId);
        verify(userRepository, times(1)).findById(userId);
    }

    @Test
    void getUserById_WithInvalidId_ShouldThrowException() {
        // Arrange
        Long userId = 999L;
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> userService.getUserById(userId))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("non trouvé");
    }

    @Test
    void getUserByUsername_WithValidUsername_ShouldReturnUser() {
        // Arrange
        String username = "testuser";
        when(userRepository.findByUsername(username)).thenReturn(Optional.of(testUser));

        // Act
        User result = userService.getUserByUsername(username);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getUsername()).isEqualTo(username);
        verify(userRepository, times(1)).findByUsername(username);
    }

    @Test
    void getUserByUsername_WithInvalidUsername_ShouldThrowException() {
        // Arrange
        String username = "nonexistent";
        when(userRepository.findByUsername(username)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> userService.getUserByUsername(username))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("non trouvé");
    }

    @Test
    void getUserByEmail_WithValidEmail_ShouldReturnUser() {
        // Arrange
        String email = "test@example.com";
        when(userRepository.findByEmail(email)).thenReturn(Optional.of(testUser));

        // Act
        User result = userService.getUserByEmail(email);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getEmail()).isEqualTo(email);
        verify(userRepository, times(1)).findByEmail(email);
    }

    @Test
    void getAllUsers_ShouldReturnAllUsers() {
        // Arrange
        List<User> users = Arrays.asList(testUser);
        when(userRepository.findAll()).thenReturn(users);

        // Act
        List<User> result = userService.getAllUsers();

        // Assert
        assertThat(result).hasSize(1);
        assertThat(result.get(0)).isEqualTo(testUser);
        verify(userRepository, times(1)).findAll();
    }

    @Test
    void getUsersByRole_ShouldReturnUsersByRole() {
        // Arrange
        Role role = Role.DEVELOPER;
        List<User> users = Arrays.asList(testUser);
        when(userRepository.findByRole(role)).thenReturn(users);

        // Act
        List<User> result = userService.getUsersByRole(role);

        // Assert
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getRole()).isEqualTo(role);
        verify(userRepository, times(1)).findByRole(role);
    }

    @Test
    void getUsersByProject_ShouldReturnProjectUsers() {
        // Arrange
        Long projectId = 1L;
        List<User> users = Arrays.asList(testUser);
        when(userRepository.findUsersByProjectId(projectId)).thenReturn(users);

        // Act
        List<User> result = userService.getUsersByProject(projectId);

        // Assert
        assertThat(result).hasSize(1);
        verify(userRepository, times(1)).findUsersByProjectId(projectId);
    }

    @Test
    void updateUser_WithValidData_ShouldUpdateUser() {
        // Arrange
        Long userId = 1L;
        String newEmail = "newemail@example.com";
        Role newRole = Role.PRODUCT_OWNER;

        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
        when(userRepository.existsByEmail(newEmail)).thenReturn(false);
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        // Act
        User result = userService.updateUser(userId, newEmail, newRole,
                "NewFirst", "NewLast", "9876543210", true);

        // Assert
        assertThat(result).isNotNull();
        verify(userRepository, times(1)).save(testUser);
    }

    @Test
    void updateUser_WithExistingEmail_ShouldThrowException() {
        // Arrange
        Long userId = 1L;
        String existingEmail = "existing@example.com";

        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
        when(userRepository.existsByEmail(existingEmail)).thenReturn(true);

        // Act & Assert
        assertThatThrownBy(() -> userService.updateUser(userId, existingEmail,
                null, null, null, null, null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("existe déjà");
    }

    @Test
    void updatePassword_WithCorrectCurrentPassword_ShouldUpdatePassword() {
        // Arrange
        Long userId = 1L;
        String currentPassword = "OldPassword123";
        String newPassword = "NewPassword123";

        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches(currentPassword, testUser.getPassword())).thenReturn(true);
        when(passwordEncoder.encode(newPassword)).thenReturn("encodedNewPassword");
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        // Act
        User result = userService.updatePassword(userId, currentPassword, newPassword);

        // Assert
        assertThat(result).isNotNull();
        verify(passwordEncoder, times(1)).encode(newPassword);
        verify(userRepository, times(1)).save(testUser);
    }

    @Test
    void updatePassword_WithIncorrectCurrentPassword_ShouldThrowException() {
        // Arrange
        Long userId = 1L;
        String currentPassword = "WrongPassword";
        String newPassword = "NewPassword123";

        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches(currentPassword, testUser.getPassword())).thenReturn(false);

        // Act & Assert
        assertThatThrownBy(() -> userService.updatePassword(userId, currentPassword, newPassword))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("incorrect");
    }

    @Test
    void updatePassword_WithShortPassword_ShouldThrowException() {
        // Arrange
        Long userId = 1L;
        String currentPassword = "OldPassword123";
        String shortPassword = "short";

        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches(currentPassword, testUser.getPassword())).thenReturn(true);

        // Act & Assert
        assertThatThrownBy(() -> userService.updatePassword(userId, currentPassword, shortPassword))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("8 caractères");
    }

    @Test
    void deleteUser_WithNoActiveTasks_ShouldDeleteUser() {
        // Arrange
        Long userId = 1L;
        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
        when(userRepository.countTasksByUserAndStatus(userId, WorkItemStatus.IN_PROGRESS))
                .thenReturn(0L);

        // Act
        userService.deleteUser(userId);

        // Assert
        verify(userRepository, times(1)).delete(testUser);
    }

    @Test
    void deleteUser_WithActiveTasks_ShouldThrowException() {
        // Arrange
        Long userId = 1L;
        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
        when(userRepository.countTasksByUserAndStatus(userId, WorkItemStatus.IN_PROGRESS))
                .thenReturn(2L);

        // Act & Assert
        assertThatThrownBy(() -> userService.deleteUser(userId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("tâches en cours");
    }

    @Test
    void getAvailableUsers_ShouldReturnAvailableUsers() {
        // Arrange
        Role role = Role.DEVELOPER;
        long maxTasks = 5;
        List<User> users = Arrays.asList(testUser);
        when(userRepository.findAvailableUsersByRole(role, maxTasks)).thenReturn(users);

        // Act
        List<User> result = userService.getAvailableUsers(role, maxTasks);

        // Assert
        assertThat(result).hasSize(1);
        verify(userRepository, times(1)).findAvailableUsersByRole(role, maxTasks);
    }

    @Test
    void countUserActiveTasks_ShouldReturnTaskCount() {
        // Arrange
        Long userId = 1L;
        when(userRepository.countTasksByUserAndStatus(userId, WorkItemStatus.IN_PROGRESS))
                .thenReturn(3L);
        when(userRepository.countTasksByUserAndStatus(userId, WorkItemStatus.TODO))
                .thenReturn(2L);

        // Act
        long result = userService.countUserActiveTasks(userId);

        // Assert
        assertThat(result).isEqualTo(5L);
    }

    @Test
    void userExists_WithExistingUser_ShouldReturnTrue() {
        // Arrange
        Long userId = 1L;
        when(userRepository.existsById(userId)).thenReturn(true);

        // Act
        boolean result = userService.userExists(userId);

        // Assert
        assertThat(result).isTrue();
        verify(userRepository, times(1)).existsById(userId);
    }

    @Test
    void usernameExists_WithExistingUsername_ShouldReturnTrue() {
        // Arrange
        String username = "testuser";
        when(userRepository.existsByUsername(username)).thenReturn(true);

        // Act
        boolean result = userService.usernameExists(username);

        // Assert
        assertThat(result).isTrue();
        verify(userRepository, times(1)).existsByUsername(username);
    }

    @Test
    void emailExists_WithExistingEmail_ShouldReturnTrue() {
        // Arrange
        String email = "test@example.com";
        when(userRepository.existsByEmail(email)).thenReturn(true);

        // Act
        boolean result = userService.emailExists(email);

        // Assert
        assertThat(result).isTrue();
        verify(userRepository, times(1)).existsByEmail(email);
    }

    @Test
    void getUserStatistics_ShouldReturnStatistics() {
        // Arrange
        Long userId = 1L;
        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
        when(userRepository.countTasksByUserAndStatus(userId, WorkItemStatus.TODO))
                .thenReturn(2L);
        when(userRepository.countTasksByUserAndStatus(userId, WorkItemStatus.IN_PROGRESS))
                .thenReturn(3L);
        when(userRepository.countTasksByUserAndStatus(userId, WorkItemStatus.DONE))
                .thenReturn(5L);

        // Act
        UserService.UserStatistics result = userService.getUserStatistics(userId);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.totalTasks()).isEqualTo(10);
        assertThat(result.completedTasks()).isEqualTo(5);
        assertThat(result.completionRate()).isEqualTo(50.0);
    }

    @Test
    void activateUser_ShouldActivateUser() {
        // Arrange
        Long userId = 1L;
        testUser.setisActive(false);
        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        // Act
        User result = userService.activateUser(userId);

        // Assert
        assertThat(result).isNotNull();
        verify(userRepository, times(1)).save(testUser);
    }

    @Test
    void deactivateUser_WithNoActiveTasks_ShouldDeactivateUser() {
        // Arrange
        Long userId = 1L;
        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
        when(userRepository.countTasksByUserAndStatus(userId, WorkItemStatus.IN_PROGRESS))
                .thenReturn(0L);
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        // Act
        User result = userService.deactivateUser(userId);

        // Assert
        assertThat(result).isNotNull();
        verify(userRepository, times(1)).save(testUser);
    }

    @Test
    void deactivateUser_WithActiveTasks_ShouldThrowException() {
        // Arrange
        Long userId = 1L;
        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
        when(userRepository.countTasksByUserAndStatus(userId, WorkItemStatus.IN_PROGRESS))
                .thenReturn(2L);

        // Act & Assert
        assertThatThrownBy(() -> userService.deactivateUser(userId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("tâches en cours");
    }

    @Test
    void getActiveUsers_ShouldReturnActiveUsers() {
        // Arrange
        List<User> users = Arrays.asList(testUser);
        when(userRepository.findByIsActiveTrue()).thenReturn(users);

        // Act
        List<User> result = userService.getActiveUsers();

        // Assert
        assertThat(result).hasSize(1);
        verify(userRepository, times(1)).findByIsActiveTrue();
    }

    @Test
    void getActiveUsersByRole_ShouldReturnActiveUsersByRole() {
        // Arrange
        Role role = Role.DEVELOPER;
        List<User> users = Arrays.asList(testUser);
        when(userRepository.findByRoleAndIsActiveTrue(role)).thenReturn(users);

        // Act
        List<User> result = userService.getActiveUsersByRole(role);

        // Assert
        assertThat(result).hasSize(1);
        verify(userRepository, times(1)).findByRoleAndIsActiveTrue(role);
    }

    @Test
    void searchUsers_ShouldReturnMatchingUsers() {
        // Arrange
        String searchTerm = "test";
        List<User> users = Arrays.asList(testUser);
        when(userRepository.findByUsernameContainingIgnoreCaseOrEmailContainingIgnoreCase(
                searchTerm, searchTerm)).thenReturn(users);

        // Act
        List<User> result = userService.searchUsers(searchTerm);

        // Assert
        assertThat(result).hasSize(1);
        verify(userRepository, times(1))
                .findByUsernameContainingIgnoreCaseOrEmailContainingIgnoreCase(searchTerm, searchTerm);
    }

    @Test
    void getAvailableDevelopers_ShouldReturnAvailableDevelopers() {
        // Arrange
        int maxActiveTasks = 5;
        List<User> users = Arrays.asList(testUser);
        when(userRepository.findAvailableUsersByRole(Role.DEVELOPER, maxActiveTasks))
                .thenReturn(users);

        // Act
        List<User> result = userService.getAvailableDevelopers(maxActiveTasks);

        // Assert
        assertThat(result).hasSize(1);
        verify(userRepository, times(1))
                .findAvailableUsersByRole(Role.DEVELOPER, maxActiveTasks);
    }

    @Test
    void getMostLoadedUsers_ShouldReturnMostLoadedUsers() {
        // Arrange
        int limit = 5;
        List<User> users = Arrays.asList(testUser);
        when(userRepository.findAll()).thenReturn(users);
        when(userRepository.countTasksByUserAndStatus(anyLong(), eq(WorkItemStatus.IN_PROGRESS)))
                .thenReturn(2L);
        when(userRepository.countTasksByUserAndStatus(anyLong(), eq(WorkItemStatus.TODO)))
                .thenReturn(1L);

        // Act
        List<UserService.UserWorkload> result = userService.getMostLoadedUsers(limit);

        // Assert
        assertThat(result).isNotEmpty();
    }

    @Test
    void getTeamWorkload_ShouldReturnTeamWorkload() {
        // Arrange
        Long projectId = 1L;
        List<User> users = Arrays.asList(testUser);
        when(userRepository.findUsersByProjectId(projectId)).thenReturn(users);
        when(userRepository.countTasksByUserAndStatus(anyLong(), eq(WorkItemStatus.IN_PROGRESS)))
                .thenReturn(1L);
        when(userRepository.countTasksByUserAndStatus(anyLong(), eq(WorkItemStatus.TODO)))
                .thenReturn(1L);

        // Act
        UserService.TeamWorkload result = userService.getTeamWorkload(projectId);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.totalMembers()).isEqualTo(1);
        assertThat(result.activeMembers()).isEqualTo(1);
    }

    @Test
    void isUserAvailable_WithFewTasks_ShouldReturnTrue() {
        // Arrange
        Long userId = 1L;
        int maxTaskThreshold = 5;
        when(userRepository.countTasksByUserAndStatus(userId, WorkItemStatus.IN_PROGRESS))
                .thenReturn(2L);
        when(userRepository.countTasksByUserAndStatus(userId, WorkItemStatus.TODO))
                .thenReturn(1L);

        // Act
        boolean result = userService.isUserAvailable(userId, maxTaskThreshold);

        // Assert
        assertThat(result).isTrue();
    }

    @Test
    void isUserAvailable_WithManyTasks_ShouldReturnFalse() {
        // Arrange
        Long userId = 1L;
        int maxTaskThreshold = 5;
        when(userRepository.countTasksByUserAndStatus(userId, WorkItemStatus.IN_PROGRESS))
                .thenReturn(4L);
        when(userRepository.countTasksByUserAndStatus(userId, WorkItemStatus.TODO))
                .thenReturn(2L);

        // Act
        boolean result = userService.isUserAvailable(userId, maxTaskThreshold);

        // Assert
        assertThat(result).isFalse();
    }

    @Test
    void getLeastLoadedUserByRole_ShouldReturnLeastLoadedUser() {
        // Arrange
        Role role = Role.DEVELOPER;
        User user1 = new User();
        user1.setId(1L);
        user1.setUsername("user1");
        user1.setRole(role);

        User user2 = new User();
        user2.setId(2L);
        user2.setUsername("user2");
        user2.setRole(role);

        List<User> users = Arrays.asList(user1, user2);
        when(userRepository.findByRoleAndIsActiveTrue(role)).thenReturn(users);

        // user1 has fewer tasks (2 total)
        when(userRepository.countTasksByUserAndStatus(1L, WorkItemStatus.IN_PROGRESS))
                .thenReturn(1L);
        when(userRepository.countTasksByUserAndStatus(1L, WorkItemStatus.TODO))
                .thenReturn(1L);

        // user2 has more tasks (5 total)
        when(userRepository.countTasksByUserAndStatus(2L, WorkItemStatus.IN_PROGRESS))
                .thenReturn(3L);
        when(userRepository.countTasksByUserAndStatus(2L, WorkItemStatus.TODO))
                .thenReturn(2L);

        // Act
        User result = userService.getLeastLoadedUserByRole(role);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L); // Should return user1 (least loaded)
        assertThat(result.getRole()).isEqualTo(role);
    }

    @Test
    void getLeastLoadedUserByRole_WithNoUsers_ShouldThrowException() {
        // Arrange
        Role role = Role.DEVELOPER;
        when(userRepository.findByRoleAndIsActiveTrue(role)).thenReturn(Arrays.asList());

        // Act & Assert
        assertThatThrownBy(() -> userService.getLeastLoadedUserByRole(role))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Aucun utilisateur disponible");
    }

    @Test
    void updateUserProfile_WithValidData_ShouldUpdateProfile() {
        // Arrange
        Long userId = 1L;
        String newEmail = "newemail@example.com";
        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
        when(userRepository.existsByEmail(newEmail)).thenReturn(false);
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        // Act
        User result = userService.updateUserProfile(userId, newEmail,
                "NewFirst", "NewLast", "9876543210");

        // Assert
        assertThat(result).isNotNull();
        verify(userRepository, times(1)).save(testUser);
    }

    @Test
    void getUserCountByRole_ShouldReturnCountMap() {
        // Arrange
        when(userRepository.countByRole(any(Role.class))).thenReturn(5);

        // Act
        Map<Role, Integer> result = userService.getUserCountByRole();

        // Assert
        assertThat(result).isNotEmpty();
        assertThat(result).containsKeys(Role.values());
    }

    @Test
    void resetPassword_WithValidPassword_ShouldResetPassword() {
        // Arrange
        Long userId = 1L;
        String newPassword = "NewPassword123";
        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
        when(passwordEncoder.encode(newPassword)).thenReturn("encodedNewPassword");
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        // Act
        User result = userService.resetPassword(userId, newPassword);

        // Assert
        assertThat(result).isNotNull();
        verify(passwordEncoder, times(1)).encode(newPassword);
        verify(userRepository, times(1)).save(testUser);
    }

    @Test
    void resetPassword_WithShortPassword_ShouldThrowException() {
        // Arrange
        Long userId = 1L;
        String shortPassword = "short";
        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));

        // Act & Assert
        assertThatThrownBy(() -> userService.resetPassword(userId, shortPassword))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("8 caractères");
    }

    @Test
    void requiresPasswordReset_ShouldReturnResetStatus() {
        // Arrange
        Long userId = 1L;
        testUser.setPasswordResetRequired(true);
        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));

        // Act
        boolean result = userService.requiresPasswordReset(userId);

        // Assert
        assertThat(result).isTrue();
    }

    @Test
    void markPasswordChanged_ShouldMarkAsChanged() {
        // Arrange
        Long userId = 1L;
        testUser.setPasswordResetRequired(true);
        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        // Act
        User result = userService.markPasswordChanged(userId);

        // Assert
        assertThat(result).isNotNull();
        verify(userRepository, times(1)).save(testUser);
    }

    @Test
    void getUserPerformance_ShouldReturnPerformance() {
        // Arrange
        Long userId = 1L;
        LocalDate startDate = LocalDate.now().minusMonths(1);
        LocalDate endDate = LocalDate.now();

        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
        when(userRepository.countTasksByUserAndStatus(userId, WorkItemStatus.TODO))
                .thenReturn(2L);
        when(userRepository.countTasksByUserAndStatus(userId, WorkItemStatus.IN_PROGRESS))
                .thenReturn(3L);
        when(userRepository.countTasksByUserAndStatus(userId, WorkItemStatus.DONE))
                .thenReturn(5L);
        when(userRepository.countTasksCompletedByUserBetweenDates(userId, startDate, endDate))
                .thenReturn(10);

        // Act
        UserService.UserPerformance result = userService.getUserPerformance(userId,
                startDate, endDate);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.tasksCompletedInPeriod()).isEqualTo(10);
    }

    @Test
    void getUsersWithoutTasks_ShouldReturnUsersWithoutTasks() {
        // Arrange
        Long projectId = 1L;
        List<User> users = Arrays.asList(testUser);
        when(userRepository.findUsersByProjectId(projectId)).thenReturn(users);
        when(userRepository.countTasksByUserAndStatus(anyLong(), eq(WorkItemStatus.IN_PROGRESS)))
                .thenReturn(0L);
        when(userRepository.countTasksByUserAndStatus(anyLong(), eq(WorkItemStatus.TODO)))
                .thenReturn(0L);

        // Act
        List<User> result = userService.getUsersWithoutTasks(projectId);

        // Assert
        assertThat(result).hasSize(1);
    }

    @Test
    void sendWelcomeNotification_ShouldSendNotification() {
        // Arrange
        Long userId = 1L;
        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));

        // Act
        userService.sendWelcomeNotification(userId);

        // Assert
        verify(userRepository, times(1)).findById(userId);
    }
}