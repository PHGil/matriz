package com.matriz.adapter.rest.resource;

import com.matriz.adapter.rest.auth.CurrentUser;
import com.matriz.adapter.rest.mapper.UserMapper;
import com.matriz.adapter.rest.to.user.CurrentUserTO;

import io.quarkus.security.Authenticated;
import jakarta.enterprise.context.RequestScoped;

import lombok.RequiredArgsConstructor;

@RequestScoped
@Authenticated
@RequiredArgsConstructor
public class AuthResource implements AuthRouter {

    private final CurrentUser currentUser;
    private final UserMapper userMapper;

    @Override
    public CurrentUserTO me() {
        return userMapper.toCurrentUserTO(currentUser.get());
    }
}
