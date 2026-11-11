package com.matriz.adapter.rest.mapper;

import java.util.List;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;

import com.matriz.adapter.rest.to.task.CommentTO;
import com.matriz.domain.task.CommentDetails;

/** Converts comment domain objects to transfer objects. */
@Mapper(componentModel = MappingConstants.ComponentModel.JAKARTA_CDI, uses = UserMapper.class)
public interface CommentMapper {

    @Mapping(target = ".", source = "comment")
    CommentTO toTO(CommentDetails details);

    List<CommentTO> toTOs(List<CommentDetails> details);
}
