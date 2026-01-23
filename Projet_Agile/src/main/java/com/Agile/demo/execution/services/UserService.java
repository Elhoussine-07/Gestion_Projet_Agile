package com.Agile.demo.execution.services;

import com.Agile.demo.execution.dto.mapper.UserMapper;
import com.Agile.demo.execution.dto.user.*;
import com.Agile.demo.model.Role;
import com.Agile.demo.model.User;
import com.Agile.demo.execution.repositories.UserRepository;
import com.Agile.demo.model.WorkItemStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserMapper userMapper;

    // ==================== CRÉATION ====================

    public UserResponseDTO createUser(CreateUserRequest request) {
        validateUserCreation(request);

        User user = userMapper.toEntity(request);
        user.setPassword(passwordEncoder.encode(request.getPassword()));

        User savedUser = userRepository.save(user);
        return userMapper.toResponseDTO(savedUser);
    }

    private void validateUserCreation(CreateUserRequest request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new IllegalArgumentException("Le nom d'utilisateur existe déjà: " + request.getUsername());
        }

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("L'email existe déjà: " + request.getEmail());
        }

        if (!isValidEmail(request.getEmail())) {
            throw new IllegalArgumentException("Format d'email invalide: " + request.getEmail());
        }

        if (request.getRoles() == null || request.getRoles().isEmpty()) {
            throw new IllegalArgumentException("Au moins un rôle doit être spécifié");
        }
    }

    // ==================== LECTURE ====================

    @Transactional(readOnly = true)
    public UserResponseDTO getUserById(Long userId) {
        User user = findUserByIdOrThrow(userId);
        return userMapper.toResponseDTO(user);
    }

    @Transactional(readOnly = true)
    public UserResponseDTO getUserByUsername(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("Utilisateur non trouvé: " + username));
        return userMapper.toResponseDTO(user);
    }

    @Transactional(readOnly = true)
    public UserResponseDTO getUserByEmail(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("Utilisateur non trouvé avec l'email: " + email));
        return userMapper.toResponseDTO(user);
    }

    @Transactional(readOnly = true)
    public List<UserResponseDTO> getAllUsers() {
        List<User> users = userRepository.findAll();
        return userMapper.toResponseDTOList(users);
    }

    @Transactional(readOnly = true)
    public List<UserResponseDTO> getUsersByRole(Role role) {
        List<User> users = userRepository.findByRole(role);
        return userMapper.toResponseDTOList(users);
    }

    @Transactional(readOnly = true)
    public List<UserResponseDTO> getUsersByRoles(List<Role> roles) {
        List<User> users = userRepository.findByRolesIn(roles);
        return userMapper.toResponseDTOList(users);
    }

    @Transactional(readOnly = true)
    public List<UserResponseDTO> getUsersByProject(Long projectId) {
        List<User> users = userRepository.findUsersByProjectId(projectId);
        return userMapper.toResponseDTOList(users);
    }

    @Transactional(readOnly = true)
    public List<UserResponseDTO> getActiveUsers() {
        List<User> users = userRepository.findByIsActiveTrue();
        return userMapper.toResponseDTOList(users);
    }

    @Transactional(readOnly = true)
    public List<UserResponseDTO> getActiveUsersByRole(Role role) {
        List<User> users = userRepository.findByRoleAndIsActiveTrue(role);
        return userMapper.toResponseDTOList(users);
    }

    @Transactional(readOnly = true)
    public List<UserResponseDTO> searchUsers(String searchTerm) {
        List<User> users = userRepository.findByUsernameContainingIgnoreCaseOrEmailContainingIgnoreCase(
                searchTerm, searchTerm);
        return userMapper.toResponseDTOList(users);
    }

    // ==================== MISE À JOUR ====================

    public UserResponseDTO updateUser(Long userId, UserUpdateRequest request) {
        User user = findUserByIdOrThrow(userId);

        validateUserUpdate(user, request);
        userMapper.updateEntityFromDTO(request, user);

        User savedUser = userRepository.save(user);
        return userMapper.toResponseDTO(savedUser);
    }

    private void validateUserUpdate(User user, UserUpdateRequest request) {
        if (request.getEmail() != null && !request.getEmail().equals(user.getEmail())) {
            if (userRepository.existsByEmail(request.getEmail())) {
                throw new IllegalArgumentException("L'email existe déjà: " + request.getEmail());
            }
            if (!isValidEmail(request.getEmail())) {
                throw new IllegalArgumentException("Format d'email invalide: " + request.getEmail());
            }
        }
    }

    public UserResponseDTO updateUserProfile(Long userId, UserProfileUpdateRequest request) {
        User user = findUserByIdOrThrow(userId);

        if (request.getEmail() != null && !request.getEmail().equals(user.getEmail())) {
            if (userRepository.existsByEmail(request.getEmail())) {
                throw new IllegalArgumentException("L'email existe déjà: " + request.getEmail());
            }
            if (!isValidEmail(request.getEmail())) {
                throw new IllegalArgumentException("Format d'email invalide: " + request.getEmail());
            }
            user.setEmail(request.getEmail());
        }

        if (request.getFirstName() != null) user.setFirstName(request.getFirstName());
        if (request.getLastName() != null) user.setLastName(request.getLastName());
        if (request.getPhoneNumber() != null) user.setPhoneNumber(request.getPhoneNumber());

        User savedUser = userRepository.save(user);
        return userMapper.toResponseDTO(savedUser);
    }

    public UserResponseDTO updatePassword(Long userId, PasswordUpdateRequest request) {
        User user = findUserByIdOrThrow(userId);

        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPassword())) {
            throw new IllegalArgumentException("Mot de passe actuel incorrect");
        }

        if (request.getNewPassword() == null || request.getNewPassword().length() < 8) {
            throw new IllegalArgumentException("Le mot de passe doit contenir au moins 8 caractères");
        }

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        User savedUser = userRepository.save(user);
        return userMapper.toResponseDTO(savedUser);
    }

    public UserResponseDTO resetPassword(Long userId, PasswordResetRequest request) {
        User user = findUserByIdOrThrow(userId);

        if (request.getNewPassword() == null || request.getNewPassword().length() < 8) {
            throw new IllegalArgumentException("Le mot de passe doit contenir au moins 8 caractères");
        }

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        user.setPasswordResetRequired(true);

        User savedUser = userRepository.save(user);
        return userMapper.toResponseDTO(savedUser);
    }

    // ==================== GESTION DES RÔLES ====================

    public UserResponseDTO addRoleToUser(Long userId, RoleManagementRequest request) {
        User user = findUserByIdOrThrow(userId);
        user.addRole(request.getRole());

        User savedUser = userRepository.save(user);
        return userMapper.toResponseDTO(savedUser);
    }

    public UserResponseDTO removeRoleFromUser(Long userId, RoleManagementRequest request) {
        User user = findUserByIdOrThrow(userId);

        if (user.getRoles().size() <= 1) {
            throw new IllegalStateException("Un utilisateur doit avoir au moins un rôle");
        }

        user.removeRole(request.getRole());
        User savedUser = userRepository.save(user);
        return userMapper.toResponseDTO(savedUser);
    }

    @Transactional(readOnly = true)
    public boolean userHasRole(Long userId, Role role) {
        User user = findUserByIdOrThrow(userId);
        return user.hasRole(role);
    }

    @Transactional(readOnly = true)
    public boolean userHasAnyRole(Long userId, Role... roles) {
        User user = findUserByIdOrThrow(userId);
        return user.hasAnyRole(roles);
    }

    // ==================== ACTIVATION / DÉSACTIVATION ====================

    public UserResponseDTO activateUser(Long userId) {
        User user = findUserByIdOrThrow(userId);
        user.setisActive(true);

        User savedUser = userRepository.save(user);
        return userMapper.toResponseDTO(savedUser);
    }

    public UserResponseDTO deactivateUser(Long userId) {
        User user = findUserByIdOrThrow(userId);

        long activeTasks = userRepository.countTasksByUserAndStatus(userId, WorkItemStatus.IN_PROGRESS);
        if (activeTasks > 0) {
            throw new IllegalStateException("Impossible de désactiver un utilisateur avec des tâches en cours");
        }

        user.setisActive(false);
        User savedUser = userRepository.save(user);
        return userMapper.toResponseDTO(savedUser);
    }

    // ==================== SUPPRESSION ====================

    public void deleteUser(Long userId) {
        User user = findUserByIdOrThrow(userId);

        long activeTasks = userRepository.countTasksByUserAndStatus(userId, WorkItemStatus.IN_PROGRESS);
        if (activeTasks > 0) {
            throw new IllegalStateException("Impossible de supprimer un utilisateur avec des tâches en cours");
        }

        userRepository.delete(user);
    }

    // ==================== STATISTIQUES (Records - pas de DTO) ====================

    @Transactional(readOnly = true)
    public UserStatistics getUserStatistics(Long userId) {
        User user = findUserByIdOrThrow(userId);

        long todoTasks = userRepository.countTasksByUserAndStatus(userId, WorkItemStatus.TODO);
        long inProgressTasks = userRepository.countTasksByUserAndStatus(userId, WorkItemStatus.IN_PROGRESS);
        long doneTasks = userRepository.countTasksByUserAndStatus(userId, WorkItemStatus.DONE);
        long totalTasks = todoTasks + inProgressTasks + doneTasks;

        double completionRate = totalTasks > 0 ? (doneTasks * 100.0) / totalTasks : 0.0;

        return new UserStatistics(
                user.getUsername(),
                user.getRoles(),
                (int) todoTasks,
                (int) inProgressTasks,
                (int) doneTasks,
                (int) totalTasks,
                completionRate
        );
    }

    @Transactional(readOnly = true)
    public List<UserWorkload> getMostLoadedUsers(int limit) {
        List<User> users = userRepository.findAll();

        return users.stream()
                .map(user -> {
                    long activeTasks = countUserActiveTasks(user.getId());
                    return new UserWorkload(
                            user.getId(),
                            user.getUsername(),
                            user.getRoles(),
                            (int) activeTasks
                    );
                })
                .sorted((a, b) -> Integer.compare(b.activeTasks(), a.activeTasks()))
                .limit(limit)
                .toList();
    }

    @Transactional(readOnly = true)
    public TeamWorkload getTeamWorkload(Long projectId) {
        List<User> teamMembers = userRepository.findUsersByProjectId(projectId);

        int totalMembers = teamMembers.size();
        int activeMembers = (int) teamMembers.stream()
                .filter(User::isActive)
                .count();

        long totalActiveTasks = teamMembers.stream()
                .mapToLong(user -> countUserActiveTasks(user.getId()))
                .sum();

        double averageTasksPerUser = activeMembers > 0 ?
                (double) totalActiveTasks / activeMembers : 0.0;

        List<UserWorkload> memberWorkloads = teamMembers.stream()
                .map(user -> new UserWorkload(
                        user.getId(),
                        user.getUsername(),
                        user.getRoles(),
                        (int) countUserActiveTasks(user.getId())
                ))
                .toList();

        return new TeamWorkload(
                totalMembers,
                activeMembers,
                (int) totalActiveTasks,
                averageTasksPerUser,
                memberWorkloads
        );
    }

    @Transactional(readOnly = true)
    public UserPerformance getUserPerformance(Long userId, LocalDate startDate, LocalDate endDate) {
        User user = findUserByIdOrThrow(userId);

        UserStatistics stats = getUserStatistics(userId);

        int tasksCompletedInPeriod = userRepository.countTasksCompletedByUserBetweenDates(
                userId, startDate, endDate);

        int averageTasksPerWeek = 0;
        long weeksBetween = java.time.temporal.ChronoUnit.WEEKS.between(startDate, endDate);
        if (weeksBetween > 0) {
            averageTasksPerWeek = (int) (tasksCompletedInPeriod / weeksBetween);
        }

        return new UserPerformance(
                user.getUsername(),
                user.getRoles(),
                tasksCompletedInPeriod,
                averageTasksPerWeek,
                stats.completionRate()
        );
    }

    @Transactional(readOnly = true)
    public Map<Role, Integer> getUserCountByRole() {
        Map<Role, Integer> roleCount = new HashMap<>();
        for (Role role : Role.values()) {
            int count = userRepository.countByRole(role);
            roleCount.put(role, count);
        }
        return roleCount;
    }

    @Transactional(readOnly = true)
    public Map<String, Object> getUserRoleStatistics() {
        List<User> allUsers = userRepository.findAll();

        Map<String, Object> stats = new HashMap<>();
        stats.put("totalUsers", allUsers.size());
        stats.put("usersWithMultipleRoles", allUsers.stream()
                .filter(u -> u.getRoles().size() > 1)
                .count());
        stats.put("usersWithSingleRole", allUsers.stream()
                .filter(u -> u.getRoles().size() == 1)
                .count());

        Map<Integer, Long> roleCountDistribution = allUsers.stream()
                .collect(Collectors.groupingBy(
                        u -> u.getRoles().size(),
                        Collectors.counting()
                ));
        stats.put("roleCountDistribution", roleCountDistribution);

        Map<Set<Role>, Long> roleCombinations = allUsers.stream()
                .collect(Collectors.groupingBy(
                        User::getRoles,
                        Collectors.counting()
                ));
        stats.put("mostCommonRoleCombinations", roleCombinations);

        return stats;
    }

    // ==================== DISPONIBILITÉ ====================

    @Transactional(readOnly = true)
    public List<UserResponseDTO> getAvailableUsers(Role role, long maxTasks) {
        List<User> users = userRepository.findAvailableUsersByRole(role, maxTasks);
        return userMapper.toResponseDTOList(users);
    }

    @Transactional(readOnly = true)
    public List<UserResponseDTO> getAvailableDevelopers(int maxActiveTasks) {
        List<User> developers = userRepository.findAvailableUsersByRole(Role.DEVELOPER, maxActiveTasks);
        return userMapper.toResponseDTOList(developers);
    }

    @Transactional(readOnly = true)
    public boolean isUserAvailable(Long userId, int maxTaskThreshold) {
        long activeTasks = countUserActiveTasks(userId);
        return activeTasks < maxTaskThreshold;
    }

    @Transactional(readOnly = true)
    public UserResponseDTO getLeastLoadedUserByRole(Role role) {
        List<User> users = userRepository.findByRoleAndIsActiveTrue(role);

        User leastLoadedUser = users.stream()
                .min((u1, u2) -> {
                    long tasks1 = countUserActiveTasks(u1.getId());
                    long tasks2 = countUserActiveTasks(u2.getId());
                    return Long.compare(tasks1, tasks2);
                })
                .orElseThrow(() -> new IllegalStateException("Aucun utilisateur disponible avec le rôle: " + role));

        return userMapper.toResponseDTO(leastLoadedUser);
    }

    @Transactional(readOnly = true)
    public List<UserResponseDTO> getUsersWithoutTasks(Long projectId) {
        List<User> projectUsers = userRepository.findUsersByProjectId(projectId);

        List<User> usersWithoutTasks = projectUsers.stream()
                .filter(user -> countUserActiveTasks(user.getId()) == 0)
                .toList();

        return userMapper.toResponseDTOList(usersWithoutTasks);
    }

    // ==================== UTILITAIRES ====================

    @Transactional(readOnly = true)
    public long countUserActiveTasks(Long userId) {
        return userRepository.countTasksByUserAndStatus(userId, WorkItemStatus.IN_PROGRESS) +
                userRepository.countTasksByUserAndStatus(userId, WorkItemStatus.TODO);
    }

    @Transactional(readOnly = true)
    public boolean userExists(Long userId) {
        return userRepository.existsById(userId);
    }

    @Transactional(readOnly = true)
    public boolean usernameExists(String username) {
        return userRepository.existsByUsername(username);
    }

    @Transactional(readOnly = true)
    public boolean emailExists(String email) {
        return userRepository.existsByEmail(email);
    }

    @Transactional(readOnly = true)
    public boolean requiresPasswordReset(Long userId) {
        User user = findUserByIdOrThrow(userId);
        return user.isPasswordResetRequired();
    }

    public UserResponseDTO markPasswordChanged(Long userId) {
        User user = findUserByIdOrThrow(userId);
        user.setPasswordResetRequired(false);

        User savedUser = userRepository.save(user);
        return userMapper.toResponseDTO(savedUser);
    }

    public void sendWelcomeNotification(Long userId) {
        User user = findUserByIdOrThrow(userId);
        System.out.println("Bienvenue " + user.getUsername() + " ! Email envoyé à: " + user.getEmail());
    }

    // ==================== MÉTHODES PRIVÉES ====================

    private User findUserByIdOrThrow(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Utilisateur non trouvé avec l'ID: " + userId));
    }

    private boolean isValidEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            return false;
        }
        String emailRegex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$";
        return email.matches(emailRegex);
    }

    // ==================== RECORDS (pour statistiques/performance) ====================

    public record UserStatistics(
            String username,
            Set<Role> roles,
            int todoTasks,
            int inProgressTasks,
            int completedTasks,
            int totalTasks,
            double completionRate
    ) {}

    public record UserWorkload(
            Long userId,
            String username,
            Set<Role> roles,
            int activeTasks
    ) {}

    public record TeamWorkload(
            int totalMembers,
            int activeMembers,
            int totalActiveTasks,
            double averageTasksPerMember,
            List<UserWorkload> memberWorkloads
    ) {}

    public record UserPerformance(
            String username,
            Set<Role> roles,
            int tasksCompletedInPeriod,
            int averageTasksPerWeek,
            double overallCompletionRate
    ) {}
}