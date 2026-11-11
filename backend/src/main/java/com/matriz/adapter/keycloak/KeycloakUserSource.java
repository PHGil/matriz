package com.matriz.adapter.keycloak;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.keycloak.admin.client.Keycloak;
import org.keycloak.representations.idm.UserRepresentation;

import com.matriz.application.port.out.identity.DomainExternalUserSource;
import com.matriz.domain.user.ExternalUser;

import io.quarkus.arc.profile.UnlessBuildProfile;
import jakarta.enterprise.context.ApplicationScoped;

import lombok.RequiredArgsConstructor;

/** Reads the realm's users through the Keycloak Admin API (service account of matriz-backend). */
@ApplicationScoped
@UnlessBuildProfile("test")
@RequiredArgsConstructor
public class KeycloakUserSource implements DomainExternalUserSource {

    private static final String REALM = "matriz";
    private static final int PAGE_SIZE = 100;

    private final Keycloak keycloak;

    @Override
    public List<ExternalUser> fetchAll() {
        List<ExternalUser> result = new ArrayList<>();
        int first = 0;
        List<UserRepresentation> page;
        do {
            page = keycloak.realm(REALM).users().list(first, PAGE_SIZE);
            for (UserRepresentation u : page) {
                if (u.getEmail() == null || u.getUsername().startsWith("service-account-")) {
                    continue;
                }
                result.add(new ExternalUser(UUID.fromString(u.getId()), displayName(u), u.getEmail(),
                        Boolean.TRUE.equals(u.isEnabled())));
            }
            first += PAGE_SIZE;
        } while (page.size() == PAGE_SIZE);
        return result;
    }

    private static String displayName(UserRepresentation u) {
        String name = ((u.getFirstName() == null ? "" : u.getFirstName()) + " "
                + (u.getLastName() == null ? "" : u.getLastName())).trim();
        return name.isEmpty() ? u.getEmail() : name;
    }
}
