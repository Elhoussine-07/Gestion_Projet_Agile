package com.Agile.demo.planning.service;

import com.Agile.demo.common.exception.BusinessException;
import com.Agile.demo.common.exception.ResourceNotFoundException;
import com.Agile.demo.model.Epic;
import com.Agile.demo.model.ProductBacklog;
import com.Agile.demo.model.UserStory;
import com.Agile.demo.planning.dto.epic.CreateEpicDTO;
import com.Agile.demo.planning.dto.epic.EpicDTO;
import com.Agile.demo.planning.dto.epic.UpdateEpicDTO;
import com.Agile.demo.planning.mapper.EpicMapper;
import com.Agile.demo.planning.repository.EpicRepository;
import com.Agile.demo.planning.repository.ProductBacklogRepository;
import com.Agile.demo.planning.repository.UserStoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class EpicService {

    private final EpicRepository epicRepository;
    private final ProductBacklogRepository productBacklogRepository;
    private final UserStoryRepository userStoryRepository;
    private final EpicMapper epicMapper;

    @Transactional
    public EpicDTO createEpic(CreateEpicDTO createDto) {
        log.info("Creating epic: {} for backlog: {}", createDto.getTitle(), createDto.getProductBacklogId());

        // Validation
        ProductBacklog backlog = productBacklogRepository.findById(createDto.getProductBacklogId())
                .orElseThrow(() -> new ResourceNotFoundException("ProductBacklog", createDto.getProductBacklogId()));

        if (epicRepository.existsByTitleAndProductBacklogId(createDto.getTitle(), createDto.getProductBacklogId())) {
            throw new BusinessException("Epic with title '" + createDto.getTitle() + "' already exists in this backlog");
        }

        // Mapping DTO -> Entity
        Epic epic = epicMapper.toEntity(createDto);
        epic.setProductBacklog(backlog);

        // Sauvegarde et conversion Entity -> DTO
        Epic savedEpic = epicRepository.save(epic);
        return epicMapper.toDto(savedEpic);
    }

    public EpicDTO getEpicById(Long id) {
        Epic epic = findEpicById(id);
        return epicMapper.toDto(epic);
    }

    public List<EpicDTO> getEpicsByProductBacklog(Long productBacklogId) {
        List<Epic> epics = epicRepository.findByProductBacklogId(productBacklogId);
        return epicMapper.toDtoList(epics);
    }

    @Transactional
    public EpicDTO updateEpic(Long id, UpdateEpicDTO updateDto) {
        Epic epic = findEpicById(id);

        // Vérifier si nouveau titre existe déjà
        if (!epic.getTitle().equals(updateDto.getTitle()) &&
                epicRepository.existsByTitleAndProductBacklogId(updateDto.getTitle(), epic.getProductBacklog().getId())) {
            throw new BusinessException("Epic with this title already exists");
        }

        // Mise à jour via mapper
        epicMapper.updateEntityFromDto(updateDto, epic);

        Epic updatedEpic = epicRepository.save(epic);
        return epicMapper.toDto(updatedEpic);
    }

    @Transactional
    public void deleteEpic(Long id) {
        Epic epic = findEpicById(id);

        // Créer une copie des user stories pour éviter ConcurrentModificationException
        List<UserStory> stories = List.copyOf(epic.getUserStories());
        for (UserStory us : stories) {
            us.setEpic(null);
            userStoryRepository.save(us);
        }

        epicRepository.delete(epic);
    }

    @Transactional
    public void addUserStoryToEpic(Long epicId, Long userStoryId) {
        Epic epic = findEpicById(epicId);
        UserStory story = userStoryRepository.findById(userStoryId)
                .orElseThrow(() -> new ResourceNotFoundException("UserStory", userStoryId));

        if (story.getEpic() != null) {
            throw new BusinessException("User story is already assigned to an epic");
        }

        story.setEpic(epic);
        userStoryRepository.save(story);
    }

    @Transactional
    public void removeUserStoryFromEpic(Long epicId, Long storyId) {
        Epic epic = findEpicById(epicId);

        UserStory story = userStoryRepository.findById(storyId)
                .orElseThrow(() -> new ResourceNotFoundException("UserStory", storyId));

        if (story.getEpic() == null || !story.getEpic().getId().equals(epic.getId())) {
            throw new BusinessException("User story is not assigned to this epic");
        }

        // Suppression de l'association
        story.setEpic(null);
        userStoryRepository.save(story);
    }

    public int calculateEpicProgress(Long epicId) {
        Epic epic = findEpicById(epicId);
        return epicMapper.calculateProgress(epic);
    }

    public List<EpicDTO> getAllEpics() {
        List<Epic> epics = epicRepository.findAll();
        return epicMapper.toDtoList(epics);
    }

    // === Méthode utilitaire privée ===

    private Epic findEpicById(Long id) {
        return epicRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Epic", id));
    }
}