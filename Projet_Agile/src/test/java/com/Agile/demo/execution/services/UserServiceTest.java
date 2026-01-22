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
import java.util.*;

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
        testUser = User.builder()
                .id(1L)
                .username("testuser")
                .email("test@example.com")
                .password("encodedPassword")
                .roles(new HashSet<>(Set.of(Role.DEVELOPER)))
                .firstName("Test")
                .lastName("User")
                .phoneNumber("1234567890")
                .isActive(true)
                .build();
    }

    @Test
    void createUser_WithValidData_ShouldCreateUser() {
        // Arrange
        String username = "newuser";
        String email = "newuser@example.com";
        String password = "Password123";
        Set<Role> roles = Set.of(Role.DEVELOPER);

        when(userRepository.existsByUsername(username)).thenReturn(false);
        when(userRepository.existsByEmail(email)).thenReturn(false);
        when(passwordEncoder.encode(password)).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        // Act
        User result = userService.createUser(username, email, password, roles,
                "First", "Last", "1234567890");

        // Assert
        assertThat(result).isNotNull();
        verify(passwordEncoder, times(1)).encode(password);
        verify(userRepository, times(1)).save(any(User.class));
    }

    // ✅ NOUVEAU TEST : Création avec plusieurs rôles
    @Test
    void createUser_WithMultipleRoles_ShouldCreateUser() {
        // Arrange
        String username = "multiuser";
        String email = "multi@example.com";
        String password = "Password123";
        Set<Role> roles = Set.of(Role.DEVELOPER, Role.SCRUM_MASTER);

        when(userRepository.existsByUsername(username)).thenReturn(false);
        when(userRepository.existsByEmail(email)).thenReturn(false);
        when(passwordEncoder.encode(password)).thenReturn("encodedPassword");

        User userWithMultipleRoles = User.builder()
                .roles(new HashSet<>(roles))
                .build();
        when(userRepository.save(any(User.class))).thenReturn(userWithMultipleRoles);

        // Act
        User result = userService.createUser(username, email, password, roles,
                "First", "Last", "1234567890");

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getRoles()).hasSize(2);
        assertThat(result.getRoles()).contains(Role.DEVELOPER, Role.SCRUM_MASTER);
    }

    // ✅ NOUVEAU TEST : Création sans rôle (devrait échouer)
    @Test
    void createUser_WithNoRoles_ShouldThrowException() {
        // Arrange
        String username = "noroleuser";
        when(userRepository.existsByUsername(username)).thenReturn(false);
        when(userRepository.existsByEmail(anyString())).thenReturn(false);

        // Act & Assert
        assertThatThrownBy(() -> userService.createUser(username, "email@test.com",
                "password", Collections.emptySet(), "First", "Last", "1234567890"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Au moins un rôle");
    }

    // ✅ TEST MODIFIÉ : Création avec un seul rôle (méthode de compatibilité)
    @Test
    void createUser_WithSingleRole_ShouldCreateUser() {
        // Arrange
        String username = "singleuser";
        String email = "single@example.com";
        String password = "Password123";
        Role role = Role.DEVELOPER;

        when(userRepository.existsByUsername(username)).thenReturn(false);
        when(userRepository.existsByEmail(email)).thenReturn(false);
        when(passwordEncoder.encode(password)).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        // Act - Utilisation de la méthode de compatibilité
        User result = userService.createUser(username, email, password, role,
                "First", "Last", "1234567890");

        // Assert
        assertThat(result).isNotNull();
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    void createUser_WithExistingUsername_ShouldThrowException() {
        // Arrange
        String username = "existinguser";
        when(userRepository.existsByUsername(username)).thenReturn(true);

        // Act & Assert
        assertThatThrownBy(() -> userService.createUser(username, "email@test.com",
                "password", Set.of(Role.DEVELOPER), "First", "Last", "1234567890"))
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
                "password", Set.of(Role.DEVELOPER), "First", "Last", "1234567890"))
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
                "password", Set.of(Role.DEVELOPER), "First", "Last", "1234567890"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("invalide");
    }

    // ✅ NOUVEAU TEST : Ajouter un rôle
    @Test
    void addRoleToUser_ShouldAddRole() {
        // Arrange
        Long userId = 1L;
        Role newRole = Role.SCRUM_MASTER;
        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        // Act
        User result = userService.addRoleToUser(userId, newRole);

        // Assert
        assertThat(result.getRoles()).contains(newRole);
        verify(userRepository, times(1)).save(testUser);
    }

    // ✅ NOUVEAU TEST : Retirer un rôle
    @Test
    void removeRoleFromUser_WithMultipleRoles_ShouldRemoveRole() {
        // Arrange
        Long userId = 1L;
        testUser.setRoles(new HashSet<>(Set.of(Role.DEVELOPER, Role.SCRUM_MASTER)));
        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        // Act
        User result = userService.removeRoleFromUser(userId, Role.SCRUM_MASTER);

        // Assert
        assertThat(result.getRoles()).doesNotContain(Role.SCRUM_MASTER);
        assertThat(result.getRoles()).contains(Role.DEVELOPER);
        verify(userRepository, times(1)).save(testUser);
    }

    // ✅ NOUVEAU TEST : Retirer le dernier rôle (devrait échouer)
    @Test
    void removeRoleFromUser_WithOnlyOneRole_ShouldThrowException() {
        // Arrange
        Long userId = 1L;
        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));

        // Act & Assert
        assertThatThrownBy(() -> userService.removeRoleFromUser(userId, Role.DEVELOPER))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("au moins un rôle");
    }

    // ✅ NOUVEAU TEST : Vérifier si un utilisateur a un rôle
    @Test
    void userHasRole_WithExistingRole_ShouldReturnTrue() {
        // Arrange
        Long userId = 1L;
        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));

        // Act
        boolean result = userService.userHasRole(userId, Role.DEVELOPER);

        // Assert
        assertThat(result).isTrue();
    }

    @Test
    void userHasRole_WithNonExistingRole_ShouldReturnFalse() {
        // Arrange
        Long userId = 1L;
        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));

        // Act
        boolean result = userService.userHasRole(userId, Role.PRODUCT_OWNER);

        // Assert
        assertThat(result).isFalse();
    }

    // ✅ NOUVEAU TEST : Vérifier si un utilisateur a au moins un des rôles
    @Test
    void userHasAnyRole_WithMatchingRole_ShouldReturnTrue() {
        // Arrange
        Long userId = 1L;
        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));

        // Act
        boolean result = userService.userHasAnyRole(userId, Role.PRODUCT_OWNER, Role.DEVELOPER);

        // Assert
        assertThat(result).isTrue();
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
    void getUserStatistics_ShouldReturnStatisticsWithRoles() {
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
        assertThat(result.roles()).isNotEmpty();
        assertThat(result.totalTasks()).isEqualTo(10);
        assertThat(result.completedTasks()).isEqualTo(5);
        assertThat(result.completionRate()).isEqualTo(50.0);
    }

    // Les autres tests restent similaires mais utilisent Set<Role> au lieu de Role unique
    // Je vais montrer quelques exemples clés

    @Test
    void updateUser_WithValidRoles_ShouldUpdateUser() {
        // Arrange
        Long userId = 1L;
        Set<Role> newRoles = Set.of(Role.DEVELOPER, Role.SCRUM_MASTER);

        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        // Act
        User result = userService.updateUser(userId, null, newRoles,
                null, null, null, null);

        // Assert
        assertThat(result).isNotNull();
        verify(userRepository, times(1)).save(testUser);
    }

    @Test
    void getMostLoadedUsers_ShouldReturnUsersWithRoles() {
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
        assertThat(result.get(0).roles()).isNotEmpty();
    }

    @Test
    void getUserPerformance_ShouldReturnPerformanceWithRoles() {
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
        assertThat(result.roles()).isNotEmpty();
        assertThat(result.tasksCompletedInPeriod()).isEqualTo(10);
    }

    // Tests supplémentaires pour les nouvelles méthodes

    @Test
    void getUsersByRoles_ShouldReturnUsers() {
        // Arrange
        List<Role> roles = Arrays.asList(Role.DEVELOPER, Role.SCRUM_MASTER);
        List<User> users = Arrays.asList(testUser);
        when(userRepository.findByRolesIn(roles)).thenReturn(users);

        // Act
        List<User> result = userService.getUsersByRoles(roles);

        // Assert
        assertThat(result).hasSize(1);
        verify(userRepository, times(1)).findByRolesIn(roles);
    }
}