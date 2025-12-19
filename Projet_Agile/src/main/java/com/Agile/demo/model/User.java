package com.Agile.demo.model;

import lombok.*;
import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String username;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String password;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;

    // ===== NOUVEAUX CHAMPS À AJOUTER =====

    /**
     * Indique si l'utilisateur est actif
     */
    @Column(name = "is_active", nullable = false)
    private boolean isActive = true;

    /**
     * Prénom de l'utilisateur
     */
    @Column(name = "first_name", length = 50)
    private String firstName;

    /**
     * Nom de famille de l'utilisateur
     */
    @Column(name = "last_name", length = 50)
    private String lastName;

    /**
     * Numéro de téléphone
     */
    @Column(name = "phone_number", length = 20)
    private String phoneNumber;

    /**
     * Indique si un changement de mot de passe est requis
     */
    @Column(name = "password_reset_required", nullable = false)
    private boolean passwordResetRequired = false;

    // ===== RELATIONS (INCHANGÉES) =====

    @ManyToMany(mappedBy = "members", fetch = FetchType.LAZY)
    private List<Project> projects = new ArrayList<>();

    @OneToMany(mappedBy = "assignedUser", fetch = FetchType.LAZY)
    private List<Task> assignedTasks = new ArrayList<>();

    // ===== MÉTHODES UTILITAIRES =====

    /**
     * Retourne le nom complet de l'utilisateur
     */
    public String getFullName() {
        if (firstName != null && lastName != null) {
            return firstName + " " + lastName;
        } else if (firstName != null) {
            return firstName;
        } else if (lastName != null) {
            return lastName;
        }
        return username;
    }

    /**
     * Vérifie si l'utilisateur a un profil complet
     */
    public boolean hasCompleteProfile() {
        return firstName != null && !firstName.trim().isEmpty() &&
                lastName != null && !lastName.trim().isEmpty() &&
                phoneNumber != null && !phoneNumber.trim().isEmpty();
    }

    @Override
    public String toString() {
        return String.format("User{id=%d, username='%s', email='%s', role=%s, active=%s}",
                id, username, email, role, isActive);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof User)) return false;
        User user = (User) o;
        return id != null && id.equals(user.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}