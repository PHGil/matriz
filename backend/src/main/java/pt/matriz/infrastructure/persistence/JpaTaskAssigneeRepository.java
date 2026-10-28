package pt.matriz.infrastructure.persistence;

import java.util.UUID;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import pt.matriz.domain.task.TaskAssigneeRepository;

@ApplicationScoped
public class JpaTaskAssigneeRepository implements TaskAssigneeRepository {

    @Inject
    EntityManager em;

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
