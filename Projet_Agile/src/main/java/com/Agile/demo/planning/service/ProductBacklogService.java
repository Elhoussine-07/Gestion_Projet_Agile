package com.Agile.demo.planning.service;

import com.Agile.demo.common.exception.ResourceNotFoundException;
import com.Agile.demo.common.exception.ValidationException;
import com.Agile.demo.model.*;
import com.Agile.demo.planning.prioritization.PrioritizationStrategyProvider;
import com.Agile.demo.planning.repository.ProductBacklogRepository;
import com.Agile.demo.execution.repositories.SprintBacklogRepository;
import com.Agile.demo.planning.repository.UserStoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class ProductBacklogService {

    private final ProductBacklogRepository productBacklogRepository;
    private final UserStoryRepository userStoryRepository;
    private final SprintBacklogRepository sprintRepository;
    private final PrioritizationStrategyProvider prioritizationStrategyProvider;

    public ProductBacklog getProductBacklogById(Long id) {
        return productBacklogRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("ProductBacklog", id));
    }

    public ProductBacklog getProductBacklogByProject(Long projectId) {
        return productBacklogRepository.findByProjectId(projectId)
                .orElseThrow(() -> new ResourceNotFoundException("ProductBacklog for project " + projectId + " not found"));
    }

    public List<UserStory> getAllStories(Long backlogId) {
        return userStoryRepository.findByProductBacklogId(backlogId);
    }

    public List<UserStory> getUnassignedStories(Long backlogId) {
        return userStoryRepository.findUnassignedStoriesByBacklogId(backlogId);
    }

    public List<UserStory> getTopPriorityStories(Long backlogId, int limit) {
        return userStoryRepository.findByProductBacklogIdOrderedByPriority(backlogId)
                .stream()
                .limit(limit)
                .collect(Collectors.toList());
    }

    /**
     * Applique une stratégie de priorisation à toutes les User Stories du backlog
     * @param backlogId ID du Product Backlog
     * @param method Méthode de priorisation (MOSCOW, WSJF, VALUE_EFFORT)
     */
    @Transactional
    public void applyPrioritization(Long backlogId, PrioritizationMethod method) {
        log.info("Applying prioritization method {} to backlog {}", method, backlogId);

        // Vérifier que le backlog existe
        ProductBacklog backlog = getProductBacklogById(backlogId);

        // Récupérer toutes les stories du backlog
        List<UserStory> stories = getAllStories(backlogId);

        if (stories.isEmpty()) {
            log.warn("No stories found in backlog {}", backlogId);
            return;
        }

        // Valider les stories avant priorisation
        validateStoriesForPrioritization(stories);

        // Obtenir la stratégie de priorisation
        IPrioritizationStrategy strategy = prioritizationStrategyProvider.getStrategy(method);

        // Appliquer la priorisation
        List<UserStory> prioritizedStories = strategy.prioritizeBacklog(stories);

        // Sauvegarder les stories avec leur nouvelle priorité
        userStoryRepository.saveAll(prioritizedStories);

        // Calculer et mettre à jour la valeur métier totale
        updateTotalBusinessValue(backlog);

        log.info("Successfully prioritized {} stories in backlog {}", prioritizedStories.size(), backlogId);
    }

    /**
     * Déplace une User Story vers un Sprint
     * @param storyId ID de la User Story
     * @param sprintId ID du Sprint
     */
    @Transactional
    public void moveStoryToSprint(Long storyId, Long sprintId) {
        log.info("Moving story {} to sprint {}", storyId, sprintId);

        // Récupérer la story
        UserStory story = userStoryRepository.findById(storyId)
                .orElseThrow(() -> new ResourceNotFoundException("UserStory", storyId));

        // Vérifier que la story est valide pour être déplacée
        validateStoryForSprint(story);

        // Récupérer le sprint
        SprintBacklog sprint = sprintRepository.findById(sprintId)
                .orElseThrow(() -> new ResourceNotFoundException("Sprint", sprintId));

        // Vérifier que le sprint n'est pas terminé
        if (sprint.getSprintStatus() == SprintStatus.COMPLETED) {
            throw new ValidationException("Cannot move story to a completed sprint");
        }

        // Vérifier la capacité du sprint
        validateSprintCapacity(sprint, story);

        // Associer la story au sprint
        story.setSprintBacklog(sprint);
        userStoryRepository.save(story);

        log.info("Story {} successfully moved to sprint {}", storyId, sprintId);
    }

    /**
     * Réordonne manuellement les User Stories dans le backlog
     * @param backlogId ID du Product Backlog
     * @param storyIds Liste des IDs dans l'ordre souhaité
     */
    @Transactional
    public void reorderStories(Long backlogId, List<Long> storyIds) {
        log.info("Reordering stories in backlog {}", backlogId);

        // Vérifier que le backlog existe
        getProductBacklogById(backlogId);

        // Récupérer toutes les stories
        List<UserStory> stories = userStoryRepository.findAllById(storyIds);

        if (stories.size() != storyIds.size()) {
            throw new ValidationException("Some story IDs are invalid");
        }

        // Vérifier que toutes les stories appartiennent au backlog
        boolean allBelongToBacklog = stories.stream()
                .allMatch(story -> story.getProductBacklog().getId().equals(backlogId));

        if (!allBelongToBacklog) {
            throw new ValidationException("All stories must belong to the specified backlog");
        }

        // Réassigner les priorités selon l'ordre fourni
        for (int i = 0; i < storyIds.size(); i++) {
            Long storyId = storyIds.get(i);
            UserStory story = stories.stream()
                    .filter(s -> s.getId().equals(storyId))
                    .findFirst()
                    .orElseThrow();

            // Priorité plus élevée = numéro plus bas (1 est plus prioritaire que 10)
            story.setPriority(i + 1);
        }

        userStoryRepository.saveAll(stories);

        log.info("Successfully reordered {} stories in backlog {}", stories.size(), backlogId);
    }

    /**
     * Calcule la valeur métier totale du backlog
     * @param backlogId ID du Product Backlog
     * @return Valeur métier totale
     */
    public Integer calculateTotalBusinessValue(Long backlogId) {
        List<UserStory> stories = getAllStories(backlogId);

        return stories.stream()
                .filter(story -> story.getBusinessValue() != null)
                .mapToInt(UserStory::getBusinessValue)
                .sum();
    }

    /**
     * Valide qu'une User Story respecte les règles métier
     * @param story User Story à valider
     */
    private void validateStory(UserStory story) {
        if (story.getDescription() == null || !story.getDescription().isValid()
        ) {
            throw new ValidationException("User Story must have a description");
        }

        if (story.getStoryPoints() == null || story.getStoryPoints() <= 0) {
            throw new ValidationException("User Story must have valid story points");
        }
    }

    /**
     * Valide une liste de User Stories pour la priorisation
     * @param stories Liste des User Stories
     */
    private void validateStoriesForPrioritization(List<UserStory> stories) {
        for (UserStory story : stories) {
            try {
                validateStory(story);
            } catch (ValidationException e) {
                log.warn("Story {} failed validation: {}", story.getId(), e.getMessage());
                throw new ValidationException("Cannot prioritize: " + e.getMessage() + " (Story ID: " + story.getId() + ")");
            }
        }
    }

    /**
     * Valide qu'une story peut être déplacée vers un sprint
     * @param story User Story à valider
     */
    private void validateStoryForSprint(UserStory story) {
        validateStory(story);

        if (story.getStatus() == WorkItemStatus.DONE) {
            throw new ValidationException("Cannot move a completed story to a sprint");
        }
    }

    private void validateSprintCapacity(SprintBacklog sprint, UserStory story) {
        // Calculer les points déjà alloués au sprint
        List<UserStory> sprintStories = userStoryRepository.findBySprintBacklogId(sprint.getId());
        Integer allocatedPoints = sprintStories.stream()
                .filter(s -> s.getStoryPoints() != null)
                .mapToInt(UserStory::getStoryPoints)
                .sum();

        // Vérifier la capacité (en supposant que Sprint a un attribut capacity)
        Integer sprintCapacity = sprint.getCapacity();
        if (sprintCapacity != null && allocatedPoints + story.getStoryPoints() > sprintCapacity) {
            throw new ValidationException(
                    String.format("Sprint capacity exceeded. Available: %d, Required: %d",
                            sprintCapacity - allocatedPoints,
                            story.getStoryPoints())
            );
        }
    }


    @Transactional
    private void updateTotalBusinessValue(ProductBacklog backlog) {
        Integer totalValue = calculateTotalBusinessValue(backlog.getId());
        backlog.setTotalBusinessValue(totalValue);
        productBacklogRepository.save(backlog);

        log.info("Updated total business value for backlog {}: {}", backlog.getId(), totalValue);
    }
}