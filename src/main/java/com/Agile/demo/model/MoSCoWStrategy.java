package com.Agile.demo.model;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class MoSCoWStrategy implements PrioritizationStrategy {
    @Override
    public void prioritize(List<UserStory> story) {
        story.sort(Comparator.comparing(UserStory::getMoscowPriority));
        System.out.println("--- Backlog priorisé par MoSCoW (Must -> Won't) ---");
    }
}
