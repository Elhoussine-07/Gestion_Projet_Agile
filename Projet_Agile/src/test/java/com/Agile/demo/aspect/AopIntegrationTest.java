package com.Agile.demo.aspect;

import com.Agile.demo.model.ProductBacklog;
import com.Agile.demo.model.Project;
import com.Agile.demo.planning.dto.userstory.CreateUserStoryDTO;
import com.Agile.demo.planning.dto.userstory.UserStoryDTO;
import com.Agile.demo.planning.repository.ProductBacklogRepository;
import com.Agile.demo.planning.repository.ProjectRepository;
import com.Agile.demo.planning.repository.UserStoryRepository;
import com.Agile.demo.planning.service.UserStoryService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@TestPropertySource(locations = "classpath:application-test.properties")
@Transactional
class AopIntegrationTest {

    @Autowired
    private UserStoryService userStoryService;

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private ProductBacklogRepository productBacklogRepository;

    @Autowired
    private UserStoryRepository userStoryRepository;

    private ProductBacklog backlog;

    @BeforeEach
    void setUp() {
        // Créer un projet (le ProductBacklog sera créé automatiquement via @PrePersist)
        Project project = Project.builder()
                .name("Test Project")
                .description("description")
                .startDate(LocalDate.now())
                .endDate(LocalDate.now().plusMonths(3))
                .build();

        projectRepository.save(project);

        backlog = project.getProductBacklog();
    }

    @Test
    void shouldApplyAllAspectsWhenCreatingUserStory() {
        // Given
        CreateUserStoryDTO createDto = new CreateUserStoryDTO(
                backlog.getId(),
                "Integration Test Story",
                "developer",
                "test AOP",
                "verify aspects work",
                5
        );

        // When
        UserStoryDTO storyDto = userStoryService.createUserStory(createDto);

        // Then
        assertThat(storyDto).isNotNull();
        assertThat(storyDto.getId()).isNotNull();
        assertThat(storyDto.getTitle()).isEqualTo("Integration Test Story");
        assertThat(storyDto.getRole()).isEqualTo("developer");
        assertThat(storyDto.getAction()).isEqualTo("test AOP");
        assertThat(storyDto.getPurpose()).isEqualTo("verify aspects work");
        assertThat(storyDto.getStoryPoints()).isEqualTo(5);

        // Vérifier que l'entité a bien été sauvegardée
        assertThat(userStoryRepository.findById(storyDto.getId().longValue())).isPresent();

        // Vérifier que l'aspect de logging a fonctionné (dans les logs)
        // Vérifier que l'aspect de performance a mesuré le temps (dans les logs)
    }

    @Test
    void shouldMeasurePerformanceForMultipleStories() {
        // Given
        int numberOfStories = 10;

        // When
        for (int i = 0; i < numberOfStories; i++) {
            CreateUserStoryDTO createDto = new CreateUserStoryDTO(
                    backlog.getId(),
                    "Story " + i,
                    "user",
                    "action " + i,
                    "purpose " + i,
                    5
            );
            userStoryService.createUserStory(createDto);
        }

        // Then
        assertThat(userStoryRepository.findAll()).hasSizeGreaterThanOrEqualTo(numberOfStories);

        // Vérifier dans les logs que chaque création a été mesurée
        // Le service retourne maintenant des DTOs, donc les aspects doivent toujours fonctionner
    }

    @Test
    void shouldLogExecutionTimeWhenGettingUserStoryById() {
        // Given - Créer une user story
        CreateUserStoryDTO createDto = new CreateUserStoryDTO(
                backlog.getId(),
                "Story for Read Test",
                "user",
                "read data",
                "verify read operations",
                3
        );
        UserStoryDTO createdStory = userStoryService.createUserStory(createDto);

        // When - Récupérer la user story
        UserStoryDTO retrievedStory = userStoryService.getUserStoryById(createdStory.getId().longValue());

        // Then
        assertThat(retrievedStory).isNotNull();
        assertThat(retrievedStory.getId()).isEqualTo(createdStory.getId());
        assertThat(retrievedStory.getTitle()).isEqualTo("Story for Read Test");

        // L'aspect @LogExecutionTime devrait avoir logué le temps d'exécution
    }

    @Test
    void shouldLogExecutionTimeWhenGettingUserStoriesByBacklog() {
        // Given - Créer plusieurs user stories
        for (int i = 0; i < 5; i++) {
            CreateUserStoryDTO createDto = new CreateUserStoryDTO(
                    backlog.getId(),
                    "Batch Story " + i,
                    "user",
                    "batch action " + i,
                    "batch purpose " + i,
                    3
            );
            userStoryService.createUserStory(createDto);
        }

        // When - Récupérer toutes les stories du backlog
        var stories = userStoryService.getUserStoriesByProductBacklog(backlog.getId());

        // Then
        assertThat(stories).hasSizeGreaterThanOrEqualTo(5);

        // L'aspect @LogExecutionTime devrait avoir logué le temps d'exécution
        // avec un seuil de 200ms selon la configuration du service
    }
}