package com.matriz.application.port.out.identity;

import java.util.List;

import com.matriz.domain.user.ExternalUser;

/** Output port: reads all accounts from the identity provider (Keycloak, ADR-09). */
public interface DomainExternalUserSource {

    List<ExternalUser> fetchAll();
}
