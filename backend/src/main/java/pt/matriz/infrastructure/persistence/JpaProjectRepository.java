package pt.matriz.infrastructure.persistence;

import java.util.Optional;
import java.util.UUID;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import pt.matriz.domain.project.Project;
import pt.matriz.domain.project.ProjectRepository;

@ApplicationScoped
public class JpaProjectRepository implements ProjectRepository {

    @Inject
    EntityManager em;

    @Override
    public Optional<Project> findById(UUID id) {
        return Optional.ofNullable(em.find(ProjectEntity.class, id)).map(ProjectEntity::toDomain);
    }

    @Override
    public Project save(Project project) {
        ProjectEntity entity = em.find(ProjectEntity.class, project.id());
        if (entity == null) {
            entity = new ProjectEntity();
            entity.copyFrom(project);
            em.persist(entity);
        } else {
            entity.copyFrom(project);
        }
        em.flush();
        return entity.toDomain();
    }

    /** Members, tasks, assignments and comments go with it through ON DELETE CASCADE (V1__schema.sql). */
    @Override
    public void delete(UUID id) {
        em.createNativeQuery("delete from project where id = :id").setParameter("id", id).executeUpdate();
    }
}
