package com.Agile.demo.planning.prioritization;

import com.Agile.demo.model.IPrioritizationStrategy;
import com.Agile.demo.model.PrioritizationMethod;

public class PrioritizationStrategyFactory {
    public static IPrioritizationStrategy getStrategy(PrioritizationMethod method) {
        return switch (method) {
            case WSJF -> new WSJFStrategy();
            case VALUE_EFFORT -> new ValueEffortStrategy();
            default -> new MoSCowStrategy();
        };
    }
}
