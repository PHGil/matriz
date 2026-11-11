package com.matriz.adapter.persistence.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.MappingTarget;

import com.matriz.adapter.persistence.entity.UserEntity;
import com.matriz.domain.user.User;

/** Converts between {@link UserEntity} and the {@link User} domain object. */
@Mapper(componentModel = MappingConstants.ComponentModel.JAKARTA_CDI)
public interface UserEntityMapper {

    User toModel(UserEntity entity);

    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    UserEntity toEntity(User user);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    void update(User user, @MappingTarget UserEntity entity);
}
