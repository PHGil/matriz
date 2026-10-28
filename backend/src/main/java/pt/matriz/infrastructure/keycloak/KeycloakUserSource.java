package pt.matriz.infrastructure.keycloak;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.keycloak.admin.client.Keycloak;
import org.keycloak.representations.idm.UserRepresentation;

import io.quarkus.arc.profile.UnlessBuildProfile;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import pt.matriz.application.user.ExternalUser;
import pt.matriz.application.user.ExternalUserSource;

/** Reads the realm's users through the Keycloak Admin API (service account of matriz-backend). */
@ApplicationScoped
@UnlessBuildProfile("test")
public class KeycloakUserSource implements ExternalUserSource {

    private static final String REALM = "matriz";
    private static final int PAGE_SIZE = 100;

    @Inject
    Keycloak keycloak;

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
