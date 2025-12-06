package com.Agile.demo.model;

import java.util.Collections;
import java.util.List;

public class ValueVsEffortStrategy implements PrioritizationStrategy {
    @Override
    public void prioritize(List<UserStory> stories) {
        // Tri décroissant sur le ratio (Business Value / Story Points)
        Collections.sort(stories, (us1, us2) -> {
            double ratio1 = (double) us1.getBusinessValue() / us1.getStoryPoints();
            double ratio2 = (double) us2.getBusinessValue() / us2.getStoryPoints();
            return Double.compare(ratio2, ratio1); // ratio2 avant ratio1 pour ordre décroissant
        });
        System.out.println("--- Backlog priorisé par Valeur vs Effort (Meilleur ROI) ---");
    }
}