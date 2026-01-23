package com.Agile.demo.planning.service;

import com.Agile.demo.common.planningAspect.LogExecutionTime;
import com.Agile.demo.common.exception.ResourceNotFoundException;
import com.Agile.demo.model.AcceptanceCriteria;
import com.Agile.demo.model.ProductBacklog;
import com.Agile.demo.model.UserStory;
import com.Agile.demo.planning.dto.userstory.*;
import com.Agile.demo.planning.mapper.UserStoryMapper;
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
    private final UserStoryMapper userStoryMapper;

    // ===== CREATE =====

    @Transactional
    @LogExecutionTime(threshold = 500)
    public UserStoryDTO createUserStory(CreateUserStoryDTO dto) {
        log.info("Creating user story: {}", dto.getTitle());

        ProductBacklog backlog = productBacklogRepository.findById(dto.getProductBacklogId())
                .orElseThrow(() -> new ResourceNotFoundException("ProductBacklog", dto.getProductBacklogId()));

        UserStory story = userStoryMapper.toEntity(dto);
        story.setProductBacklog(backlog);

        UserStory saved = userStoryRepository.save(story);
        return userStoryMapper.toDto(saved);
    }

    @Transactional
    public UserStoryDTO createUserStoryWithCriteria(CreateUserStoryWithCriteriaDTO dto) {
        log.info("Creating user story with acceptance criteria: {}", dto.getTitle());

        ProductBacklog backlog = productBacklogRepository.findById(dto.getProductBacklogId())
                .orElseThrow(() -> new ResourceNotFoundException("ProductBacklog", dto.getProductBacklogId()));

        UserStory story = userStoryMapper.toEntityWithCriteria(dto);
        story.setProductBacklog(backlog);

        UserStory saved = userStoryRepository.save(story);
        return userStoryMapper.toDto(saved);
    }

    // ===== READ =====

    @LogExecutionTime(threshold = 100)
    public UserStoryDTO getUserStoryById(Long id) {
        UserStory story = userStoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("UserStory", id));
        return userStoryMapper.toDto(story);
    }

    @LogExecutionTime(threshold = 200)
    public List<UserStoryDTO> getUserStoriesByProductBacklog(Long backlogId) {
        List<UserStory> stories = userStoryRepository.findByProductBacklogId(backlogId);
        return userStoryMapper.toDtoList(stories);
    }

    public List<UserStoryDTO> getUserStoriesByEpic(Long epicId) {
        List<UserStory> stories = userStoryRepository.findByEpicId(epicId);
        return userStoryMapper.toDtoList(stories);
    }

    public List<UserStoryDTO> getUnassignedStories(Long backlogId) {
        List<UserStory> stories = userStoryRepository.findByProductBacklogIdAndEpicIsNull(backlogId);
        return userStoryMapper.toDtoList(stories);
    }

    public List<UserStoryDTO> getStoriesOrderedByPriority(Long backlogId) {
        List<UserStory> stories = userStoryRepository.findByProductBacklogIdOrderedByPriority(backlogId);
        return userStoryMapper.toDtoList(stories);
    }

    public List<UserStoryDTO> getReadyStories(Long backlogId) {
        log.debug("Fetching ready stories for backlog: {}", backlogId);
        List<UserStory> stories = userStoryRepository.findByProductBacklogId(backlogId).stream()
                .filter(UserStory::isValid)
                .filter(story -> story.getStoryPoints() > 0)
                .filter(story -> !story.isInSprint())
                .toList();
        return userStoryMapper.toDtoList(stories);
    }

    public List<UserStoryDTO> getAllUserStories() {
        List<UserStory> stories = userStoryRepository.findAll();
        return userStoryMapper.toDtoList(stories);
    }

    // ===== UPDATE =====

    @Transactional
    @LogExecutionTime(threshold = 300)
    public UserStoryDTO updateUserStory(Long id, UpdateUserStoryDTO dto) {
        UserStory story = userStoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("UserStory", id));

        userStoryMapper.updateEntityFromDto(dto, story);

        UserStory updated = userStoryRepository.save(story);
        return userStoryMapper.toDto(updated);
    }

    @Transactional
    public UserStoryDTO updateAcceptanceCriteria(Long id, UpdateAcceptanceCriteriaDTO dto) {
        log.info("Updating acceptance criteria for story: {}", id);

        UserStory story = userStoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("UserStory", id));

        AcceptanceCriteria criteria = new AcceptanceCriteria();
        if (dto.getGivenClauses() != null) {
            dto.getGivenClauses().forEach(criteria::addGiven);
        }
        if (dto.getWhenClauses() != null) {
            dto.getWhenClauses().forEach(criteria::addWhen);
        }
        if (dto.getThenClauses() != null) {
            dto.getThenClauses().forEach(criteria::addThen);
        }

        story.setAcceptanceCriteria(criteria);

        UserStory updated = userStoryRepository.save(story);
        return userStoryMapper.toDto(updated);
    }

    @Transactional
    public UserStoryDTO addGivenClause(Long id, String clause) {
        UserStory story = userStoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("UserStory", id));

        if (story.getAcceptanceCriteria() == null) {
            story.setAcceptanceCriteria(new AcceptanceCriteria());
        }

        story.getAcceptanceCriteria().addGiven(clause);
        UserStory updated = userStoryRepository.save(story);
        return userStoryMapper.toDto(updated);
    }

    @Transactional
    public UserStoryDTO addWhenClause(Long id, String clause) {
        UserStory story = userStoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("UserStory", id));

        if (story.getAcceptanceCriteria() == null) {
            story.setAcceptanceCriteria(new AcceptanceCriteria());
        }

        story.getAcceptanceCriteria().addWhen(clause);
        UserStory updated = userStoryRepository.save(story);
        return userStoryMapper.toDto(updated);
    }

    @Transactional
    public UserStoryDTO addThenClause(Long id, String clause) {
        UserStory story = userStoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("UserStory", id));

        if (story.getAcceptanceCriteria() == null) {
            story.setAcceptanceCriteria(new AcceptanceCriteria());
        }

        story.getAcceptanceCriteria().addThen(clause);
        UserStory updated = userStoryRepository.save(story);
        return userStoryMapper.toDto(updated);
    }

    @Transactional
    public void updatePriority(Long id, UpdatePriorityDTO dto) {
        UserStory story = userStoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("UserStory", id));
        story.setPriority(dto.getPriority());
        userStoryRepository.save(story);
    }

    @Transactional
    public UserStoryDTO updateMetric(Long id, String metricName, Integer value) {
        log.info("Updating metric {} for story {}: {}", metricName, id, value);

        UserStory story = userStoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("UserStory", id));
        story.setMetric(metricName, value);

        UserStory updated = userStoryRepository.save(story);
        return userStoryMapper.toDto(updated);
    }

    @Transactional
    public UserStoryDTO updateMetrics(Long id, UpdateMetricsDTO dto) {
        log.info("Batch updating {} metrics for story {}", dto.getMetrics().size(), id);

        UserStory story = userStoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("UserStory", id));
        dto.getMetrics().forEach(story::setMetric);

        UserStory updated = userStoryRepository.save(story);
        return userStoryMapper.toDto(updated);
    }

    // ===== DELETE =====

    @Transactional
    public void deleteUserStory(Long id) {
        UserStory story = userStoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("UserStory", id));
        userStoryRepository.delete(story);
    }

    // ===== BUSINESS LOGIC =====

    public boolean isReadyForSprint(Long id) {
        UserStory story = userStoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("UserStory", id));
        return story.isValid()
                && story.getStoryPoints() > 0
                && story.getAcceptanceCriteria() != null
                && story.getAcceptanceCriteria().isValid();
    }

    public String getGherkinFormat(Long id) {
        UserStory story = userStoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("UserStory", id));
        return story.getAcceptanceCriteria() != null
                ? story.getAcceptanceCriteria().toGherkinFormat()
                : "No acceptance criteria defined";
    }
}