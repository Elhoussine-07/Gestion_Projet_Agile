package com.agile.demo.model;

public enum PrioritizationMethod {
    MOSCOW("MoSCoW"),
    WSJF("Weighted Shortest Job First"),
    VALUE_EFFORT("Value vs Effort");

    private final String displayName;

    PrioritizationMethod(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
