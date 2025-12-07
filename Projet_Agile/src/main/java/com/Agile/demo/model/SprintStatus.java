package com.Agile.demo.model;

public enum SprintStatus {
    PLANNED("Planifié"),
    ACTIVE("Actif"),
    COMPLETED("Terminé"),
    CANCELLED("Annulé");

    private final String displayName;

    SprintStatus(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    //  Simple state queries
    public boolean isActive() {
        return this == ACTIVE;
    }

    public boolean isFinished() {
        return this == COMPLETED || this == CANCELLED;
    }

    //  Simple transition rules (state machine)
    public boolean canTransitionTo(SprintStatus newStatus) {
        switch (this) {
            case PLANNED:
                return newStatus == ACTIVE || newStatus == CANCELLED;
            case ACTIVE:
                return newStatus == COMPLETED || newStatus == CANCELLED;
            case COMPLETED:
            case CANCELLED:
                return false;
            default:
                return false;
        }
    }
}
