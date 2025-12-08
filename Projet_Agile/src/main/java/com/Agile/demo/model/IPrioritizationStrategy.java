package com.Agile.demo.model;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public interface IPrioritizationStrategy {
    int calculatePriority(UserStory story);

    /**
     * Priorise une liste de User Stories
     * Implémentation par défaut : trie par score décroissant
     *
     * @param stories Liste des User Stories à prioriser
     * @return Liste triée par ordre de priorité (plus prioritaire en premier)
     */
    default List<UserStory> prioritizeBacklog(List<UserStory> stories) {
        return stories.stream()
                .sorted(Comparator.comparingInt(this::calculatePriority).reversed())
                .collect(Collectors.toList());
    }}
