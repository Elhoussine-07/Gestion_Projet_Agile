package com.Agile.demo.execution.services;

import com.Agile.demo.model.Role;
import com.Agile.demo.model.User;
import com.Agile.demo.execution.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Transactional
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    /**
     * Crée un nouvel utilisateur
     */
    public User createUser(String username, String email, String password, Role role) {
        // Vérifier que le nom d'utilisateur n'existe pas déjà
        if (userRepository.existsByUsername(username)) {
            throw new IllegalArgumentException("Le nom d'utilisateur existe déjà: " + username);
        }

        // Vérifier que l'email n'existe pas déjà
        if (userRepository.existsByEmail(email)) {
            throw new IllegalArgumentException("L'email existe déjà: " + email);
        }

        // Valider l'email
        if (!isValidEmail(email)) {
            throw new IllegalArgumentException("Format d'email invalide: " + email);
        }

        User user = new User();
        user.setUsername(username);
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(password));
        user.setRole(role);

        return userRepository.save(user);
    }

    /**
     * Récupère un utilisateur par son ID
     */
    @Transactional(readOnly = true)
    public User getUserById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Utilisateur non trouvé avec l'ID: " + userId));
    }

    /**
     * Récupère un utilisateur par son nom d'utilisateur
     */
    @Transactional(readOnly = true)
    public User getUserByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("Utilisateur non trouvé: " + username));
    }

    /**
     * Récupère un utilisateur par son email
     */
    @Transactional(readOnly = true)
    public User getUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("Utilisateur non trouvé avec l'email: " + email));
    }

    /**
     * Récupère tous les utilisateurs
     */
    @Transactional(readOnly = true)
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    /**
     * Récupère les utilisateurs par rôle
     */
    @Transactional(readOnly = true)
    public List<User> getUsersByRole(Role role) {
        return userRepository.findByRole(role);
    }

    /**
     * Récupère les utilisateurs d'un projet
     */
    @Transactional(readOnly = true)
    public List<User> getUsersByProject(Long projectId) {
        return userRepository.findUsersByProjectId(projectId);
    }

    /**
     * Met à jour les informations d'un utilisateur
     */
    public User updateUser(Long userId, String email, Role role) {
        User user = getUserById(userId);

        // Vérifier que l'email n'est pas déjà utilisé par un autre utilisateur
        if (email != null && !email.equals(user.getEmail())) {
            if (userRepository.existsByEmail(email)) {
                throw new IllegalArgumentException("L'email existe déjà: " + email);
            }
            if (!isValidEmail(email)) {
                throw new IllegalArgumentException("Format d'email invalide: " + email);
            }
            user.setEmail(email);
        }

        if (role != null) {
            user.setRole(role);
        }

        return userRepository.save(user);
    }

    /**
     * Met à jour le mot de passe d'un utilisateur
     */
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

    /**
     * Supprime un utilisateur
     */
    public void deleteUser(Long userId) {
        User user = getUserById(userId);

        // Vérifier que l'utilisateur n'a pas de tâches en cours
        long activeTasks = userRepository.countTasksByUserAndStatus(userId, "IN_PROGRESS");
        if (activeTasks > 0) {
            throw new IllegalStateException("Impossible de supprimer un utilisateur avec des tâches en cours");
        }

        userRepository.delete(user);
    }

    /**
     * Récupère les utilisateurs disponibles (avec peu de tâches)
     */
    @Transactional(readOnly = true)
    public List<User> getAvailableUsers(Role role, long maxTasks) {
        return userRepository.findAvailableUsersByRole(role, maxTasks);
    }

    /**
     * Compte le nombre de tâches assignées à un utilisateur
     */
    @Transactional(readOnly = true)
    public long countUserActiveTasks(Long userId) {
        return userRepository.countTasksByUserAndStatus(userId, "IN_PROGRESS") +
                userRepository.countTasksByUserAndStatus(userId, "TODO");
    }

    /**
     * Vérifie si un utilisateur existe
     */
    @Transactional(readOnly = true)
    public boolean userExists(Long userId) {
        return userRepository.existsById(userId);
    }

    /**
     * Vérifie si un nom d'utilisateur existe
     */
    @Transactional(readOnly = true)
    public boolean usernameExists(String username) {
        return userRepository.existsByUsername(username);
    }

    /**
     * Vérifie si un email existe
     */
    @Transactional(readOnly = true)
    public boolean emailExists(String email) {
        return userRepository.existsByEmail(email);
    }

    /**
     * Valide le format d'un email
     */
    private boolean isValidEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            return false;
        }
        String emailRegex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$";
        return email.matches(emailRegex);
    }

    /**
     * Récupère les statistiques d'un utilisateur
     */
    @Transactional(readOnly = true)
    public UserStatistics getUserStatistics(Long userId) {
        User user = getUserById(userId);

        long todoTasks = userRepository.countTasksByUserAndStatus(userId, "TODO");
        long inProgressTasks = userRepository.countTasksByUserAndStatus(userId, "IN_PROGRESS");
        long doneTasks = userRepository.countTasksByUserAndStatus(userId, "DONE");
        long totalTasks = todoTasks + inProgressTasks + doneTasks;

        double completionRate = totalTasks > 0 ? (doneTasks * 100.0) / totalTasks : 0.0;

        return new UserStatistics(
                user.getUsername(),
                user.getRole(),
                (int) todoTasks,
                (int) inProgressTasks,
                (int) doneTasks,
                (int) totalTasks,
                completionRate
        );
    }

    /**
     * Classe interne pour les statistiques utilisateur
     */
    public record UserStatistics(
            String username,
            Role role,
            int todoTasks,
            int inProgressTasks,
            int completedTasks,
            int totalTasks,
            double completionRate
    ) {}


// Méthodes à ajouter dans UserService

    /**
     * Active un utilisateur
     */
    public User activateUser(Long userId) {
        User user = getUserById(userId);
        user.setActive(true);
        return userRepository.save(user);
    }

    /**
     * Désactive un utilisateur
     */
    public User deactivateUser(Long userId) {
        User user = getUserById(userId);

        // Vérifier qu'il n'a pas de tâches en cours
        long activeTasks = userRepository.countTasksByUserAndStatus(userId, "IN_PROGRESS");
        if (activeTasks > 0) {
            throw new IllegalStateException("Impossible de désactiver un utilisateur avec des tâches en cours");
        }

        user.setActive(false);
        return userRepository.save(user);
    }

    /**
     * Récupère les utilisateurs actifs
     */
    @Transactional(readOnly = true)
    public List<User> getActiveUsers() {
        return userRepository.findByIsActiveTrue();
    }

    /**
     * Récupère les utilisateurs par rôle et statut actif
     */
    @Transactional(readOnly = true)
    public List<User> getActiveUsersByRole(Role role) {
        return userRepository.findByRoleAndIsActiveTrue(role);
    }

    /**
     * Recherche des utilisateurs par nom d'utilisateur ou email
     */
    @Transactional(readOnly = true)
    public List<User> searchUsers(String searchTerm) {
        return userRepository.findByUsernameContainingIgnoreCaseOrEmailContainingIgnoreCase(
                searchTerm, searchTerm);
    }

    /**
     * Récupère les développeurs disponibles pour assignation
     */
    @Transactional(readOnly = true)
    public List<User> getAvailableDevelopers(int maxActiveTasks) {
        return userRepository.findAvailableUsersByRole(Role.DEVELOPER, maxActiveTasks);
    }

    /**
     * Récupère les utilisateurs les plus chargés
     */
    @Transactional(readOnly = true)
    public List<UserWorkload> getMostLoadedUsers(int limit) {
        List<User> users = userRepository.findAll();

        return users.stream()
                .map(user -> {
                    long activeTasks = countUserActiveTasks(user.getId());
                    return new UserWorkload(
                            user.getId(),
                            user.getUsername(),
                            user.getRole(),
                            (int) activeTasks
                    );
                })
                .sorted((a, b) -> Integer.compare(b.activeTasks(), a.activeTasks()))
                .limit(limit)
                .toList();
    }

    /**
     * Récupère la charge de travail d'une équipe
     */
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
                        user.getRole(),
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

    /**
     * Vérifie la disponibilité d'un utilisateur
     */
    @Transactional(readOnly = true)
    public boolean isUserAvailable(Long userId, int maxTaskThreshold) {
        long activeTasks = countUserActiveTasks(userId);
        return activeTasks < maxTaskThreshold;
    }

    /**
     * Récupère l'utilisateur le moins chargé par rôle
     */
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

    /**
     * Met à jour le profil utilisateur complet
     */
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

    /**
     * Récupère les statistiques d'équipe par rôle
     */
    @Transactional(readOnly = true)
    public Map<Role, Integer> getUserCountByRole() {
        Map<Role, Integer> roleCount = new HashMap<>();
        for (Role role : Role.values()) {
            int count = userRepository.countByRole(role);
            roleCount.put(role, count);
        }
        return roleCount;
    }

    /**
     * Réinitialise le mot de passe (pour admin)
     */
    public User resetPassword(Long userId, String newPassword) {
        User user = getUserById(userId);

        if (newPassword == null || newPassword.length() < 8) {
            throw new IllegalArgumentException("Le mot de passe doit contenir au moins 8 caractères");
        }

        user.setPassword(passwordEncoder.encode(newPassword));
        user.setPasswordResetRequired(true);
        return userRepository.save(user);
    }

    /**
     * Vérifie si un utilisateur nécessite un reset de mot de passe
     */
    @Transactional(readOnly = true)
    public boolean requiresPasswordReset(Long userId) {
        User user = getUserById(userId);
        return user.isPasswordResetRequired();
    }

    /**
     * Marque le mot de passe comme changé
     */
    public User markPasswordChanged(Long userId) {
        User user = getUserById(userId);
        user.setPasswordResetRequired(false);
        return userRepository.save(user);
    }

    /**
     * Récupère les performances d'un utilisateur sur une période
     */
    @Transactional(readOnly = true)
    public UserPerformance getUserPerformance(Long userId, LocalDate startDate, LocalDate endDate) {
        UserStatistics stats = getUserStatistics(userId);

        // Récupérer les tâches complétées dans la période
        int tasksCompletedInPeriod = userRepository.countTasksCompletedByUserBetweenDates(
                userId, startDate, endDate);

        int averageTasksPerWeek = 0;
        long weeksBetween = java.time.temporal.ChronoUnit.WEEKS.between(startDate, endDate);
        if (weeksBetween > 0) {
            averageTasksPerWeek = (int) (tasksCompletedInPeriod / weeksBetween);
        }

        return new UserPerformance(
                stats.username(),
                stats.role(),
                tasksCompletedInPeriod,
                averageTasksPerWeek,
                stats.completionRate()
        );
    }

    /**
     * Récupère les utilisateurs n'ayant pas de tâches assignées
     */
    @Transactional(readOnly = true)
    public List<User> getUsersWithoutTasks(Long projectId) {
        List<User> projectUsers = getUsersByProject(projectId);

        return projectUsers.stream()
                .filter(user -> countUserActiveTasks(user.getId()) == 0)
                .toList();
    }

    /**
     * Envoie une notification de bienvenue (à implémenter avec un service de notification)
     */
    public void sendWelcomeNotification(Long userId) {
        User user = getUserById(userId);
        // TODO: Implémenter l'envoi d'email/notification
        System.out.println("Bienvenue " + user.getUsername() + " ! Email envoyé à: " + user.getEmail());
    }

// Classes internes pour les statistiques

    public record UserWorkload(
            Long userId,
            String username,
            Role role,
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
            Role role,
            int tasksCompletedInPeriod,
            int averageTasksPerWeek,
            double overallCompletionRate
    ) {}

// Méthodes à ajouter dans UserRepository :
// List<User> findByIsActiveTrue();
// List<User> findByRoleAndIsActiveTrue(Role role);
// List<User> findByUsernameContainingIgnoreCaseOrEmailContainingIgnoreCase(String username, String email);
// int countByRole(Role role);
// @Query("SELECT COUNT(t) FROM Task t WHERE t.assignedUser.id = :userId AND t.completedDate BETWEEN :startDate AND :endDate AND t.status = 'DONE'")
// int countTasksCompletedByUserBetweenDates(@Param("userId") Long userId, @Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

// Ajouter dans l'entité User :
// private boolean isActive = true;
// private String firstName;
// private String lastName;
// private String phoneNumber;
// private boolean passwordResetRequired = false;



}