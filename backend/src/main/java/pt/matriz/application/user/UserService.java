package pt.matriz.application.user;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.logging.Logger;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import pt.matriz.domain.user.User;
import pt.matriz.domain.user.UserRepository;

@ApplicationScoped
public class UserService {

    private static final Logger LOG = Logger.getLogger(UserService.class.getName());
    private static final int DIRECTORY_LIMIT = 20;

    @Inject
    UserRepository users;

    /** Creates or refreshes the local user from the token claims; never changes the active flag. */
    @Transactional
    public User registerLogin(UUID id, String name, String email) {
        Optional<User> existing = users.findById(id);
        if (existing.isEmpty()) {
            User user = new User(id, name, email, true);
            users.save(user);
            return user;
        }
        User user = existing.get();
        if (user.refresh(name, email, user.isActive())) {
            users.save(user);
        }
        return user;
    }

    /** Mirrors the full identity-provider list; local users missing there become inactive (BR-10). */
    @Transactional
    public void synchronise(Collection<ExternalUser> externalUsers) {
        Set<UUID> seen = new HashSet<>();
        for (ExternalUser ext : externalUsers) {
            seen.add(ext.id());
            Optional<User> existing = users.findById(ext.id());
            if (existing.isEmpty()) {
                if (users.emailUsedByAnother(ext.email(), ext.id())) {
                    LOG.warning(() -> "Skipping external user " + ext.id() + ": email already used by another user");
                    continue;
                }
                users.save(new User(ext.id(), ext.name(), ext.email(), ext.active()));
            } else if (existing.get().refresh(ext.name(), ext.email(), ext.active())) {
                users.save(existing.get());
            }
        }
        if (!seen.isEmpty()) {
            users.deactivateAllExcept(seen);
        }
    }

    /** Directory of active users to add as members (US-04). */
    public List<User> searchActive(String text) {
        return users.searchActive(text == null ? "" : text.strip(), DIRECTORY_LIMIT);
    }
}
