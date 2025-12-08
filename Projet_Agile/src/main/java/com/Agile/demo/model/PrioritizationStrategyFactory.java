package com.Agile.demo.model;

public class PrioritizationStrategyFactory {
    public static IPrioritizationStrategy getStrategy(PrioritizationMethod method) {
        return switch (method) {
            case WSJF -> new WSJFStrategy();
            case VALUE_EFFORT -> new ValueEffortStrategy();
            default -> new MoSCowStrategy();
        };
    }
}
