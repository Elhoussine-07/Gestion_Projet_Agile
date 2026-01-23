package com.Agile.demo.planning.mapper;

import com.Agile.demo.model.ProductBacklog;
import com.Agile.demo.model.Project;
import com.Agile.demo.planning.dto.productbacklog.ProductBacklogDTO;
import com.Agile.demo.planning.dto.productbacklog.UpdateProductBacklogDTO;
import org.mapstruct.*;

import java.util.List;

@Mapper(
        componentModel = MappingConstants.ComponentModel.SPRING,
        injectionStrategy = InjectionStrategy.CONSTRUCTOR,
        unmappedTargetPolicy = ReportingPolicy.ERROR,
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE
)
public interface ProductBacklogMapper {

    /**
     * Convertit une entité ProductBacklog en ProductBacklogDTO
     */
    @Mapping(target = "projectId", source = "project.id")
    @Mapping(target = "projectName", source = "project.name")
    @Mapping(target = "epicCount", expression = "java(getEpicCount(backlog))")
    @Mapping(target = "userStoryCount", expression = "java(getUserStoryCount(backlog))")
    @Mapping(target = "unassignedStoryCount", expression = "java(getUnassignedStoryCount(backlog))")
    ProductBacklogDTO toDto(ProductBacklog backlog);

    /**
     * Convertit une liste d'entités ProductBacklog en liste de DTOs
     */
    //List<ProductBacklogDTO> toDtoList(List<ProductBacklog> backlogs);

    /**
     * Met à jour une entité ProductBacklog existante
     * Seul le nom et la méthode de priorisation peuvent être modifiés
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "stories", ignore = true)
    @Mapping(target = "epics", ignore = true)
    @Mapping(target = "project", ignore = true)
    @Mapping(target = "prioritizationStrategy", ignore = true)
    @Mapping(target = "totalBusinessValue", ignore = true)
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateEntityFromDto(UpdateProductBacklogDTO updateDto, @MappingTarget ProductBacklog backlog);

    // === Méthodes helpers pour les calculs ===

    /**
     * Calcule le nombre d'epics
     */
    default int getEpicCount(ProductBacklog backlog) {
        if (backlog == null || backlog.getEpics() == null) {
            return 0;
        }
        return backlog.getEpics().size();
    }

    /**
     * Calcule le nombre total de user stories
     */
    default int getUserStoryCount(ProductBacklog backlog) {
        if (backlog == null || backlog.getStories() == null) {
            return 0;
        }
        return backlog.getStories().size();
    }

    /**
     * Calcule le nombre de user stories non assignées à un sprint
     */
    default int getUnassignedStoryCount(ProductBacklog backlog) {
        if (backlog == null || backlog.getStories() == null) {
            return 0;
        }
        return (int) backlog.getStories().stream()
                .filter(us -> us.getSprintBacklog() == null)
                .count();
    }
}