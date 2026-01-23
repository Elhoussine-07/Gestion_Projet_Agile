package com.Agile.demo.execution.dto.mapper;

import com.Agile.demo.execution.dto.SprintBacklogResponseDTO;
import com.Agile.demo.execution.dto.SprintCreateRequest;
import com.Agile.demo.execution.dto.SprintUpdateRequest;
import com.Agile.demo.model.SprintBacklog;
import org.mapstruct.*;

import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface SprintMapper {

    // ==================== TO ENTITY ====================
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "project", ignore = true)
    @Mapping(target = "sprintStatus", ignore = true)
    @Mapping(target = "userStories", ignore = true)
    @Mapping(source = "sprintNumber", target = "sprintNumber")
    @Mapping(source = "startDate", target = "startDate")
    @Mapping(source = "endDate", target = "endDate")
    @Mapping(source = "goal", target = "goal")
    SprintBacklog toEntity(SprintCreateRequest request);

    // ==================== TO DTO ====================
    // Seuls les champs présents dans SprintBacklogResponseDTO sont mappés ici
    @Mapping(source = "id", target = "id")
    @Mapping(source = "sprintNumber", target = "sprintNumber")
    @Mapping(source = "startDate", target = "startDate")
    @Mapping(source = "endDate", target = "endDate")
    @Mapping(source = "goal", target = "goal")
    @Mapping(source = "sprintStatus", target = "status")
    @Mapping(source = "project.id", target = "projectId")
    SprintBacklogResponseDTO toDto(SprintBacklog sprint);

    List<SprintBacklogResponseDTO> toDtoList(List<SprintBacklog> sprints);

    // ==================== UPDATE ====================
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "project", ignore = true)
    @Mapping(target = "sprintStatus", ignore = true)
    @Mapping(target = "userStories", ignore = true)
    @Mapping(target = "sprintNumber", ignore = true)
    void updateSprintFromDto(SprintUpdateRequest request, @MappingTarget SprintBacklog sprint);
}