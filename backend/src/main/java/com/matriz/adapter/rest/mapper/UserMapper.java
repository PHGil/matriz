package com.matriz.adapter.rest.mapper;

import java.util.List;

import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

import com.matriz.adapter.rest.to.user.CurrentUserTO;
import com.matriz.adapter.rest.to.user.UserDirectoryEntryTO;
import com.matriz.adapter.rest.to.user.UserSummaryTO;
import com.matriz.domain.user.User;

/** Converts {@link User} domain objects to transfer objects. */
@Mapper(componentModel = MappingConstants.ComponentModel.JAKARTA_CDI)
public interface UserMapper {

    UserSummaryTO toSummaryTO(User user);

    UserDirectoryEntryTO toDirectoryEntryTO(User user);

    List<UserDirectoryEntryTO> toDirectoryEntryTOs(List<User> users);

    CurrentUserTO toCurrentUserTO(User user);
}
