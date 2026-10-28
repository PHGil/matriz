package pt.matriz.domain.task;

import java.util.UUID;

/** Port: storage of task assignments (FR-24). Extended in increment 4. */
public interface TaskAssigneeRepository {

    /** Removes the user as assignee from every task of the project (EC-02, BR-06). */
    void removeUserFromProjectTasks(UUID projectId, UUID userId);
}
