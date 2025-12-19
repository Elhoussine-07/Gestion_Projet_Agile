package com.Agile.demo.planning.service;

import com.Agile.demo.common.exception.BusinessException;
import com.Agile.demo.common.exception.ResourceNotFoundException;
import com.Agile.demo.model.Epic;
import com.Agile.demo.model.ProductBacklog;
import com.Agile.demo.model.UserStory;
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

    @Transactional
    public Epic createEpic(Long productBacklogId, String title, String description) {
        log.info("Creating epic: {} for backlog: {}", title, productBacklogId);

        // Validation
        ProductBacklog backlog = productBacklogRepository.findById(productBacklogId)
                .orElseThrow(() -> new ResourceNotFoundException("ProductBacklog", productBacklogId));

        if (epicRepository.existsByTitleAndProductBacklogId(title, productBacklogId)) {
            throw new BusinessException("Epic with title '" + title + "' already exists in this backlog");
        }

        // Création
        Epic epic = new Epic();
        epic.setTitle(title);
        epic.setDescription(description);
        epic.setProductBacklog(backlog);

        return epicRepository.save(epic);
    }

    public Epic getEpicById(Long id) {
        return epicRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Epic", id));
    }

    public List<Epic> getEpicsByProductBacklog(Long productBacklogId) {
        return epicRepository.findByProductBacklogId(productBacklogId);
    }

    @Transactional
    public Epic updateEpic(Long id, String title, String description) {
        Epic epic = getEpicById(id);

        // Vérifier si nouveau titre existe déjà
        if (!epic.getTitle().equals(title) &&
                epicRepository.existsByTitleAndProductBacklogId(title, epic.getProductBacklog().getId())) {
            throw new BusinessException("Epic with this title already exists");
        }

        epic.setTitle(title);
        epic.setDescription(description);

        return epicRepository.save(epic);
    }

    @Transactional
    public void deleteEpic(Long id) {
        Epic epic = getEpicById(id);

        // Créer une copie des user stories pour éviter ConcurrentModificationException
        List<UserStory> stories = List.copyOf(epic.getUserStories());
        for (UserStory us : stories) {
            us.setEpic(null);
            userStoryRepository.save(us); // si nécessaire
        }

        epicRepository.delete(epic);
    }


    @Transactional
    public void addUserStoryToEpic(Long epicId, Long userStoryId) {
        Epic epic = getEpicById(epicId);
        UserStory story = userStoryRepository.findById(userStoryId)
                .orElseThrow(() -> new ResourceNotFoundException("UserStory", userStoryId));

        if (story.getEpic() != null) {
            throw new BusinessException("User story is already assigned to an epic");
        }

        story.setEpic(epic);
        userStoryRepository.save(story);
    }
}
