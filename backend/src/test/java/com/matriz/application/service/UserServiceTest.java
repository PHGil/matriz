package com.matriz.application.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import com.matriz.application.port.out.persistence.DomainUserRepository;
import com.matriz.domain.exception.ConflictException;
import com.matriz.domain.user.ExternalUser;
import com.matriz.domain.user.User;

import io.quarkus.narayana.jta.QuarkusTransaction;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;

@QuarkusTest
class UserServiceTest {

    @Inject
    UserService userService;

    @Inject
    DomainUserRepository users;

    @Inject
    EntityManager em;

    /** synchronise() deactivates every user missing from the list, including other test classes' users. */
    @AfterEach
    void reactivateAll() {
        QuarkusTransaction.requiringNew().run(() -> em.createNativeQuery("update app_user set active = true").executeUpdate());
    }

    @Test
    void synchroniseMirrorsKeycloakAndDeactivatesMissingUsers() {
        UUID kept = UUID.fromString("0f8b2c7e-2222-4000-8000-000000000001");
        UUID removed = UUID.fromString("0f8b2c7e-2222-4000-8000-000000000002");
        userService.registerLogin(removed, "Old User", "old.user@matriz.test");

        userService.synchronise(List.of(new ExternalUser(kept, "Kept User", "kept.user@matriz.test", false)));

        User keptUser = load(kept);
        assertEquals("Kept User", keptUser.getName());
        assertFalse(keptUser.isActive(), "disabled in Keycloak");
        assertFalse(load(removed).isActive(), "missing in Keycloak");

        userService.synchronise(List.of(new ExternalUser(kept, "Kept User", "kept.user@matriz.test", true)));
        assertTrue(load(kept).isActive());
    }

    @Test
    void simultaneousFirstLoginRequestsCreateOneProjection() throws Exception {
        UUID id = UUID.randomUUID();
        String email = id + "@matriz.test";
        int requests = 6;
        var ready = new CountDownLatch(requests);
        var start = new CountDownLatch(1);
        var executor = Executors.newFixedThreadPool(requests);
        try {
            List<Future<User>> results = IntStream.range(0, requests)
                    .mapToObj(index -> executor.submit(() -> {
                        ready.countDown();
                        assertTrue(start.await(10, TimeUnit.SECONDS));
                        return userService.registerLogin(id, "New User", email);
                    }))
                    .toList();
            assertTrue(ready.await(10, TimeUnit.SECONDS));
            start.countDown();
            for (Future<User> result : results) {
                User user = result.get(10, TimeUnit.SECONDS);
                assertEquals(id, user.getId());
                assertEquals(email, user.getEmail());
                assertTrue(user.isActive());
            }
            long count = QuarkusTransaction.requiringNew().call(() -> em.createQuery(
                    "select count(u) from UserEntity u where u.id = :id", Long.class)
                    .setParameter("id", id).getSingleResult());
            assertEquals(1L, count);
        } finally {
            start.countDown();
            executor.shutdownNow();
        }
    }

    @Test
    void insertingAnExistingProjectionDoesNotOverwriteItsDisabledState() {
        UUID id = UUID.randomUUID();
        String email = id + "@matriz.test";
        userService.synchronise(List.of(new ExternalUser(id, "Disabled User", email, false)));

        User user = QuarkusTransaction.requiringNew().call(
                () -> users.insertIfAbsent(new User(id, "Stale Name", email, true)));

        assertEquals("Disabled User", user.getName());
        assertFalse(user.isActive());
        assertFalse(load(id).isActive());
    }

    @Test
    void loginRefreshesProfileWithoutReactivatingDisabledUser() {
        UUID id = UUID.randomUUID();
        String email = id + "@matriz.test";
        userService.synchronise(List.of(new ExternalUser(id, "Old Name", email, false)));

        User user = userService.registerLogin(id, "New Name", email);

        assertEquals("New Name", user.getName());
        assertFalse(user.isActive());
        assertEquals("New Name", load(id).getName());
        assertFalse(load(id).isActive());
    }

    @Test
    void firstLoginWithAnotherUsersEmailIsAnExplicitConflict() {
        UUID id = UUID.randomUUID();
        String email = id + "@matriz.test";
        userService.registerLogin(id, "Existing User", email);
        UUID other = UUID.randomUUID();

        assertThrows(ConflictException.class,
                () -> userService.registerLogin(other, "Another User", email));

        assertTrue(QuarkusTransaction.requiringNew().call(() -> users.findById(other).isEmpty()));
        assertEquals("Existing User", load(id).getName());
    }

    private User load(UUID id) {
        return QuarkusTransaction.requiringNew().call(() -> users.findById(id).orElseThrow());
    }
}
