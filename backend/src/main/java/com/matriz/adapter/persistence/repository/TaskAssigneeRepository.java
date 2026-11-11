package com.matriz.adapter.persistence.repository;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import com.matriz.application.port.out.persistence.DomainTaskAssigneeRepository;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.EntityManager;

import lombok.RequiredArgsConstructor;

@ApplicationScoped
@RequiredArgsConstructor
public class TaskAssigneeRepository implements DomainTaskAssigneeRepository {

    private final EntityManager em;

    @Override
    @SuppressWarnings("unchecked")
    public Map<UUID, List<UUID>> userIdsByTask(Collection<UUID> taskIds) {
        if (taskIds.isEmpty()) {
            return Map.of();
        }
        List<Object[]> rows = em.createNativeQuery("""
                select task_id, user_id from task_assignee
                 where task_id in (:taskIds)
                 order by assigned_at, user_id
                """)
                .setParameter("taskIds", taskIds)
                .getResultList();
        return rows.stream().collect(Collectors.groupingBy(r -> (UUID) r[0], LinkedHashMap::new,
                Collectors.mapping(r -> (UUID) r[1], Collectors.toList())));
    }

    @Override
    public boolean exists(UUID taskId, UUID userId) {
        return !em.createNativeQuery("select 1 from task_assignee where task_id = :taskId and user_id = :userId")
                .setParameter("taskId", taskId)
                .setParameter("userId", userId)
                .getResultList().isEmpty();
    }

    @Override
    public void add(UUID taskId, UUID userId) {
        em.createNativeQuery("insert into task_assignee (task_id, user_id) values (:taskId, :userId)")
                .setParameter("taskId", taskId)
                .setParameter("userId", userId)
                .executeUpdate();
    }

    @Override
    public boolean remove(UUID taskId, UUID userId) {
        return em.createNativeQuery("delete from task_assignee where task_id = :taskId and user_id = :userId")
                .setParameter("taskId", taskId)
                .setParameter("userId", userId)
                .executeUpdate() > 0;
    }

    @Override
    public void removeUserFromProjectTasks(UUID projectId, UUID userId) {
        em.createNativeQuery("""
                delete from task_assignee a
                 using task t
                 where a.task_id = t.id and t.project_id = :projectId and a.user_id = :userId
                """)
                .setParameter("projectId", projectId)
                .setParameter("userId", userId)
                .executeUpdate();
    }
}
