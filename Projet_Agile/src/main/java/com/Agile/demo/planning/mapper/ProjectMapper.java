package com.Agile.demo.planning.mapper;

import com.Agile.demo.model.Project;
import com.Agile.demo.planning.dto.project.CreateProjectDTO;
import com.Agile.demo.planning.dto.project.ProjectDTO;
import com.Agile.demo.planning.dto.project.UpdateProjectDTO;
import org.mapstruct.*;

import java.util.List;

@Mapper(
        componentModel = MappingConstants.ComponentModel.SPRING,
        injectionStrategy = InjectionStrategy.CONSTRUCTOR,
        unmappedTargetPolicy = ReportingPolicy.ERROR,
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE
)
public interface ProjectMapper {

    /**
     * Convertit une entité Project en ProjectDTO
     */
    @Mapping(target = "memberCount", expression = "java(getMemberCount(project))")
    @Mapping(target = "productBacklogId", source = "productBacklog.id")
    @Mapping(target = "sprintCount", expression = "java(getSprintCount(project))")
    ProjectDTO toDto(Project project);

    /**
     * Convertit une liste d'entités Project en liste de DTOs
     */
    List<ProjectDTO> toDtoList(List<Project> projects);

    /**
     * Convertit un CreateProjectDTO en entité Project
     * Le ProductBacklog sera créé automatiquement via @PrePersist
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "productBacklog", ignore = true)
    @Mapping(target = "sprints", ignore = true)
    @Mapping(target = "members", ignore = true)
    Project toEntity(CreateProjectDTO createDto);

    /**
     * Met à jour une entité Project existante avec les données d'un UpdateProjectDTO
     * Seuls les champs non-null du DTO sont appliqués
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "productBacklog", ignore = true)
    @Mapping(target = "sprints", ignore = true)
    @Mapping(target = "members", ignore = true)
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateEntityFromDto(UpdateProjectDTO updateDto, @MappingTarget Project project);

    // === Méthodes helpers pour les calculs ===

    /**
     * Calcule le nombre de membres dans le projet
     */
    default int getMemberCount(Project project) {
        if (project == null || project.getMembers() == null) {
            return 0;
        }
        return project.getMembers().size();
    }

    /**
     * Calcule le nombre de sprints dans le projet
     */
    default int getSprintCount(Project project) {
        if (project == null || project.getSprints() == null) {
            return 0;
        }
        return project.getSprints().size();
    }
}