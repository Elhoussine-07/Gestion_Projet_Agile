package com.Agile.demo.model;

import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

/**
 * Value Object représentant les critères d'acceptation au format Gherkin
 * Format: Given-When-Then
 */
@Embeddable
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class AcceptanceCriteria {


    @ElementCollection
    @CollectionTable(
            name = "acceptance_criteria_given",
            joinColumns = @JoinColumn(name = "user_story_id")
    )
    @Column(name = "given_clause", length = 500)
    private List<String> givenClauses = new ArrayList<>();


    @ElementCollection
    @CollectionTable(
            name = "acceptance_criteria_when",
            joinColumns = @JoinColumn(name = "user_story_id")
    )
    @Column(name = "when_clause", length = 500)
    private List<String> whenClauses = new ArrayList<>();


    @ElementCollection
    @CollectionTable(
            name = "acceptance_criteria_then",
            joinColumns = @JoinColumn(name = "user_story_id")
    )
    @Column(name = "then_clause", length = 500)
    private List<String> thenClauses = new ArrayList<>();


    public void addGiven(String clause) {
        if (clause != null && !clause.trim().isEmpty()) {
            givenClauses.add(clause.trim());
        }
    }


    public void addWhen(String clause) {
        if (clause != null && !clause.trim().isEmpty()) {
            whenClauses.add(clause.trim());
        }
    }

    public void addThen(String clause) {
        if (clause != null && !clause.trim().isEmpty()) {
            thenClauses.add(clause.trim());
        }
    }


    public String toGherkinFormat() {
        StringBuilder gherkin = new StringBuilder();

        gherkin.append("Scenario: Acceptance Criteria\n");

        // Given
        if (!givenClauses.isEmpty()) {
            gherkin.append("  Given ").append(givenClauses.get(0)).append("\n");
            for (int i = 1; i < givenClauses.size(); i++) {
                gherkin.append("    And ").append(givenClauses.get(i)).append("\n");
            }
        }

        // When
        if (!whenClauses.isEmpty()) {
            gherkin.append("  When ").append(whenClauses.get(0)).append("\n");
            for (int i = 1; i < whenClauses.size(); i++) {
                gherkin.append("    And ").append(whenClauses.get(i)).append("\n");
            }
        }

        // Then
        if (!thenClauses.isEmpty()) {
            gherkin.append("  Then ").append(thenClauses.get(0)).append("\n");
            for (int i = 1; i < thenClauses.size(); i++) {
                gherkin.append("    And ").append(thenClauses.get(i)).append("\n");
            }
        }

        return gherkin.toString();
    }

    public boolean isValid() {
        return !givenClauses.isEmpty()
                && !whenClauses.isEmpty()
                && !thenClauses.isEmpty();
    }


    public int getTotalClauses() {
        return givenClauses.size() + whenClauses.size() + thenClauses.size();
    }

    @Override
    public String toString() {
        return toGherkinFormat();
    }
}