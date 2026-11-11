package com.matriz.adapter.rest.mapper;

import java.util.List;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;

import com.matriz.adapter.rest.to.task.TaskTO;
import com.matriz.adapter.rest.to.task.TaskUpdateTO;
import com.matriz.domain.task.TaskChanges;
import com.matriz.domain.task.TaskDetails;

/** Converts task domain objects to and from transfer objects. */
@Mapper(componentModel = MappingConstants.ComponentModel.JAKARTA_CDI, uses = UserMapper.class)
public interface TaskMapper {

    @Mapping(target = ".", source = "task")
    TaskTO toTO(TaskDetails details);

    List<TaskTO> toTOs(List<TaskDetails> details);

    TaskChanges toChanges(TaskUpdateTO update);
}
