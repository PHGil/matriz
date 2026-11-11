package com.matriz.application.port.in;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

import com.matriz.domain.user.ExternalUser;
import com.matriz.domain.user.User;

/** Input port: user use cases. */
public interface UserUseCases {

    /** Creates or refreshes the local user from the token claims; never changes the active flag. */
    User registerLogin(UUID id, String name, String email);

    /** Mirrors the full identity-provider list; local users missing there become inactive (BR-10). */
    void synchronise(Collection<ExternalUser> externalUsers);

    /** Directory of active users to add as members (US-04). */
    List<User> searchActive(String text);
}
