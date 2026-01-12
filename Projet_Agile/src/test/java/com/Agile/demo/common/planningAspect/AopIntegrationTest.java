package com.Agile.demo.common.planningAspect;

import com.Agile.demo.model.ProductBacklog;
import com.Agile.demo.model.Project;
import com.Agile.demo.model.UserStory;
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
        Project project =  Project.builder().name("Test Project")
                .description("description")
                .startDate(LocalDate.now())
                .endDate(LocalDate.now())
                .build();

        projectRepository.save(project);

        backlog = project.getProductBacklog();
    }

    @Test
    void shouldApplyAllAspectsWhenCreatingUserStory() {
        // Given
        String title = "Integration Test Story";
        String role = "developer";
        String action = "test AOP";
        String purpose = "verify aspects work";
        Integer storyPoints = 5;

        // When
        UserStory story = userStoryService.createUserStory(
                backlog.getId(), title, role, action, purpose, storyPoints
        );

        // Then
        assertThat(story).isNotNull();
        assertThat(story.getId()).isNotNull();
        assertThat(story.getTitle()).isEqualTo(title);

        // Vérifier que l'aspect de logging a fonctionné (dans les logs)
        // Vérifier que l'aspect de performance a mesuré le temps (dans les logs)
    }

    @Test
    void shouldMeasurePerformanceForMultipleStories() {
        // Given
        int numberOfStories = 10;

        // When
        for (int i = 0; i < numberOfStories; i++) {
            userStoryService.createUserStory(
                    backlog.getId(),
                    "Story " + i,
                    "user",
                    "action " + i,
                    "purpose " + i,
                    5
            );
        }

        // Then
        assertThat(userStoryRepository.findAll()).hasSize(numberOfStories);

        // Vérifier dans les logs que chaque création a été mesurée
    }
}