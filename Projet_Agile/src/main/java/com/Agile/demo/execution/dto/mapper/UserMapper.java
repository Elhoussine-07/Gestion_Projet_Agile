package com.Agile.demo.execution.dto.mapper;

import com.Agile.demo.execution.dto.user.CreateUserRequest;
import com.Agile.demo.execution.dto.user.UserResponseDTO;
import com.Agile.demo.execution.dto.user.UserUpdateRequest;
import com.Agile.demo.model.User;
import org.mapstruct.*;

import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface UserMapper {

    // ============= Entity to DTO (Réponses) =============

    @Mapping(target = "roles", source = "roles")
    UserResponseDTO toResponseDTO(User user);

    List<UserResponseDTO> toResponseDTOList(List<User> users);

    // ============= DTO to Entity (Création) =============

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "password", ignore = true) // Géré séparément avec encoding
    @Mapping(target = "isActive", constant = "true")
    @Mapping(target = "passwordResetRequired", constant = "false")
    User toEntity(CreateUserRequest request);

    // ============= Update methods =============

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "username", ignore = true)
    @Mapping(target = "password", ignore = true)
    @Mapping(target = "passwordResetRequired", ignore = true)
    void updateEntityFromDTO(UserUpdateRequest dto, @MappingTarget User entity);
}