package pt.matriz.domain.task;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/** Port: storage of task assignments (FR-24). */
public interface TaskAssigneeRepository {

    /** Assigned user ids per task, in assignment order; tasks without assignees are absent. */
    Map<UUID, List<UUID>> userIdsByTask(Collection<UUID> taskIds);

    /** Removes the user as assignee from every task of the project (EC-02, BR-06). */
    void removeUserFromProjectTasks(UUID projectId, UUID userId);
}
