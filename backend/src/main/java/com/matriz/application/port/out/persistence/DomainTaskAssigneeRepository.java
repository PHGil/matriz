package com.matriz.application.port.out.persistence;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/** Output port: storage of task assignments (FR-24). */
public interface DomainTaskAssigneeRepository {

    /** Assigned user ids per task, in assignment order; tasks without assignees are absent. */
    Map<UUID, List<UUID>> userIdsByTask(Collection<UUID> taskIds);

    boolean exists(UUID taskId, UUID userId);

    void add(UUID taskId, UUID userId);

    /** Returns false when the user was not assigned. */
    boolean remove(UUID taskId, UUID userId);

    /** Removes the user as assignee from every task of the project (EC-02, BR-06). */
    void removeUserFromProjectTasks(UUID projectId, UUID userId);
}
