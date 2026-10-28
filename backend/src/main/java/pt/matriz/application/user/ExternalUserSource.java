package pt.matriz.application.user;

import java.util.List;

/** Port: reads all accounts from the identity provider (Keycloak, ADR-09). */
public interface ExternalUserSource {

    List<ExternalUser> fetchAll();
}
