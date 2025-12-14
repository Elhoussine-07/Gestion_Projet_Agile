package com.agile.demo.planning.service;

import com.agile.demo.common.exception.ResourceNotFoundException;
import com.agile.demo.model.ProductBacklog;
import com.agile.demo.model.UserStory;
import com.agile.demo.planning.repository.ProductBacklogRepository;
import com.agile.demo.planning.repository.UserStoryRepository;
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
}