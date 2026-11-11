package com.matriz.adapter.persistence.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.MappingTarget;

import com.matriz.adapter.persistence.entity.ProjectEntity;
import com.matriz.domain.project.Project;

/** Converts between {@link ProjectEntity} and the {@link Project} domain object. */
@Mapper(componentModel = MappingConstants.ComponentModel.JAKARTA_CDI)
public interface ProjectEntityMapper {

    Project toModel(ProjectEntity entity);

    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    ProjectEntity toEntity(Project project);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    void update(Project project, @MappingTarget ProjectEntity entity);
}
