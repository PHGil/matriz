package com.matriz.application.port.out.persistence;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.matriz.domain.task.Progress;
import com.matriz.domain.task.Task;
import com.matriz.domain.task.TaskPriority;
import com.matriz.domain.task.TaskStatus;

/** Output port: storage of tasks. */
public interface DomainTaskRepository {

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
