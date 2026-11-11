package com.matriz.adapter.persistence.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.matriz.adapter.persistence.entity.TaskEntity;
import com.matriz.adapter.persistence.mapper.TaskEntityMapper;
import com.matriz.application.port.out.persistence.DomainTaskRepository;
import com.matriz.domain.task.Progress;
import com.matriz.domain.task.Task;
import com.matriz.domain.task.TaskPriority;
import com.matriz.domain.task.TaskStatus;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.EntityManager;

import lombok.RequiredArgsConstructor;

@ApplicationScoped
@RequiredArgsConstructor
public class TaskRepository implements DomainTaskRepository {

    private final EntityManager em;
    private final TaskEntityMapper mapper;

    @Override
    public Optional<Task> findById(UUID id) {
        return Optional.ofNullable(em.find(TaskEntity.class, id)).map(mapper::toModel);
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
                .getResultStream().map(mapper::toModel).toList();
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
        TaskEntity entity = em.find(TaskEntity.class, task.getId());
        if (entity == null) {
            entity = mapper.toEntity(task);
            em.persist(entity);
        } else {
            mapper.update(task, entity);
        }
        em.flush();
        return mapper.toModel(entity);
    }
}
