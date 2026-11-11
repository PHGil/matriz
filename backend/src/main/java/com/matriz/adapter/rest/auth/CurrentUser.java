package com.matriz.adapter.rest.auth;

import java.util.UUID;

import org.eclipse.microprofile.jwt.JsonWebToken;

import com.matriz.application.port.in.UserUseCases;
import com.matriz.domain.user.User;

import io.quarkus.security.UnauthorizedException;
import jakarta.enterprise.context.RequestScoped;

import lombok.RequiredArgsConstructor;

/** The authenticated user of the current request, resolved from the validated access token. */
@RequestScoped
@RequiredArgsConstructor
public class CurrentUser {

    private final JsonWebToken jwt;
    private final UserUseCases userUseCases;

    private User user;

    public User get() {
        if (user == null) {
            String sub = jwt.getSubject();
            if (sub == null) {
                throw new UnauthorizedException();
            }
            String email = firstNonBlank(jwt.getClaim("email"), jwt.getClaim("preferred_username"));
            String name = firstNonBlank(jwt.getClaim("name"), email);
            user = userUseCases.registerLogin(UUID.fromString(sub), name, email);
        }
        return user;
    }

    public UUID id() {
        return get().getId();
    }

    private static String firstNonBlank(Object first, Object second) {
        if (first != null && !first.toString().isBlank()) {
            return first.toString();
        }
        return second == null ? null : second.toString();
    }
}
