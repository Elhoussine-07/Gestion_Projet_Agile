package com.Agile.demo.execution.services;

import com.Agile.demo.model.Role;
import com.Agile.demo.model.User;
import com.Agile.demo.execution.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

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
}