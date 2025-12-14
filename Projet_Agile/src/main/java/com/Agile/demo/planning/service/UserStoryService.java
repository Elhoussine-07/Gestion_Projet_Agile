package com.agile.demo.planning.service;

import com.agile.demo.common.exception.ResourceNotFoundException;
import com.agile.demo.model.ProductBacklog;
import com.agile.demo.model.UserStory;
import com.agile.demo.model.UserStoryDescription;
import com.agile.demo.planning.repository.EpicRepository;
import com.agile.demo.planning.repository.ProductBacklogRepository;
import com.agile.demo.planning.repository.UserStoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class UserStoryService {

    private final UserStoryRepository userStoryRepository;
    private final ProductBacklogRepository productBacklogRepository;
    private final EpicRepository epicRepository;

    @Transactional
    public UserStory createUserStory(Long productBacklogId, String title,
                                     String role, String action, String purpose,
                                     Integer storyPoints) {
        log.info("Creating user story: {}", title);

        ProductBacklog backlog = productBacklogRepository.findById(productBacklogId)
                .orElseThrow(() -> new ResourceNotFoundException("ProductBacklog", productBacklogId));

        UserStory story = new UserStory(title, role, action, purpose, storyPoints);
        story.setProductBacklog(backlog);

        return userStoryRepository.save(story);
    }

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

    @Transactional
    public UserStory updateUserStory(Long id, String title, String role,
                                     String action, String purpose, Integer storyPoints) {
        UserStory story = getUserStoryById(id);

        story.setTitle(title);
        story.setDescription(new UserStoryDescription(role, action, purpose));
        story.setStoryPoints(storyPoints);

        return userStoryRepository.save(story);
    }

    @Transactional
    public void updatePriority(Long id, Integer priority) {
        UserStory story = getUserStoryById(id);
        story.setPriority(priority);
        userStoryRepository.save(story);
    }

    @Transactional
    public void deleteUserStory(Long id) {
        UserStory story = getUserStoryById(id);
        userStoryRepository.delete(story);
    }
}