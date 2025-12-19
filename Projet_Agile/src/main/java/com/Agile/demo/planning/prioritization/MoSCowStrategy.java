package com.Agile.demo.planning.prioritization;

import com.Agile.demo.model.IPrioritizationStrategy;
import com.Agile.demo.model.UserStory;

public class MoSCowStrategy implements IPrioritizationStrategy {
    @Override
    public int calculatePriority(UserStory story) {
        // Basé sur des critères métier (valeur, urgence, dépendances)
        int businessValue = story.getBusinessValue();  // 1-10
        int urgency = story.getUrgency();              // 1-10
        int dependencies = story.getDependencies().size();

        // Formule de score
        return (int) ((businessValue * 2) + (urgency * 1.5) - (dependencies * 0.5));
    }

    // Méthode helper pour classifier après coup
    public String getMoSCowCategory(UserStory story) {
        int score = calculatePriority(story);
        if (score >= 15) return "MUST_HAVE";
        if (score >= 10) return "SHOULD_HAVE";
        if (score >= 5) return "COULD_HAVE";
        return "WONT_HAVE";
    }
}