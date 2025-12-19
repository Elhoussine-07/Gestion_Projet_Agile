package com.Agile.demo.planning.prioritization;

import com.Agile.demo.model.IPrioritizationStrategy;
import com.Agile.demo.model.UserStory;

public class WSJFStrategy implements IPrioritizationStrategy {
    @Override
    public int calculatePriority(UserStory story) {
        // WSJF = (Business Value + Time Criticality + Risk Reduction) / Job Size
        int businessValue = story.getMetric("businessValue");
        int timeCriticality = story.getMetric("timeCriticality");
        int riskReduction = story.getMetric("riskReduction");
        int jobSize = story.getStoryPoints();

        if (jobSize == 0) jobSize = 1; // Éviter division par zéro

        return (businessValue + timeCriticality + riskReduction) / jobSize;
    }
}