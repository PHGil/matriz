package pt.matriz.infrastructure.persistence;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import pt.matriz.domain.task.Progress;
import pt.matriz.domain.task.Task;
import pt.matriz.domain.task.TaskPriority;
import pt.matriz.domain.task.TaskRepository;
import pt.matriz.domain.task.TaskStatus;

@ApplicationScoped
public class JpaTaskRepository implements TaskRepository {

    @Inject
    EntityManager em;

    @Override
    public Optional<Task> findById(UUID id) {
        return Optional.ofNullable(em.find(TaskEntity.class, id)).map(TaskEntity::toDomain);
    }

    @Override
    public List<Task> findByProject(UUID projectId, TaskStatus status, TaskPriority priority) {
        return em.createQuery("""
                from TaskEntity
                 where projectId = :projectId
                   and (:status is null or status = :status)
                   and (:priority is null or priority = :priority)
                 order by updatedAt desc, id
                """, TaskEntity.class)
                .setParameter("projectId", projectId)
                .setParameter("status", status)
                .setParameter("priority", priority)
                .getResultStream().map(TaskEntity::toDomain).toList();
    }

    @Override
    public Progress progress(UUID projectId) {
        Object[] row = (Object[]) em.createNativeQuery("""
                select count(*) filter (where status = 'TODO'),
                       count(*) filter (where status = 'IN_PROGRESS'),
                       count(*) filter (where status = 'DONE')
                  from task where project_id = :projectId
                """)
                .setParameter("projectId", projectId)
                .getSingleResult();
        return new Progress(((Number) row[0]).longValue(), ((Number) row[1]).longValue(),
                ((Number) row[2]).longValue());
    }

    /** Assignments and comments go with it through ON DELETE CASCADE (V1__schema.sql). */
    @Override
    public void delete(UUID id) {
        em.createNativeQuery("delete from task where id = :id").setParameter("id", id).executeUpdate();
    }

    @Override
    public Task save(Task task) {
        TaskEntity entity = em.find(TaskEntity.class, task.id());
        if (entity == null) {
            entity = new TaskEntity();
            entity.copyFrom(task);
            em.persist(entity);
        } else {
            entity.copyFrom(task);
        }
        em.flush();
        return entity.toDomain();
    }
}
