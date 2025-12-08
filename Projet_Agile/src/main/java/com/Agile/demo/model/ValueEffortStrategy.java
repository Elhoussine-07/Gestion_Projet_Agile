package com.Agile.demo.model;

public class ValueEffortStrategy implements IPrioritizationStrategy {
    @Override
    public int calculatePriority(UserStory story) {
        // Ratio simple : Value / Effort
        int value = story.getBusinessValue();
        int effort = story.getStoryPoints();

        if (effort == 0) effort = 1;

        return (value * 100) / effort;  // Multiplié par 100 pour avoir des entiers
    }
}
