package com.matriz.domain.user;

import java.util.Objects;
import java.util.UUID;

import lombok.Getter;

/** A person who can use Matriz; mirrors the Keycloak account (ADR-09). The id is the Keycloak subject. */
@Getter
public class User {

    private final UUID id;
    private String name;
    private String email;
    private boolean active;

    public User(UUID id, String name, String email, boolean active) {
        this.id = Objects.requireNonNull(id);
        this.name = name;
        this.email = email;
        this.active = active;
    }

    /** Applies the account data from the identity provider; returns true when anything changed. */
    public boolean refresh(String name, String email, boolean active) {
        boolean changed = !Objects.equals(this.name, name) || !Objects.equals(this.email, email)
                || this.active != active;
        this.name = name;
        this.email = email;
        this.active = active;
        return changed;
    }
}
