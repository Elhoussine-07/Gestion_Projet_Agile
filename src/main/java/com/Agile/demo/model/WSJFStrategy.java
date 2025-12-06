package com.Agile.demo.model;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import com.Agile.demo.model.PrioritizationStrategy;
import com.Agile.demo.model.UserStory;

public class WSJFStrategy implements PrioritizationStrategy {

    @Override
    public void prioritize(List<UserStory> stories) {
        // Logique WSJF : calcule le score (Coût du Délai / Durée du Job)
        Collections.sort(stories, (us1, us2) -> {
            double score1 = calculateWSJFScore(us1);
            double score2 = calculateWSJFScore(us2);
            return Double.compare(score2, score1);
        });
        System.out.println("--- Stratégie WSJF appliquée ---");
    }

    private double calculateWSJFScore(UserStory us) {
        double costOfDelay = us.getBusinessValue() + us.getTimeCriticality() + us.getRiskReduction();
        // S'assurer que le Job Size (Story Points) n'est pas zéro
        double duration = us.getStoryPoints() == 0 ? 1 : us.getStoryPoints();
        return costOfDelay / duration;
    }
}