package com.Agile.demo.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import jakarta.persistence.*;

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


    public boolean isValid() {
        return role != null && !role.trim().isEmpty()
                && action != null && !action.trim().isEmpty()
                && purpose != null && !purpose.trim().isEmpty();
    }


    public String toShortString() {
        return String.format("En tant que %s, je veux %s", role, action);
    }
}