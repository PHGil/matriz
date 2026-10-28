package pt.matriz.infrastructure.persistence;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import pt.matriz.domain.project.MemberRepository;
import pt.matriz.domain.project.ProjectMember;

@ApplicationScoped
public class JpaMemberRepository implements MemberRepository {

    @Inject
    EntityManager em;

    @Override
    public Optional<ProjectMember> find(UUID projectId, UUID userId) {
        return Optional.ofNullable(em.find(ProjectMemberEntity.class, new ProjectMemberEntity.Key(projectId, userId)))
                .map(ProjectMemberEntity::toDomain);
    }

    @Override
    public List<ProjectMember> findByProject(UUID projectId) {
        return em.createQuery("from ProjectMemberEntity where id.projectId = :projectId", ProjectMemberEntity.class)
                .setParameter("projectId", projectId)
                .getResultStream().map(ProjectMemberEntity::toDomain).toList();
    }

    @Override
    public ProjectMember add(ProjectMember member) {
        ProjectMemberEntity entity = ProjectMemberEntity.from(member);
        em.persist(entity);
        em.flush();
        return entity.toDomain();
    }

    @Override
    public void remove(UUID projectId, UUID userId) {
        em.createQuery("delete from ProjectMemberEntity where id.projectId = :projectId and id.userId = :userId")
                .setParameter("projectId", projectId)
                .setParameter("userId", userId)
                .executeUpdate();
    }
}
