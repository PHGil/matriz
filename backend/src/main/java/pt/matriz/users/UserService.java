package pt.matriz.users;

import java.util.Collection;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

import org.jboss.logging.Logger;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;

@ApplicationScoped
public class UserService {

    private static final Logger LOG = Logger.getLogger(UserService.class);

    /** Snapshot of a Keycloak account, independent of the Keycloak client classes. */
    public record ExternalUser(UUID id, String name, String email, boolean active) {
    }

    /** Creates or refreshes the local user from token claims; never changes the active flag. */
    @Transactional
    public AppUser upsertFromToken(UUID id, String name, String email) {
        AppUser user = AppUser.findById(id);
        if (user == null) {
            user = new AppUser();
            user.id = id;
            user.name = name;
            user.email = email;
            user.persist();
        } else if (!Objects.equals(user.name, name) || !Objects.equals(user.email, email)) {
            user.name = name;
            user.email = email;
        }
        return user;
    }

    /** Mirrors the full Keycloak user list; local users missing in Keycloak become inactive (BR-10). */
    @Transactional
    public void synchronise(Collection<ExternalUser> externalUsers) {
        Set<UUID> seen = new HashSet<>();
        for (ExternalUser ext : externalUsers) {
            seen.add(ext.id());
            AppUser user = AppUser.findById(ext.id());
            if (user == null) {
                if (AppUser.count("lower(email) = lower(?1)", ext.email()) > 0) {
                    LOG.warnf("Skipping Keycloak user %s: email already used by another local user", ext.id());
                    continue;
                }
                user = new AppUser();
                user.id = ext.id();
                user.name = ext.name();
                user.email = ext.email();
                user.active = ext.active();
                user.persist();
            } else if (!Objects.equals(user.name, ext.name()) || !Objects.equals(user.email, ext.email())
                    || user.active != ext.active()) {
                user.name = ext.name();
                user.email = ext.email();
                user.active = ext.active();
            }
        }
        if (!seen.isEmpty()) {
            AppUser.update("active = false where active = true and id not in ?1", seen);
        }
    }
}
