package com.Agile.demo.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import jakarta.persistence.*;

/**
 * Value Object représentant la description structurée d'une User Story
 * selon le format Agile standard : "En tant que... Je veux... Afin de..."
 */
@Embeddable
@Getter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class UserStoryDescription {

    @Column(name = "role", nullable = false, length = 255)
    private String role;

    @Column(name = "action", nullable = false, length = 1000)
    private String action;

    @Column(name = "purpose", nullable = false, length = 1000)
    private String purpose;


    @Override
    public String toString() {
        return String.format("En tant que %s, je veux %s afin de %s",
                role, action, purpose);
    }

    /**
     * Crée une description à partir d'un texte formaté
     * Utile pour parser une description existante
     *
     * @param formattedDescription La description au format standard
     * @return UserStoryDescription ou null si le format est invalide
     */
    public static UserStoryDescription fromString(String formattedDescription) {
        if (formattedDescription == null || formattedDescription.isEmpty()) {
            return null;
        }

        try {
            // Parse le format "En tant que X, je veux Y afin de Z"
            String[] parts = formattedDescription.split(", je veux | afin de ");

            if (parts.length != 3) {
                return null;
            }

            String role = parts[0].replace("En tant que ", "").trim();
            String action = parts[1].trim();
            String purpose = parts[2].trim();

            return new UserStoryDescription(role, action, purpose);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Vérifie si la description est valide (tous les champs remplis)
     *
     * @return true si tous les champs sont non-vides
     */
    public boolean isValid() {
        return role != null && !role.trim().isEmpty()
                && action != null && !action.trim().isEmpty()
                && purpose != null && !purpose.trim().isEmpty();
    }

    /**
     * Génère une version courte de la description (sans le "afin de")
     *
     * @return Version abrégée
     */
    public String toShortString() {
        return String.format("En tant que %s, je veux %s", role, action);
    }
}