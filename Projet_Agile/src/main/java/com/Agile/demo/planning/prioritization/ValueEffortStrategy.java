package com.Agile.demo.planning.prioritization;

import com.Agile.demo.model.IPrioritizationStrategy;
import com.Agile.demo.model.UserStory;

public class ValueEffortStrategy implements IPrioritizationStrategy {
    @Override
    public int calculatePriority(UserStory story) {
        // Ratio simple : Value / Effort
        int value = story.getMetric("value");
        int effort = story.getStoryPoints();

        if (effort == 0) effort = 1;

        return (value * 100) / effort;  // Multiplié par 100 pour avoir des entiers
    }
}
