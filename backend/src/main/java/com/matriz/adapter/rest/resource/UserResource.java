package com.matriz.adapter.rest.resource;

import java.util.List;

import com.matriz.adapter.rest.mapper.UserMapper;
import com.matriz.adapter.rest.to.user.UserDirectoryEntryTO;
import com.matriz.application.port.in.UserUseCases;

import io.quarkus.security.Authenticated;
import jakarta.enterprise.context.RequestScoped;

import lombok.RequiredArgsConstructor;

@RequestScoped
@Authenticated
@RequiredArgsConstructor
public class UserResource implements UserRouter {

    private final UserUseCases userUseCases;
    private final UserMapper userMapper;

    @Override
    public List<UserDirectoryEntryTO> search(String q) {
        return userMapper.toDirectoryEntryTOs(userUseCases.searchActive(q));
    }
}
