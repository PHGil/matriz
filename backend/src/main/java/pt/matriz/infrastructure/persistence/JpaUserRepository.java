package pt.matriz.infrastructure.persistence;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import pt.matriz.domain.user.User;
import pt.matriz.domain.user.UserRepository;

@ApplicationScoped
public class JpaUserRepository implements UserRepository {

    @Inject
    EntityManager em;

    @Override
    public Optional<User> findById(UUID id) {
        return Optional.ofNullable(em.find(UserEntity.class, id)).map(UserEntity::toDomain);
    }

    @Override
    public List<User> findAllById(Collection<UUID> ids) {
        if (ids.isEmpty()) {
            return List.of();
        }
        return em.createQuery("from UserEntity where id in :ids", UserEntity.class)
                .setParameter("ids", ids)
                .getResultStream().map(UserEntity::toDomain).toList();
    }

    @Override
    public boolean emailUsedByAnother(String email, UUID exceptId) {
        return em.createQuery("select count(u) from UserEntity u where lower(u.email) = lower(:email) and u.id <> :id",
                Long.class)
                .setParameter("email", email)
                .setParameter("id", exceptId)
                .getSingleResult() > 0;
    }

    @Override
    public void save(User user) {
        UserEntity entity = em.find(UserEntity.class, user.id());
        if (entity == null) {
            entity = new UserEntity();
            entity.copyFrom(user);
            em.persist(entity);
        } else {
            entity.copyFrom(user);
        }
    }

    @Override
    public void deactivateAllExcept(Set<UUID> keep) {
        em.createQuery("update UserEntity set active = false where active = true and id not in :keep")
                .setParameter("keep", keep)
                .executeUpdate();
    }

    @Override
    public List<User> searchActive(String text, int limit) {
        var query = text.isBlank()
                ? em.createQuery("from UserEntity where active = true order by lower(name)", UserEntity.class)
                : em.createQuery("""
                        from UserEntity where active = true
                         and (lower(name) like :pattern escape '\\' or lower(email) like :pattern escape '\\')
                         order by lower(name)
                        """, UserEntity.class).setParameter("pattern", LikePattern.contains(text));
        return query.setMaxResults(limit).getResultStream().map(UserEntity::toDomain).toList();
    }
}
