package com.agile.demo.planning.prioritization;

import com.agile.demo.model.IPrioritizationStrategy;
import com.agile.demo.model.PrioritizationMethod;

public class PrioritizationStrategyFactory {
    public static IPrioritizationStrategy getStrategy(PrioritizationMethod method) {
        return switch (method) {
            case WSJF -> new WSJFStrategy();
            case VALUE_EFFORT -> new ValueEffortStrategy();
            default -> new MoSCowStrategy();
        };
    }
}
