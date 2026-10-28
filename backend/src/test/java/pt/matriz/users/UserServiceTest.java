package pt.matriz.users;

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

@QuarkusTest
class UserServiceTest {

    @Inject
    UserService userService;

    /** synchronise() deactivates every user missing from the list, including other test classes' users. */
    @AfterEach
    void reactivateAll() {
        QuarkusTransaction.requiringNew().run(() -> AppUser.update("active = true"));
    }

    @Test
    void synchroniseMirrorsKeycloakAndDeactivatesMissingUsers() {
        UUID kept = UUID.fromString("0f8b2c7e-2222-4000-8000-000000000001");
        UUID removed = UUID.fromString("0f8b2c7e-2222-4000-8000-000000000002");
        userService.upsertFromToken(removed, "Old User", "old.user@matriz.test");

        userService.synchronise(List.of(
                new UserService.ExternalUser(kept, "Kept User", "kept.user@matriz.test", false)));

        QuarkusTransaction.requiringNew().run(() -> {
            AppUser keptUser = AppUser.findById(kept);
            assertEquals("Kept User", keptUser.name);
            assertFalse(keptUser.active, "disabled in Keycloak");
            AppUser removedUser = AppUser.findById(removed);
            assertFalse(removedUser.active, "missing in Keycloak");
        });

        userService.synchronise(List.of(
                new UserService.ExternalUser(kept, "Kept User", "kept.user@matriz.test", true)));
        QuarkusTransaction.requiringNew().run(() -> assertTrue(AppUser.<AppUser>findById(kept).active));
    }
}
