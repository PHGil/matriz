package pt.matriz.users;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.jboss.logging.Logger;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.representations.idm.UserRepresentation;

import io.quarkus.arc.profile.UnlessBuildProfile;
import io.quarkus.scheduler.Scheduled;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

/** Periodically mirrors Keycloak users into app_user (ADR-09, IMP-13). Disabled in tests. */
@ApplicationScoped
@UnlessBuildProfile("test")
public class KeycloakUserSync {

    private static final Logger LOG = Logger.getLogger(KeycloakUserSync.class);
    private static final String REALM = "matriz";
    private static final int PAGE_SIZE = 100;

    @Inject
    Keycloak keycloak;

    @Inject
    UserService userService;

    @Scheduled(every = "5m", concurrentExecution = Scheduled.ConcurrentExecution.SKIP)
    void run() {
        try {
            List<UserService.ExternalUser> users = fetchAll();
            userService.synchronise(users);
            LOG.debugf("User sync finished: %d Keycloak users", users.size());
        } catch (RuntimeException e) {
            LOG.warnf("User sync with Keycloak failed: %s", e.getMessage());
        }
    }

    private List<UserService.ExternalUser> fetchAll() {
        List<UserService.ExternalUser> result = new ArrayList<>();
        int first = 0;
        List<UserRepresentation> page;
        do {
            page = keycloak.realm(REALM).users().list(first, PAGE_SIZE);
            for (UserRepresentation u : page) {
                if (u.getEmail() == null || u.getUsername().startsWith("service-account-")) {
                    continue;
                }
                result.add(new UserService.ExternalUser(UUID.fromString(u.getId()), displayName(u), u.getEmail(),
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
