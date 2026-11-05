package pt.matriz.domain.task;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/** Port: storage of tasks. */
public interface TaskRepository {

    Optional<Task> findById(UUID id);

    /** Tasks of a project, most recently updated first; {@code null} filters are ignored. */
    List<Task> findByProject(UUID projectId, TaskStatus status, TaskPriority priority);

    /** Inserts or updates; returns the stored state (with timestamps). */
    Task save(Task task);

    /** Deletes the task with its assignments and comments (BR-08). */
    void delete(UUID id);

    /** Counts over all tasks of the project, regardless of any filter (US-09 AC2). */
    Progress progress(UUID projectId);
}
