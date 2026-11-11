package com.matriz.adapter.persistence.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;

import com.matriz.adapter.persistence.entity.ProjectMemberEntity;
import com.matriz.domain.project.ProjectMember;

/** Converts between {@link ProjectMemberEntity} and the {@link ProjectMember} domain object. */
@Mapper(componentModel = MappingConstants.ComponentModel.JAKARTA_CDI)
public interface ProjectMemberEntityMapper {

    @Mapping(target = "projectId", source = "id.projectId")
    @Mapping(target = "userId", source = "id.userId")
    ProjectMember toModel(ProjectMemberEntity entity);

    @Mapping(target = "addedAt", ignore = true)
    @Mapping(target = "id", expression = "java(new ProjectMemberEntity.Key(member.getProjectId(), member.getUserId()))")
    ProjectMemberEntity toEntity(ProjectMember member);
}
