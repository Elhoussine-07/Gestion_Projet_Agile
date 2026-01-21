package com.Agile.demo.execution.services;

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

    public User createUser(String username, String email, String password, Set<Role> roles,
                           String firstName, String lastName, String phoneNumber) {

        if (userRepository.existsByUsername(username)) {
            throw new IllegalArgumentException("Le nom d'utilisateur existe déjà: " + username);
        }


        if (userRepository.existsByEmail(email)) {
            throw new IllegalArgumentException("L'email existe déjà: " + email);
        }


        if (!isValidEmail(email)) {
            throw new IllegalArgumentException("Format d'email invalide: " + email);
        }


        if (roles == null || roles.isEmpty()) {
            throw new IllegalArgumentException("Au moins un rôle doit être spécifié");
        }

        User user = User.builder()
                .username(username)
                .email(email)
                .password(passwordEncoder.encode(password))
                .roles(new HashSet<>(roles))
                .firstName(firstName)
                .lastName(lastName)
                .phoneNumber(phoneNumber)
                .isActive(true)
                .passwordResetRequired(false)
                .build();

        return userRepository.save(user);
    }

    public User createUser(String username, String email, String password, Role role,
                           String firstName, String lastName, String phoneNumber) {
        return createUser(username, email, password, Set.of(role), firstName, lastName, phoneNumber);
    }

    @Transactional(readOnly = true)
    public User getUserById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Utilisateur non trouvé avec l'ID: " + userId));
    }

    @Transactional(readOnly = true)
    public User getUserByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("Utilisateur non trouvé: " + username));
    }

    @Transactional(readOnly = true)
    public User getUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("Utilisateur non trouvé avec l'email: " + email));
    }

    @Transactional(readOnly = true)
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    @Transactional(readOnly = true)
    public List<User> getUsersByRole(Role role) {
        return userRepository.findByRole(role);
    }

    // ✅ NOUVEAU : Recherche par plusieurs rôles
    @Transactional(readOnly = true)
    public List<User> getUsersByRoles(List<Role> roles) {
        return userRepository.findByRolesIn(roles);
    }

    @Transactional(readOnly = true)
    public List<User> getUsersByProject(Long projectId) {
        return userRepository.findUsersByProjectId(projectId);
    }

    // ✅ MODIFIÉ : Gestion des rôles multiples
    public User updateUser(Long userId, String email, Set<Role> roles, String firstName,
                           String lastName, String phoneNumber, Boolean isActive) {
        User user = getUserById(userId);

        if (email != null && !email.equals(user.getEmail())) {
            if (userRepository.existsByEmail(email)) {
                throw new IllegalArgumentException("L'email existe déjà: " + email);
            }
            if (!isValidEmail(email)) {
                throw new IllegalArgumentException("Format d'email invalide: " + email);
            }
            user.setEmail(email);
        }

        if (roles != null && !roles.isEmpty()) {
            user.setRoles(new HashSet<>(roles));
        }
        if (firstName != null) user.setFirstName(firstName);
        if (lastName != null) user.setLastName(lastName);
        if (phoneNumber != null) user.setPhoneNumber(phoneNumber);
        if (isActive != null) user.setisActive(isActive);

        return userRepository.save(user);
    }

    // ✅ NOUVEAU : Méthode de compatibilité pour un seul rôle
    public User updateUser(Long userId, String email, Role role, String firstName,
                           String lastName, String phoneNumber, Boolean isActive) {
        return updateUser(userId, email, role != null ? Set.of(role) : null,
                firstName, lastName, phoneNumber, isActive);
    }

    // ✅ NOUVEAU : Ajouter un rôle à un utilisateur
    public User addRoleToUser(Long userId, Role role) {
        User user = getUserById(userId);
        user.addRole(role);
        return userRepository.save(user);
    }

    // ✅ NOUVEAU : Retirer un rôle à un utilisateur
    public User removeRoleFromUser(Long userId, Role role) {
        User user = getUserById(userId);

        if (user.getRoles().size() <= 1) {
            throw new IllegalStateException("Un utilisateur doit avoir au moins un rôle");
        }

        user.removeRole(role);
        return userRepository.save(user);
    }

    // ✅ NOUVEAU : Vérifier si un utilisateur a un rôle
    @Transactional(readOnly = true)
    public boolean userHasRole(Long userId, Role role) {
        User user = getUserById(userId);
        return user.hasRole(role);
    }

    // ✅ NOUVEAU : Vérifier si un utilisateur a au moins un des rôles
    @Transactional(readOnly = true)
    public boolean userHasAnyRole(Long userId, Role... roles) {
        User user = getUserById(userId);
        return user.hasAnyRole(roles);
    }

    public User updatePassword(Long userId, String currentPassword, String newPassword) {
        User user = getUserById(userId);

        // Vérifier l'ancien mot de passe
        if (!passwordEncoder.matches(currentPassword, user.getPassword())) {
            throw new IllegalArgumentException("Mot de passe actuel incorrect");
        }

        // Valider le nouveau mot de passe
        if (newPassword == null || newPassword.length() < 8) {
            throw new IllegalArgumentException("Le mot de passe doit contenir au moins 8 caractères");
        }

        user.setPassword(passwordEncoder.encode(newPassword));
        return userRepository.save(user);
    }

    public void deleteUser(Long userId) {
        User user = getUserById(userId);

        // Vérifier que l'utilisateur n'a pas de tâches en cours
        long activeTasks = userRepository.countTasksByUserAndStatus(userId, WorkItemStatus.IN_PROGRESS);
        if (activeTasks > 0) {
            throw new IllegalStateException("Impossible de supprimer un utilisateur avec des tâches en cours");
        }

        userRepository.delete(user);
    }

    @Transactional(readOnly = true)
    public List<User> getAvailableUsers(Role role, long maxTasks) {
        return userRepository.findAvailableUsersByRole(role, maxTasks);
    }

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

    private boolean isValidEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            return false;
        }
        String emailRegex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$";
        return email.matches(emailRegex);
    }

    // ✅ MODIFIÉ : UserStatistics avec Set<Role>
    @Transactional(readOnly = true)
    public UserStatistics getUserStatistics(Long userId) {
        User user = getUserById(userId);

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

    public User activateUser(Long userId) {
        User user = getUserById(userId);
        user.setisActive(true);
        return userRepository.save(user);
    }

    public User deactivateUser(Long userId) {
        User user = getUserById(userId);

        // Vérifier qu'il n'a pas de tâches en cours
        long activeTasks = userRepository.countTasksByUserAndStatus(userId, WorkItemStatus.IN_PROGRESS);
        if (activeTasks > 0) {
            throw new IllegalStateException("Impossible de désactiver un utilisateur avec des tâches en cours");
        }

        user.setisActive(false);
        return userRepository.save(user);
    }

    @Transactional(readOnly = true)
    public List<User> getActiveUsers() {
        return userRepository.findByIsActiveTrue();
    }

    @Transactional(readOnly = true)
    public List<User> getActiveUsersByRole(Role role) {
        return userRepository.findByRoleAndIsActiveTrue(role);
    }

    @Transactional(readOnly = true)
    public List<User> searchUsers(String searchTerm) {
        return userRepository.findByUsernameContainingIgnoreCaseOrEmailContainingIgnoreCase(
                searchTerm, searchTerm);
    }

    @Transactional(readOnly = true)
    public List<User> getAvailableDevelopers(int maxActiveTasks) {
        return userRepository.findAvailableUsersByRole(Role.DEVELOPER, maxActiveTasks);
    }

    // ✅ MODIFIÉ : UserWorkload avec Set<Role>
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
        List<User> teamMembers = getUsersByProject(projectId);

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
    public boolean isUserAvailable(Long userId, int maxTaskThreshold) {
        long activeTasks = countUserActiveTasks(userId);
        return activeTasks < maxTaskThreshold;
    }

    @Transactional(readOnly = true)
    public User getLeastLoadedUserByRole(Role role) {
        List<User> users = getActiveUsersByRole(role);

        return users.stream()
                .min((u1, u2) -> {
                    long tasks1 = countUserActiveTasks(u1.getId());
                    long tasks2 = countUserActiveTasks(u2.getId());
                    return Long.compare(tasks1, tasks2);
                })
                .orElseThrow(() -> new IllegalStateException("Aucun utilisateur disponible avec le rôle: " + role));
    }

    public User updateUserProfile(Long userId, String email, String firstName,
                                  String lastName, String phoneNumber) {
        User user = getUserById(userId);

        if (email != null && !email.equals(user.getEmail())) {
            if (userRepository.existsByEmail(email)) {
                throw new IllegalArgumentException("L'email existe déjà: " + email);
            }
            if (!isValidEmail(email)) {
                throw new IllegalArgumentException("Format d'email invalide: " + email);
            }
            user.setEmail(email);
        }

        if (firstName != null) user.setFirstName(firstName);
        if (lastName != null) user.setLastName(lastName);
        if (phoneNumber != null) user.setPhoneNumber(phoneNumber);

        return userRepository.save(user);
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

    // ✅ NOUVEAU : Statistiques des utilisateurs multi-rôles
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

        // Distribution par nombre de rôles
        Map<Integer, Long> roleCountDistribution = allUsers.stream()
                .collect(Collectors.groupingBy(
                        u -> u.getRoles().size(),
                        Collectors.counting()
                ));
        stats.put("roleCountDistribution", roleCountDistribution);

        // Combinaisons de rôles les plus fréquentes
        Map<Set<Role>, Long> roleCombinations = allUsers.stream()
                .collect(Collectors.groupingBy(
                        User::getRoles,
                        Collectors.counting()
                ));
        stats.put("mostCommonRoleCombinations", roleCombinations);

        return stats;
    }

    public User resetPassword(Long userId, String newPassword) {
        User user = getUserById(userId);

        if (newPassword == null || newPassword.length() < 8) {
            throw new IllegalArgumentException("Le mot de passe doit contenir au moins 8 caractères");
        }

        user.setPassword(passwordEncoder.encode(newPassword));
        user.setPasswordResetRequired(true);
        return userRepository.save(user);
    }

    @Transactional(readOnly = true)
    public boolean requiresPasswordReset(Long userId) {
        User user = getUserById(userId);
        return user.isPasswordResetRequired();
    }

    public User markPasswordChanged(Long userId) {
        User user = getUserById(userId);
        user.setPasswordResetRequired(false);
        return userRepository.save(user);
    }

    @Transactional(readOnly = true)
    public UserPerformance getUserPerformance(Long userId, LocalDate startDate, LocalDate endDate) {
        UserStatistics stats = getUserStatistics(userId);

        int tasksCompletedInPeriod = userRepository.countTasksCompletedByUserBetweenDates(
                userId, startDate, endDate);

        int averageTasksPerWeek = 0;
        long weeksBetween = java.time.temporal.ChronoUnit.WEEKS.between(startDate, endDate);
        if (weeksBetween > 0) {
            averageTasksPerWeek = (int) (tasksCompletedInPeriod / weeksBetween);
        }

        return new UserPerformance(
                stats.username(),
                stats.roles(),
                tasksCompletedInPeriod,
                averageTasksPerWeek,
                stats.completionRate()
        );
    }

    @Transactional(readOnly = true)
    public List<User> getUsersWithoutTasks(Long projectId) {
        List<User> projectUsers = getUsersByProject(projectId);

        return projectUsers.stream()
                .filter(user -> countUserActiveTasks(user.getId()) == 0)
                .toList();
    }

    public void sendWelcomeNotification(Long userId) {
        User user = getUserById(userId);
        // TODO: Implémenter l'envoi d'email/notification
        System.out.println("Bienvenue " + user.getUsername() + " ! Email envoyé à: " + user.getEmail());
    }

    // ✅ MODIFIÉ : Records avec Set<Role>
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