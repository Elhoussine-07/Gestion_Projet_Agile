package com.Agile.demo.model;

/**
 * Statuts possibles d'un élément de travail (UserStory ou Task)
 */
public enum WorkItemStatus {
    TODO("À faire"),
    IN_PROGRESS("En cours"),
    IN_REVIEW("En revue"),
    TESTING("En test"),
    DONE("Terminé"),
    BLOCKED("Bloqué");

    private final String displayName;

    WorkItemStatus(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    public boolean isActive() {
        return this == IN_PROGRESS || this == IN_REVIEW || this == TESTING;
    }

    public boolean isFinal() {
        return this == DONE;
    }

    @Override
    public String toString() {
        return displayName;
    }
}