package com.matriz.adapter.scheduler;

import org.jboss.logging.Logger;

import com.matriz.application.port.in.UserUseCases;
import com.matriz.application.port.out.identity.DomainExternalUserSource;

import io.quarkus.arc.profile.UnlessBuildProfile;
import io.quarkus.scheduler.Scheduled;
import jakarta.enterprise.context.ApplicationScoped;

import lombok.RequiredArgsConstructor;

/** Mirrors Keycloak users into the local projection at startup and every 5 minutes (ADR-09, IMP-13). */
@ApplicationScoped
@UnlessBuildProfile("test")
@RequiredArgsConstructor
public class UserSyncScheduler {

    private static final Logger LOG = Logger.getLogger(UserSyncScheduler.class);

    private final DomainExternalUserSource externalUserSource;
    private final UserUseCases userUseCases;

    @Scheduled(every = "5m", concurrentExecution = Scheduled.ConcurrentExecution.SKIP)
    void run() {
        try {
            var users = externalUserSource.fetchAll();
            userUseCases.synchronise(users);
            LOG.debugf("User sync finished: %d Keycloak users", users.size());
        } catch (RuntimeException e) {
            LOG.warnf("User sync with Keycloak failed: %s", e.getMessage());
        }
    }
}
