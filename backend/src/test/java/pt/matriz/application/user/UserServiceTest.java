package pt.matriz.application.user;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import io.quarkus.narayana.jta.QuarkusTransaction;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import pt.matriz.domain.user.User;
import pt.matriz.domain.user.UserRepository;

@QuarkusTest
class UserServiceTest {

    @Inject
    UserService userService;

    @Inject
    UserRepository users;

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
        assertEquals("Kept User", keptUser.name());
        assertFalse(keptUser.isActive(), "disabled in Keycloak");
        assertFalse(load(removed).isActive(), "missing in Keycloak");

        userService.synchronise(List.of(new ExternalUser(kept, "Kept User", "kept.user@matriz.test", true)));
        assertTrue(load(kept).isActive());
    }

    private User load(UUID id) {
        return QuarkusTransaction.requiringNew().call(() -> users.findById(id).orElseThrow());
    }
}
