package com.matriz.adapter.persistence.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.MappingTarget;

import com.matriz.adapter.persistence.entity.TaskEntity;
import com.matriz.domain.task.Task;

/** Converts between {@link TaskEntity} and the {@link Task} domain object. */
@Mapper(componentModel = MappingConstants.ComponentModel.JAKARTA_CDI)
public interface TaskEntityMapper {

    Task toModel(TaskEntity entity);

    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    TaskEntity toEntity(Task task);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "projectId", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    void update(Task task, @MappingTarget TaskEntity entity);
}
