package com.matriz.adapter.persistence.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;

import com.matriz.adapter.persistence.entity.TaskCommentEntity;
import com.matriz.domain.task.Comment;

/** Converts between {@link TaskCommentEntity} and the {@link Comment} domain object. */
@Mapper(componentModel = MappingConstants.ComponentModel.JAKARTA_CDI)
public interface CommentEntityMapper {

    Comment toModel(TaskCommentEntity entity);

    @Mapping(target = "createdAt", ignore = true)
    TaskCommentEntity toEntity(Comment comment);
}
