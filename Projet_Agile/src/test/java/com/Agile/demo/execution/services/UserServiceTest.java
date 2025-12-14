package com.Agile.demo.execution.services;

import com.Agile.demo.model.Role;
import com.Agile.demo.model.User;
import com.Agile.demo.execution.repositories.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
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

    private User user;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(1L);
        user.setUsername("testuser");
        user.setEmail("test@example.com");
        user.setPassword("encodedPassword");
        user.setRole(Role.DEVELOPER);
    }

    @Test
    void createUser_WhenValidData_ShouldCreateUser() {
        // Arrange
        when(userRepository.existsByUsername("newuser")).thenReturn(false);
        when(userRepository.existsByEmail("new@example.com")).thenReturn(false);
        when(passwordEncoder.encode("password123")).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenReturn(user);

        // Act
        User result = userService.createUser("newuser", "new@example.com", "password123", Role.DEVELOPER);

        // Assert
        assertThat(result).isNotNull();
        verify(passwordEncoder).encode("password123");
        verify(userRepository).save(any(User.class));
    }

    @Test
    void createUser_WhenUsernameExists_ShouldThrowException() {
        // Arrange
        when(userRepository.existsByUsername("existinguser")).thenReturn(true);

        // Act & Assert
        assertThatThrownBy(() -> userService.createUser("existinguser", "email@test.com", "pass", Role.DEVELOPER))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("nom d'utilisateur existe déjà");
    }

    @Test
    void createUser_WhenEmailExists_ShouldThrowException() {
        // Arrange
        when(userRepository.existsByUsername("newuser")).thenReturn(false);
        when(userRepository.existsByEmail("existing@example.com")).thenReturn(true);

        // Act & Assert
        assertThatThrownBy(() -> userService.createUser("newuser", "existing@example.com", "pass", Role.DEVELOPER))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("email existe déjà");
    }

    @Test
    void createUser_WhenInvalidEmail_ShouldThrowException() {
        // Arrange
        when(userRepository.existsByUsername("newuser")).thenReturn(false);
        when(userRepository.existsByEmail("invalidemail")).thenReturn(false);

        // Act & Assert
        assertThatThrownBy(() -> userService.createUser("newuser", "invalidemail", "pass", Role.DEVELOPER))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Format d'email invalide");
    }

    @Test
    void getUserById_WhenUserExists_ShouldReturnUser() {
        // Arrange
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        // Act
        User result = userService.getUserById(1L);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getUsername()).isEqualTo("testuser");
    }

    @Test
    void getUserById_WhenUserNotFound_ShouldThrowException() {
        // Arrange
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> userService.getUserById(999L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Utilisateur non trouvé");
    }

    @Test
    void getUserByUsername_WhenUserExists_ShouldReturnUser() {
        // Arrange
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));

        // Act
        User result = userService.getUserByUsername("testuser");

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getUsername()).isEqualTo("testuser");
    }

    @Test
    void getUserByUsername_WhenUserNotFound_ShouldThrowException() {
        // Arrange
        when(userRepository.findByUsername("nonexistent")).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> userService.getUserByUsername("nonexistent"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Utilisateur non trouvé");
    }

    @Test
    void getUserByEmail_WhenUserExists_ShouldReturnUser() {
        // Arrange
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(user));

        // Act
        User result = userService.getUserByEmail("test@example.com");

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getEmail()).isEqualTo("test@example.com");
    }

    @Test
    void getAllUsers_ShouldReturnAllUsers() {
        // Arrange
        List<User> users = List.of(user);
        when(userRepository.findAll()).thenReturn(users);

        // Act
        List<User> result = userService.getAllUsers();

        // Assert
        assertThat(result).hasSize(1);
        assertThat(result.get(0)).isEqualTo(user);
    }

    @Test
    void getUsersByRole_ShouldReturnUsersByRole() {
        // Arrange
        List<User> users = List.of(user);
        when(userRepository.findByRole(Role.DEVELOPER)).thenReturn(users);

        // Act
        List<User> result = userService.getUsersByRole(Role.DEVELOPER);

        // Assert
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getRole()).isEqualTo(Role.DEVELOPER);
    }

    @Test
    void getUsersByProject_ShouldReturnProjectUsers() {
        // Arrange
        List<User> users = List.of(user);
        when(userRepository.findUsersByProjectId(1L)).thenReturn(users);

        // Act
        List<User> result = userService.getUsersByProject(1L);

        // Assert
        assertThat(result).hasSize(1);
    }

    @Test
    void updateUser_WhenValidData_ShouldUpdateUser() {
        // Arrange
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userRepository.existsByEmail("newemail@example.com")).thenReturn(false);
        when(userRepository.save(any(User.class))).thenReturn(user);

        // Act
        User result = userService.updateUser(1L, "newemail@example.com", Role.SCRUM_MASTER);

        // Assert
        assertThat(result).isNotNull();
        verify(userRepository).save(user);
    }

    @Test
    void updateUser_WhenEmailAlreadyUsed_ShouldThrowException() {
        // Arrange
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userRepository.existsByEmail("existing@example.com")).thenReturn(true);

        // Act & Assert
        assertThatThrownBy(() -> userService.updateUser(1L, "existing@example.com", Role.DEVELOPER))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("email existe déjà");
    }

    @Test
    void updateUser_WhenInvalidEmail_ShouldThrowException() {
        // Arrange
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        // Act & Assert
        assertThatThrownBy(() -> userService.updateUser(1L, "invalidemail", Role.DEVELOPER))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Format d'email invalide");
    }

    @Test
    void updatePassword_WhenCurrentPasswordCorrect_ShouldUpdatePassword() {
        // Arrange
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("oldPassword", "encodedPassword")).thenReturn(true);
        when(passwordEncoder.encode("newPassword123")).thenReturn("newEncodedPassword");
        when(userRepository.save(any(User.class))).thenReturn(user);

        // Act
        User result = userService.updatePassword(1L, "oldPassword", "newPassword123");

        // Assert
        assertThat(result).isNotNull();
        verify(passwordEncoder).encode("newPassword123");
        verify(userRepository).save(user);
    }

    @Test
    void updatePassword_WhenCurrentPasswordIncorrect_ShouldThrowException() {
        // Arrange
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("wrongPassword", "encodedPassword")).thenReturn(false);

        // Act & Assert
        assertThatThrownBy(() -> userService.updatePassword(1L, "wrongPassword", "newPass"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Mot de passe actuel incorrect");
    }

    @Test
    void updatePassword_WhenNewPasswordTooShort_ShouldThrowException() {
        // Arrange
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("oldPass", "encodedPassword")).thenReturn(true);

        // Act & Assert
        assertThatThrownBy(() -> userService.updatePassword(1L, "oldPass", "short"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("au moins 8 caractères");
    }

    @Test
    void deleteUser_WhenNoActiveTasks_ShouldDeleteUser() {
        // Arrange
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userRepository.countTasksByUserAndStatus(1L, "IN_PROGRESS")).thenReturn(0L);

        // Act
        userService.deleteUser(1L);

        // Assert
        verify(userRepository).delete(user);
    }

    @Test
    void deleteUser_WhenHasActiveTasks_ShouldThrowException() {
        // Arrange
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userRepository.countTasksByUserAndStatus(1L, "IN_PROGRESS")).thenReturn(3L);

        // Act & Assert
        assertThatThrownBy(() -> userService.deleteUser(1L))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("tâches en cours");
    }

    @Test
    void getAvailableUsers_ShouldReturnAvailableUsers() {
        // Arrange
        List<User> users = List.of(user);
        when(userRepository.findAvailableUsersByRole(Role.DEVELOPER, 5L)).thenReturn(users);

        // Act
        List<User> result = userService.getAvailableUsers(Role.DEVELOPER, 5L);

        // Assert
        assertThat(result).hasSize(1);
    }

    @Test
    void countUserActiveTasks_ShouldReturnTaskCount() {
        // Arrange
        when(userRepository.countTasksByUserAndStatus(1L, "IN_PROGRESS")).thenReturn(2L);
        when(userRepository.countTasksByUserAndStatus(1L, "TODO")).thenReturn(3L);

        // Act
        long result = userService.countUserActiveTasks(1L);

        // Assert
        assertThat(result).isEqualTo(5L);
    }

    @Test
    void userExists_WhenUserExists_ShouldReturnTrue() {
        // Arrange
        when(userRepository.existsById(1L)).thenReturn(true);

        // Act
        boolean result = userService.userExists(1L);

        // Assert
        assertThat(result).isTrue();
    }

    @Test
    void userExists_WhenUserNotExists_ShouldReturnFalse() {
        // Arrange
        when(userRepository.existsById(999L)).thenReturn(false);

        // Act
        boolean result = userService.userExists(999L);

        // Assert
        assertThat(result).isFalse();
    }

    @Test
    void usernameExists_WhenUsernameExists_ShouldReturnTrue() {
        // Arrange
        when(userRepository.existsByUsername("testuser")).thenReturn(true);

        // Act
        boolean result = userService.usernameExists("testuser");

        // Assert
        assertThat(result).isTrue();
    }

    @Test
    void emailExists_WhenEmailExists_ShouldReturnTrue() {
        // Arrange
        when(userRepository.existsByEmail("test@example.com")).thenReturn(true);

        // Act
        boolean result = userService.emailExists("test@example.com");

        // Assert
        assertThat(result).isTrue();
    }

    @Test
    void getUserStatistics_ShouldReturnStatistics() {
        // Arrange
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userRepository.countTasksByUserAndStatus(1L, "TODO")).thenReturn(2L);
        when(userRepository.countTasksByUserAndStatus(1L, "IN_PROGRESS")).thenReturn(3L);
        when(userRepository.countTasksByUserAndStatus(1L, "DONE")).thenReturn(5L);

        // Act
        UserService.UserStatistics result = userService.getUserStatistics(1L);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.username()).isEqualTo("testuser");
        assertThat(result.role()).isEqualTo(Role.DEVELOPER);
        assertThat(result.todoTasks()).isEqualTo(2);
        assertThat(result.inProgressTasks()).isEqualTo(3);
        assertThat(result.completedTasks()).isEqualTo(5);
        assertThat(result.totalTasks()).isEqualTo(10);
        assertThat(result.completionRate()).isEqualTo(50.0);
    }

    @Test
    void getUserStatistics_WhenNoTasks_ShouldReturnZeroCompletionRate() {
        // Arrange
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userRepository.countTasksByUserAndStatus(1L, "TODO")).thenReturn(0L);
        when(userRepository.countTasksByUserAndStatus(1L, "IN_PROGRESS")).thenReturn(0L);
        when(userRepository.countTasksByUserAndStatus(1L, "DONE")).thenReturn(0L);

        // Act
        UserService.UserStatistics result = userService.getUserStatistics(1L);

        // Assert
        assertThat(result.totalTasks()).isEqualTo(0);
        assertThat(result.completionRate()).isEqualTo(0.0);
    }
}