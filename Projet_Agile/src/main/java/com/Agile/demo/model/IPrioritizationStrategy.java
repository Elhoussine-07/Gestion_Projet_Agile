package com.Agile.demo.model;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public interface IPrioritizationStrategy {
    int calculatePriority(UserStory story);


    default List<UserStory> prioritizeBacklog(List<UserStory> stories) {
        return stories.stream()
                .sorted(Comparator.comparingInt(this::calculatePriority).reversed())
                .collect(Collectors.toList());
    }}
