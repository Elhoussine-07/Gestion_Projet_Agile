package com.Agile.demo.planning.prioritization;

import com.Agile.demo.model.IPrioritizationStrategy;
import com.Agile.demo.model.PrioritizationMethod;

public interface PrioritizationStrategyProvider {
    IPrioritizationStrategy getStrategy(PrioritizationMethod method);
}
