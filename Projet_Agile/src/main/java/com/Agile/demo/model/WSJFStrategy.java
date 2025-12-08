package com.Agile.demo.model;

public class WSJFStrategy implements IPrioritizationStrategy {
    @Override
    public int calculatePriority(UserStory story) {
        // WSJF = (Business Value + Time Criticality + Risk Reduction) / Job Size
        int businessValue = story.getBusinessValue();
        int timeCriticality = story.getTimeCriticality();
        int riskReduction = story.getRiskReduction();
        int jobSize = story.getStoryPoints();

        if (jobSize == 0) jobSize = 1; // Éviter division par zéro

        return (businessValue + timeCriticality + riskReduction) / jobSize;
    }
}