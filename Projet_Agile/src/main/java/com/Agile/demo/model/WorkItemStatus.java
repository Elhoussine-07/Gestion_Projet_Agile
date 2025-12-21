package com.Agile.demo.model;

public enum WorkItemStatus {
    
    // Backlog & Planning
    BACKLOG("Backlog"),
    TODO("À faire"),
    READY("pret"),

    // Active work
    IN_PROGRESS("En cours"),
    IN_REVIEW("En revue"),
    TESTING("En test"),

    // Blocked / On hold
    ON_HOLD("suspendu"),
    BLOCKED("Bloqué"),

    // Final states
    DONE("Terminé"),
    CANCELLED("Cancelled");

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

    public boolean isReady() {
        return this == READY;
    }

    public boolean isBlocked() {
        return this == BLOCKED || this == ON_HOLD;
    }

    public boolean isFinal() {
        return this == DONE;
    }

    @Override
    public String toString() {
        return displayName;
    }
}