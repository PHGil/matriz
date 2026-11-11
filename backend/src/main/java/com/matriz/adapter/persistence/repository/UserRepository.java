package com.matriz.adapter.persistence.repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import com.matriz.adapter.persistence.entity.UserEntity;
import com.matriz.adapter.persistence.mapper.UserEntityMapper;
import com.matriz.application.port.out.persistence.DomainUserRepository;
import com.matriz.domain.user.User;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.EntityManager;

import lombok.RequiredArgsConstructor;

@ApplicationScoped
@RequiredArgsConstructor
public class UserRepository implements DomainUserRepository {

    private final EntityManager em;
    private final UserEntityMapper mapper;

    @Override
    public Optional<User> findById(UUID id) {
        return Optional.ofNullable(em.find(UserEntity.class, id)).map(mapper::toModel);
    }

    @Override
    public List<User> findAllById(Collection<UUID> ids) {
        if (ids.isEmpty()) {
            return List.of();
        }
        return em.createQuery("from UserEntity where id in :ids", UserEntity.class)
                .setParameter("ids", ids)
                .getResultStream().map(mapper::toModel).toList();
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
        UserEntity entity = em.find(UserEntity.class, user.getId());
        if (entity == null) {
            em.persist(mapper.toEntity(user));
        } else {
            mapper.update(user, entity);
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
        return query.setMaxResults(limit).getResultStream().map(mapper::toModel).toList();
    }
}
