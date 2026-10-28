package pt.matriz.domain.user;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

/** Port: storage of users. */
public interface UserRepository {

    Optional<User> findById(UUID id);

    List<User> findAllById(Collection<UUID> ids);

    boolean emailUsedByAnother(String email, UUID exceptId);

    /** Inserts or updates. */
    void save(User user);

    /** Marks inactive every active user whose id is not in {@code keep} (BR-10). */
    void deactivateAllExcept(Set<UUID> keep);

    /** Active users whose name or email contains {@code text} (case-insensitive), sorted by name. */
    List<User> searchActive(String text, int limit);
}
