package com.Agile.demo.model;

import java.util.List;

public interface PrioritizationStrategy {
    public void prioritize(List<UserStory> stories);
}
