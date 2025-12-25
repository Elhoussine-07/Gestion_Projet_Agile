package com.Agile.demo.planning.service;

import com.Agile.demo.common.exception.ResourceNotFoundException;
import com.Agile.demo.model.AcceptanceCriteria;
import com.Agile.demo.model.ProductBacklog;
import com.Agile.demo.model.UserStory;
import com.Agile.demo.model.UserStoryDescription;
import com.Agile.demo.planning.repository.EpicRepository;
import com.Agile.demo.planning.repository.ProductBacklogRepository;
import com.Agile.demo.planning.repository.UserStoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class UserStoryService {

    private final UserStoryRepository userStoryRepository;
    private final ProductBacklogRepository productBacklogRepository;
    private final EpicRepository epicRepository;

    // ===== CREATE =====

    @Transactional
    public UserStory createUserStory(Long productBacklogId, String title,
                                     String role, String action, String purpose,
                                     Integer storyPoints) {
        log.info("Creating user story: {}", title);

        ProductBacklog backlog = productBacklogRepository.findById(productBacklogId)
                .orElseThrow(() -> new ResourceNotFoundException("ProductBacklog", productBacklogId));

        UserStory story = UserStory.builder()
                .title(title)
                .description(new UserStoryDescription(role, action, purpose))
                .acceptanceCriteria(new AcceptanceCriteria())
                .storyPoints(storyPoints)
                .build();

        story.setProductBacklog(backlog);

        return userStoryRepository.save(story);
    }

    //  Créer avec critères d'acceptation
    @Transactional
    public UserStory createUserStoryWithCriteria(Long productBacklogId, String title,
                                                 String role, String action, String purpose,
                                                 List<String> givenClauses,
                                                 List<String> whenClauses,
                                                 List<String> thenClauses,
                                                 Integer storyPoints) {
        log.info("Creating user story with acceptance criteria: {}", title);

        ProductBacklog backlog = productBacklogRepository.findById(productBacklogId)
                .orElseThrow(() -> new ResourceNotFoundException("ProductBacklog", productBacklogId));

        // Créer les critères
        AcceptanceCriteria criteria = new AcceptanceCriteria();
        givenClauses.forEach(criteria::addGiven);
        whenClauses.forEach(criteria::addWhen);
        thenClauses.forEach(criteria::addThen);

        UserStory story = UserStory.builder()
                .title(title)
                .description(new UserStoryDescription(role, action, purpose))
                .acceptanceCriteria(criteria)
                .storyPoints(storyPoints)
                .build();

        story.setProductBacklog(backlog);

        return userStoryRepository.save(story);
    }

    // ===== READ =====

    public UserStory getUserStoryById(Long id) {
        return userStoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("UserStory", id));
    }

    public List<UserStory> getUserStoriesByProductBacklog(Long backlogId) {
        return userStoryRepository.findByProductBacklogId(backlogId);
    }

    public List<UserStory> getUserStoriesByEpic(Long epicId) {
        return userStoryRepository.findByEpicId(epicId);
    }

    public List<UserStory> getUnassignedStories(Long backlogId) {
        return userStoryRepository.findByProductBacklogIdAndEpicIsNull(backlogId);
    }

    public List<UserStory> getStoriesOrderedByPriority(Long backlogId) {
        return userStoryRepository.findByProductBacklogIdOrderedByPriority(backlogId);
    }

    //  Récupérer stories prêtes pour le sprint (Definition of Ready)
    public List<UserStory> getReadyStories(Long backlogId) {
        log.debug("Fetching ready stories for backlog: {}", backlogId);
        return userStoryRepository.findByProductBacklogId(backlogId).stream()
                .filter(UserStory::isValid)
                .filter(story -> story.getStoryPoints() > 0)
                .filter(story -> !story.isInSprint())
                .toList();
    }

    // ===== UPDATE =====

    @Transactional
    public UserStory updateUserStory(Long id, String title, String role,
                                     String action, String purpose, Integer storyPoints) {
        UserStory story = getUserStoryById(id);

        story.setTitle(title);
        story.setDescription(new UserStoryDescription(role, action, purpose));
        story.setStoryPoints(storyPoints);

        return userStoryRepository.save(story);
    }

    //  Mettre à jour les critères d'acceptation
    @Transactional
    public UserStory updateAcceptanceCriteria(Long id,
                                              List<String> givenClauses,
                                              List<String> whenClauses,
                                              List<String> thenClauses) {
        log.info("Updating acceptance criteria for story: {}", id);

        UserStory story = getUserStoryById(id);

        // Réinitialiser et remplir les critères
        AcceptanceCriteria criteria = new AcceptanceCriteria();
        givenClauses.forEach(criteria::addGiven);
        whenClauses.forEach(criteria::addWhen);
        thenClauses.forEach(criteria::addThen);

        story.setAcceptanceCriteria(criteria);

        return userStoryRepository.save(story);
    }

    //  Ajouter une clause Given
    @Transactional
    public UserStory addGivenClause(Long id, String clause) {
        UserStory story = getUserStoryById(id);

        if (story.getAcceptanceCriteria() == null) {
            story.setAcceptanceCriteria(new AcceptanceCriteria());
        }

        story.getAcceptanceCriteria().addGiven(clause);
        return userStoryRepository.save(story);
    }

    //  Ajouter une clause When
    @Transactional
    public UserStory addWhenClause(Long id, String clause) {
        UserStory story = getUserStoryById(id);

        if (story.getAcceptanceCriteria() == null) {
            story.setAcceptanceCriteria(new AcceptanceCriteria());
        }

        story.getAcceptanceCriteria().addWhen(clause);
        return userStoryRepository.save(story);
    }

    //  Ajouter une clause Then
    @Transactional
    public UserStory addThenClause(Long id, String clause) {
        UserStory story = getUserStoryById(id);

        if (story.getAcceptanceCriteria() == null) {
            story.setAcceptanceCriteria(new AcceptanceCriteria());
        }

        story.getAcceptanceCriteria().addThen(clause);
        return userStoryRepository.save(story);
    }

    @Transactional
    public void updatePriority(Long id, Integer priority) {
        UserStory story = getUserStoryById(id);
        story.setPriority(priority);
        userStoryRepository.save(story);
    }

    //  Mettre à jour les métriques personnalisées
    @Transactional
    public UserStory updateMetric(Long id, String metricName, Integer value) {
        log.info("Updating metric {} for story {}: {}", metricName, id, value);

        UserStory story = getUserStoryById(id);
        story.setMetric(metricName, value);

        return userStoryRepository.save(story);
    }

    //  Batch update de métriques
    @Transactional
    public UserStory updateMetrics(Long id, Map<String, Integer> metrics) {
        log.info("Batch updating {} metrics for story {}", metrics.size(), id);

        UserStory story = getUserStoryById(id);
        metrics.forEach(story::setMetric);

        return userStoryRepository.save(story);
    }

    // ===== DELETE =====

    @Transactional
    public void deleteUserStory(Long id) {
        UserStory story = getUserStoryById(id);
        userStoryRepository.delete(story);
    }

    // ===== BUSINESS LOGIC =====

    //   Vérifier si story est prête pour le sprint
    public boolean isReadyForSprint(Long id) {
        UserStory story = getUserStoryById(id);
        return story.isValid()
                && story.getStoryPoints() > 0
                && story.getAcceptanceCriteria() != null
                && story.getAcceptanceCriteria().isValid();
    }

    //  Obtenir le format Gherkin
    public String getGherkinFormat(Long id) {
        UserStory story = getUserStoryById(id);
        return story.getAcceptanceCriteria() != null
                ? story.getAcceptanceCriteria().toGherkinFormat()
                : "No acceptance criteria defined";
    }


    public List<UserStory> getAllUserStories() {
        return userStoryRepository.findAll();
    }
}