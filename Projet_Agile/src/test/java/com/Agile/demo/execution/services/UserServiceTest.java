package com.Agile.demo.execution.services;

import com.Agile.demo.execution.dto.mapper.UserMapper;
import com.Agile.demo.execution.dto.user.*;
import com.Agile.demo.execution.repositories.UserRepository;
import com.Agile.demo.model.Role;
import com.Agile.demo.model.User;
import com.Agile.demo.model.WorkItemStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.*;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

// Import des Records internes définis dans UserService
import com.Agile.demo.execution.services.UserService.UserStatistics;
import com.Agile.demo.execution.services.UserService.TeamWorkload;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private UserMapper userMapper;

    @InjectMocks
    private UserService userService;

    private User testUser;
    private UserResponseDTO testUserResponseDTO;
    private CreateUserRequest createUserRequest;

    @BeforeEach
    void setUp() {
        // Initialisation des objets communs pour les tests
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
                .passwordResetRequired(false)
                .build();

        testUserResponseDTO = UserResponseDTO.builder()
                .id(1L)
                .username("testuser")
                .email("test@example.com")
                .roles(Set.of(Role.DEVELOPER))
                .firstName("Test")
                .lastName("User")
                .isActive(true)
                .build();

        createUserRequest = CreateUserRequest.builder()
                .username("newuser")
                .email("newuser@example.com")
                .password("Password123")
                .roles(Set.of(Role.DEVELOPER))
                .firstName("First")
                .lastName("Last")
                .build();
    }

    // ==================== TESTS CRÉATION ====================

    @Test
    @DisplayName("Devrait créer un utilisateur avec succès quand les données sont valides")
    void createUser_WithValidData_ShouldCreateUser() {
        // Arrange
        when(userRepository.existsByUsername(createUserRequest.getUsername())).thenReturn(false);
        when(userRepository.existsByEmail(createUserRequest.getEmail())).thenReturn(false);
        when(userMapper.toEntity(createUserRequest)).thenReturn(testUser);
        when(passwordEncoder.encode(createUserRequest.getPassword())).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenReturn(testUser);
        when(userMapper.toResponseDTO(testUser)).thenReturn(testUserResponseDTO);

        // Act
        UserResponseDTO result = userService.createUser(createUserRequest);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getUsername()).isEqualTo("testuser");

        // Vérification que le mot de passe a bien été encodé avant la sauvegarde
        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());
        assertThat(userCaptor.getValue().getPassword()).isEqualTo("encodedPassword");
    }

    @Test
    @DisplayName("Devrait lancer une exception si le nom d'utilisateur existe déjà")
    void createUser_WithExistingUsername_ShouldThrowException() {
        // Arrange
        when(userRepository.existsByUsername(createUserRequest.getUsername())).thenReturn(true);

        // Act & Assert
        assertThatThrownBy(() -> userService.createUser(createUserRequest))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("existe déjà");

        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("Devrait lancer une exception si l'email est invalide (Regex)")
    void createUser_WithInvalidEmail_ShouldThrowException() {
        // Arrange
        createUserRequest.setEmail("invalid-email-format");
        when(userRepository.existsByUsername(anyString())).thenReturn(false);
        when(userRepository.existsByEmail(anyString())).thenReturn(false);

        // Act & Assert
        assertThatThrownBy(() -> userService.createUser(createUserRequest))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("invalide");
    }

    // ==================== TESTS LECTURE ====================

    @Test
    void getUserById_WithValidId_ShouldReturnUserDTO() {
        // Arrange
        Long userId = 1L;
        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
        when(userMapper.toResponseDTO(testUser)).thenReturn(testUserResponseDTO);

        // Act
        UserResponseDTO result = userService.getUserById(userId);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(userId);
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
    void searchUsers_ShouldReturnMatchingUsers() {
        // Arrange
        String term = "test";
        List<User> users = List.of(testUser);
        when(userRepository.findByUsernameContainingIgnoreCaseOrEmailContainingIgnoreCase(term, term))
                .thenReturn(users);
        when(userMapper.toResponseDTOList(users)).thenReturn(List.of(testUserResponseDTO));

        // Act
        List<UserResponseDTO> result = userService.searchUsers(term);

        // Assert
        assertThat(result).hasSize(1);
        verify(userRepository).findByUsernameContainingIgnoreCaseOrEmailContainingIgnoreCase(term, term);
    }

    // ==================== TESTS MISE À JOUR ====================

    @Test
    void updateUser_WithValidData_ShouldUpdateUser() {
        // Arrange
        Long userId = 1L;
        UserUpdateRequest updateRequest = UserUpdateRequest.builder()
                .email("newemail@example.com") // Changement d'email
                .firstName("NewFirst")
                .build();

        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
        when(userRepository.existsByEmail(updateRequest.getEmail())).thenReturn(false);
        when(userRepository.save(testUser)).thenReturn(testUser);
        when(userMapper.toResponseDTO(testUser)).thenReturn(testUserResponseDTO);

        // Act
        userService.updateUser(userId, updateRequest);

        // Assert
        verify(userMapper).updateEntityFromDTO(updateRequest, testUser);
        verify(userRepository).save(testUser);
    }

    @Test
    void updatePassword_WithCorrectCurrentPassword_ShouldUpdate() {
        // Arrange
        Long userId = 1L;
        // CORRECTION : Utilisation du builder au lieu du new()
        PasswordUpdateRequest req = PasswordUpdateRequest.builder()
                .currentPassword("oldPass")
                .newPassword("newPass123")
                .build();

        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches(req.getCurrentPassword(), testUser.getPassword())).thenReturn(true);
        when(passwordEncoder.encode(req.getNewPassword())).thenReturn("newEncoded");
        when(userRepository.save(testUser)).thenReturn(testUser);

        // Act
        userService.updatePassword(userId, req);

        // Assert
        assertThat(testUser.getPassword()).isEqualTo("newEncoded");
        verify(userRepository).save(testUser);
    }

    // ==================== GESTION DES RÔLES ====================

    @Test
    void addRoleToUser_ShouldAddRoleAndSave() {
        // Arrange
        Long userId = 1L;
        RoleManagementRequest request = new RoleManagementRequest(Role.SCRUM_MASTER);
        // Note: testUser a déjà DEVELOPER

        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
        when(userRepository.save(testUser)).thenReturn(testUser);

        // Act
        userService.addRoleToUser(userId, request);

        // Assert
        assertThat(testUser.getRoles()).contains(Role.DEVELOPER, Role.SCRUM_MASTER);
        verify(userRepository).save(testUser);
    }

    @Test
    void removeRoleFromUser_LastRole_ShouldThrowException() {
        // Arrange
        Long userId = 1L;
        testUser.setRoles(new HashSet<>(Set.of(Role.DEVELOPER))); // Un seul rôle
        RoleManagementRequest request = new RoleManagementRequest(Role.DEVELOPER);

        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));

        // Act & Assert
        assertThatThrownBy(() -> userService.removeRoleFromUser(userId, request))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("au moins un rôle");
    }

    // ==================== ACTIVATION / DÉSACTIVATION ====================

    @Test
    void deactivateUser_WithActiveTasks_ShouldThrowException() {
        // Arrange
        Long userId = 1L;
        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
        when(userRepository.countTasksByUserAndStatus(userId, WorkItemStatus.IN_PROGRESS)).thenReturn(1L);

        // Act & Assert
        assertThatThrownBy(() -> userService.deactivateUser(userId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("tâches en cours");
    }

    @Test
    void deactivateUser_WithNoTasks_ShouldDeactivate() {
        // Arrange
        Long userId = 1L;
        testUser.setisActive(true);
        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
        when(userRepository.countTasksByUserAndStatus(userId, WorkItemStatus.IN_PROGRESS)).thenReturn(0L);
        when(userRepository.save(testUser)).thenReturn(testUser);

        // Act
        userService.deactivateUser(userId);

        // Assert
        // Vérification via ArgumentCaptor pour s'assurer que le flag a changé
        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(captor.capture());
        assertThat(captor.getValue().isActive()).isFalse();
    }

    // ==================== DISPONIBILITÉ & STATISTIQUES (Complex Logic) ====================

    @Test
    void getLeastLoadedUserByRole_ShouldReturnUserWithFewestTasks() {
        // Arrange
        User busyUser = User.builder().id(2L).username("busy").roles(Set.of(Role.DEVELOPER)).isActive(true).build();
        User freeUser = User.builder().id(3L).username("free").roles(Set.of(Role.DEVELOPER)).isActive(true).build();

        // Mock: busyUser a 5 tâches, freeUser a 1 tâche
        when(userRepository.findByRoleAndIsActiveTrue(Role.DEVELOPER)).thenReturn(List.of(busyUser, freeUser));

        // Attention: countUserActiveTasks appelle le repository deux fois (TODO + IN_PROGRESS)
        // Pour busyUser (Total 5)
        when(userRepository.countTasksByUserAndStatus(2L, WorkItemStatus.IN_PROGRESS)).thenReturn(3L);
        when(userRepository.countTasksByUserAndStatus(2L, WorkItemStatus.TODO)).thenReturn(2L);

        // Pour freeUser (Total 1)
        when(userRepository.countTasksByUserAndStatus(3L, WorkItemStatus.IN_PROGRESS)).thenReturn(0L);
        when(userRepository.countTasksByUserAndStatus(3L, WorkItemStatus.TODO)).thenReturn(1L);

        when(userMapper.toResponseDTO(freeUser)).thenReturn(UserResponseDTO.builder().username("free").build());

        // Act
        UserResponseDTO result = userService.getLeastLoadedUserByRole(Role.DEVELOPER);

        // Assert
        assertThat(result.getUsername()).isEqualTo("free");
    }

    @Test
    void getUserStatistics_ShouldCalculateRatesCorrectly() {
        // Arrange
        Long userId = 1L;
        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
        when(userRepository.countTasksByUserAndStatus(userId, WorkItemStatus.TODO)).thenReturn(2L);
        when(userRepository.countTasksByUserAndStatus(userId, WorkItemStatus.IN_PROGRESS)).thenReturn(3L);
        when(userRepository.countTasksByUserAndStatus(userId, WorkItemStatus.DONE)).thenReturn(5L);
        // Total = 10, Done = 5 => 50%

        // Act
        UserStatistics stats = userService.getUserStatistics(userId);

        // Assert
        assertThat(stats.totalTasks()).isEqualTo(10);
        assertThat(stats.completionRate()).isEqualTo(50.0);
    }

    @Test
    void getTeamWorkload_ShouldReturnCorrectMetrics() {
        // Arrange
        Long projectId = 100L;
        when(userRepository.findUsersByProjectId(projectId)).thenReturn(List.of(testUser));

        // Mock des appels internes à countUserActiveTasks
        when(userRepository.countTasksByUserAndStatus(eq(1L), eq(WorkItemStatus.IN_PROGRESS))).thenReturn(2L);
        when(userRepository.countTasksByUserAndStatus(eq(1L), eq(WorkItemStatus.TODO))).thenReturn(1L);
        // Total active tasks = 3

        // Act
        TeamWorkload workload = userService.getTeamWorkload(projectId);

        // Assert
        assertThat(workload.totalMembers()).isEqualTo(1);
        assertThat(workload.totalActiveTasks()).isEqualTo(3);
        assertThat(workload.memberWorkloads()).hasSize(1);
        assertThat(workload.memberWorkloads().get(0).activeTasks()).isEqualTo(3);
    }
}