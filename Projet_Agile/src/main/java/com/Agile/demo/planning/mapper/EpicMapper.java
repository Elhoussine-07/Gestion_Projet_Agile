package com.Agile.demo.planning.mapper;

import com.Agile.demo.model.Epic;
import com.Agile.demo.model.ProductBacklog;
import com.Agile.demo.model.WorkItemStatus;
import com.Agile.demo.planning.dto.epic.CreateEpicDTO;
import com.Agile.demo.planning.dto.epic.EpicDTO;
import com.Agile.demo.planning.dto.epic.UpdateEpicDTO;
import org.mapstruct.*;

import java.util.List;

@Mapper(
        componentModel = MappingConstants.ComponentModel.SPRING,
        injectionStrategy = InjectionStrategy.CONSTRUCTOR,
        unmappedTargetPolicy = ReportingPolicy.ERROR,
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE
)
public interface EpicMapper {

    /**
     * Convertit une entité Epic en EpicDTO avec calcul de progression
     */
    @Mapping(target = "productBacklogId", source = "productBacklog.id")
    @Mapping(target = "userStoryCount", expression = "java(getUserStoryCount(epic))")
    @Mapping(target = "completedStoryCount", expression = "java(getCompletedStoryCount(epic))")
    @Mapping(target = "progress", expression = "java(calculateProgress(epic))")
    EpicDTO toDto(Epic epic);

    /**
     * Convertit une liste d'entités Epic en liste de DTOs
     */
    List<EpicDTO> toDtoList(List<Epic> epics);

    /**
     * Convertit un CreateEpicDTO en entité Epic (mapping partiel)
     * Le ProductBacklog doit être assigné manuellement dans le service
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "userStories", ignore = true)
    @Mapping(target = "productBacklog", ignore = true)
    Epic toEntity(CreateEpicDTO createDto);

    /**
     * Met à jour une entité Epic existante avec les données d'un UpdateEpicDTO
     * Seuls les champs non-null du DTO sont appliqués
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "userStories", ignore = true)
    @Mapping(target = "productBacklog", ignore = true)
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateEntityFromDto(UpdateEpicDTO updateDto, @MappingTarget Epic epic);

    // === Méthodes de mapping personnalisées ===

    /**
     * Mappe l'ID du ProductBacklog vers l'entité complète
     * Utilisé pour les mappings inverses si nécessaire
     */
    @Named("mapProductBacklogId")
    default ProductBacklog mapProductBacklogId(Long productBacklogId) {
        if (productBacklogId == null) {
            return null;
        }
        ProductBacklog backlog = new ProductBacklog();
        backlog.setId(productBacklogId);
        return backlog;
    }

    // === Méthodes helpers pour les calculs de progression ===

    /**
     * Calcule le nombre total de user stories dans l'epic
     */
    default int getUserStoryCount(Epic epic) {
        if (epic == null || epic.getUserStories() == null) {
            return 0;
        }
        return epic.getUserStories().size();
    }

    /**
     * Calcule le nombre de user stories complétées (DONE)
     */
    default int getCompletedStoryCount(Epic epic) {
        if (epic == null || epic.getUserStories() == null) {
            return 0;
        }
        return (int) epic.getUserStories().stream()
                .filter(us -> us.getStatus() == WorkItemStatus.DONE)
                .count();
    }

    /**
     * Calcule le pourcentage de progression de l'epic
     * @return Valeur entre 0 et 100
     */
    default int calculateProgress(Epic epic) {
        int totalStories = getUserStoryCount(epic);
        if (totalStories == 0) {
            return 0;
        }
        int completedStories = getCompletedStoryCount(epic);
        return (int) Math.round((completedStories * 100.0) / totalStories);
    }
}