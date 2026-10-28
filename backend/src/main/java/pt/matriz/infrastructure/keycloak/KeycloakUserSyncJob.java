package pt.matriz.infrastructure.keycloak;

import org.jboss.logging.Logger;

import io.quarkus.arc.profile.UnlessBuildProfile;
import io.quarkus.scheduler.Scheduled;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import pt.matriz.application.user.ExternalUserSource;
import pt.matriz.application.user.UserService;

/** Mirrors Keycloak users into the local projection at startup and every 5 minutes (ADR-09, IMP-13). */
@ApplicationScoped
@UnlessBuildProfile("test")
public class KeycloakUserSyncJob {

    private static final Logger LOG = Logger.getLogger(KeycloakUserSyncJob.class);

    @Inject
    ExternalUserSource source;

    @Inject
    UserService userService;

    @Scheduled(every = "5m", concurrentExecution = Scheduled.ConcurrentExecution.SKIP)
    void run() {
        try {
            var users = source.fetchAll();
            userService.synchronise(users);
            LOG.debugf("User sync finished: %d Keycloak users", users.size());
        } catch (RuntimeException e) {
            LOG.warnf("User sync with Keycloak failed: %s", e.getMessage());
        }
    }
}
