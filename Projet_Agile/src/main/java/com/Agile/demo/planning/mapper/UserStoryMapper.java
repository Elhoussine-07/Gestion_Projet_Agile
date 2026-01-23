package com.Agile.demo.planning.mapper;

import com.Agile.demo.model.AcceptanceCriteria;
import com.Agile.demo.model.UserStory;
import com.Agile.demo.model.UserStoryDescription;
import com.Agile.demo.model.WorkItemStatus;
import com.Agile.demo.planning.dto.userstory.CreateUserStoryDTO;
import com.Agile.demo.planning.dto.userstory.CreateUserStoryWithCriteriaDTO;
import com.Agile.demo.planning.dto.userstory.UpdateUserStoryDTO;
import com.Agile.demo.planning.dto.userstory.UserStoryDTO;
import org.mapstruct.*;

import java.util.List;

@Mapper(
        componentModel = MappingConstants.ComponentModel.SPRING,
        injectionStrategy = InjectionStrategy.CONSTRUCTOR,
        unmappedTargetPolicy = ReportingPolicy.ERROR,
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE
)
public interface UserStoryMapper {

    /**
     * Convertit une entité UserStory en UserStoryDTO
     */
    @Mapping(target = "id", expression = "java(toInteger(story.getId()))")
    @Mapping(target = "formattedDescription", source = "story", qualifiedByName = "getFormattedDescription")
    @Mapping(target = "role", source = "description.role")
    @Mapping(target = "action", source = "description.action")
    @Mapping(target = "purpose", source = "description.purpose")
    @Mapping(target = "epicId", source = "epic.id")
    @Mapping(target = "epicTitle", source = "epic.title")
    @Mapping(target = "productBacklogId", source = "productBacklog.id")
    @Mapping(target = "sprintBacklogId", source = "sprintBacklog.id")
    @Mapping(target = "sprintNumber", source = "sprintBacklog.sprintNumber")
    @Mapping(target = "taskCount", expression = "java(getTaskCount(story))")
    @Mapping(target = "completedTaskCount", expression = "java(getCompletedTaskCount(story))")
    @Mapping(target = "progress", expression = "java(story.calculateProgress())")
    @Mapping(target = "isInSprint", expression = "java(story.isInSprint())")
    @Mapping(target = "isValid", expression = "java(story.isValid())")
    @Mapping(target = "givenClauses", source = "acceptanceCriteria.givenClauses")
    @Mapping(target = "whenClauses", source = "acceptanceCriteria.whenClauses")
    @Mapping(target = "thenClauses", source = "acceptanceCriteria.thenClauses")
    @Mapping(target = "gherkinFormat", source = "story", qualifiedByName = "getGherkinFormat")
    @Mapping(target = "customMetrics", source = "customMetrics")
    UserStoryDTO toDto(UserStory story);

    /**
     * Convertit une liste d'entités UserStory en liste de DTOs
     */
    List<UserStoryDTO> toDtoList(List<UserStory> stories);

    /**
     * Convertit un CreateUserStoryDTO en entité UserStory
     * Les relations (ProductBacklog) seront gérées dans le service
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "description", expression = "java(createDescription(dto))")
    @Mapping(target = "acceptanceCriteria", expression = "java(new AcceptanceCriteria())")
    @Mapping(target = "storyPoints", source = "storyPoints")
    @Mapping(target = "priority", constant = "0")
    @Mapping(target = "businessValue", ignore = true)
    @Mapping(target = "customMetrics", ignore = true)
    @Mapping(target = "epic", ignore = true)
    @Mapping(target = "productBacklog", ignore = true)
    @Mapping(target = "sprintBacklog", ignore = true)
    @Mapping(target = "tasks", ignore = true)
    @Mapping(target = "dependencies", ignore = true)
    @Mapping(target = "title", source = "title")
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "createdDate", ignore = true)
    @Mapping(target = "updatedDate", ignore = true)
    UserStory toEntity(CreateUserStoryDTO dto);

    /**
     * Convertit un CreateUserStoryWithCriteriaDTO en entité UserStory
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "description", expression = "java(createDescription(dto.getRole(), dto.getAction(), dto.getPurpose()))")
    @Mapping(target = "acceptanceCriteria", expression = "java(createAcceptanceCriteria(dto))")
    @Mapping(target = "storyPoints", source = "storyPoints")
    @Mapping(target = "priority", constant = "0")
    @Mapping(target = "businessValue", ignore = true)
    @Mapping(target = "customMetrics", ignore = true)
    @Mapping(target = "epic", ignore = true)
    @Mapping(target = "productBacklog", ignore = true)
    @Mapping(target = "sprintBacklog", ignore = true)
    @Mapping(target = "tasks", ignore = true)
    @Mapping(target = "dependencies", ignore = true)
    @Mapping(target = "title", source = "title")
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "createdDate", ignore = true)
    @Mapping(target = "updatedDate", ignore = true)
    UserStory toEntityWithCriteria(CreateUserStoryWithCriteriaDTO dto);

    /**
     * Met à jour une entité UserStory existante avec les données d'un UpdateUserStoryDTO
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "description", expression = "java(createDescription(dto.getRole(), dto.getAction(), dto.getPurpose()))")
    @Mapping(target = "acceptanceCriteria", ignore = true)
    @Mapping(target = "priority", ignore = true)
    @Mapping(target = "businessValue", ignore = true)
    @Mapping(target = "customMetrics", ignore = true)
    @Mapping(target = "epic", ignore = true)
    @Mapping(target = "productBacklog", ignore = true)
    @Mapping(target = "sprintBacklog", ignore = true)
    @Mapping(target = "tasks", ignore = true)
    @Mapping(target = "dependencies", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "createdDate", ignore = true)
    @Mapping(target = "updatedDate", ignore = true)
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateEntityFromDto(UpdateUserStoryDTO dto, @MappingTarget UserStory story);

    // === Méthodes helpers ===

    /**
     * Convertit Long en Integer pour l'ID
     */
    default Integer toInteger(Long value) {
        return value != null ? Math.toIntExact(value) : null;
    }

    /**
     * Calcule le nombre de tâches
     */
    default int getTaskCount(UserStory story) {
        if (story == null || story.getTasks() == null) {
            return 0;
        }
        return story.getTasks().size();
    }

    /**
     * Calcule le nombre de tâches complétées
     */
    default int getCompletedTaskCount(UserStory story) {
        if (story == null || story.getTasks() == null) {
            return 0;
        }
        return (int) story.getTasks().stream()
                .filter(t -> t.getStatus() == WorkItemStatus.DONE)
                .count();
    }

    /**
     * Obtient la description formatée
     */
    @Named("getFormattedDescription")
    default String getFormattedDescription(UserStory story) {
        if (story == null) {
            return "";
        }
        return story.getFormattedDescription();
    }

    /**
     * Obtient le format Gherkin
     */
    @Named("getGherkinFormat")
    default String getGherkinFormat(UserStory story) {
        if (story == null || story.getAcceptanceCriteria() == null) {
            return "";
        }
        return story.getAcceptanceCriteria().toGherkinFormat();
    }

    /**
     * Crée une UserStoryDescription à partir d'un CreateUserStoryDTO
     */
    default UserStoryDescription createDescription(CreateUserStoryDTO dto) {
        if (dto == null) {
            return null;
        }
        return new UserStoryDescription(dto.getRole(), dto.getAction(), dto.getPurpose());
    }

    /**
     * Crée une UserStoryDescription à partir de paramètres
     */
    default UserStoryDescription createDescription(String role, String action, String purpose) {
        if (role == null || action == null || purpose == null) {
            return null;
        }
        return new UserStoryDescription(role, action, purpose);
    }

    /**
     * Crée des AcceptanceCriteria à partir d'un CreateUserStoryWithCriteriaDTO
     */
    default AcceptanceCriteria createAcceptanceCriteria(CreateUserStoryWithCriteriaDTO dto) {
        if (dto == null) {
            return new AcceptanceCriteria();
        }

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

        return criteria;
    }
}