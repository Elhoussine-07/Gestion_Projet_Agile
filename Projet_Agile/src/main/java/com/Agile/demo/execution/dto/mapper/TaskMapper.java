package com.Agile.demo.execution.dto.mapper;

import com.Agile.demo.execution.dto.task.CreateTaskRequest;
import com.Agile.demo.execution.dto.task.TaskResponseDTO;
import com.Agile.demo.execution.dto.task.UpdateTaskRequest;
import com.Agile.demo.model.Task;
import org.mapstruct.*;

import java.util.List;

@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface TaskMapper {

    @Mapping(source = "assignedUser.id", target = "assignedUserId")
    @Mapping(source = "assignedUser.username", target = "assignedUsername")
    @Mapping(source = "userStory.id", target = "userStoryId")
    TaskResponseDTO toResponseDTO(Task task);

    List<TaskResponseDTO> toResponseDTOList(List<Task> tasks);

    // Création : on ignore les relations complexes ici, on les gère dans le service
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "status", constant = "TODO")
    @Mapping(target = "actualHours", constant = "0")
    @Mapping(target = "userStory", ignore = true)
    @Mapping(target = "assignedUser", ignore = true)
    Task toEntity(CreateTaskRequest request);

    // Mise à jour partielle
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "status", ignore = true) // Le statut change via workflow
    @Mapping(target = "userStory", ignore = true)
    void updateEntityFromDTO(UpdateTaskRequest request, @MappingTarget Task task);
}