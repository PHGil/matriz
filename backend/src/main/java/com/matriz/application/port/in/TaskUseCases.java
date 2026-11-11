package com.matriz.application.port.in;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import com.matriz.domain.task.Progress;
import com.matriz.domain.task.TaskChanges;
import com.matriz.domain.task.TaskDetails;
import com.matriz.domain.task.TaskPriority;
import com.matriz.domain.task.TaskStatus;
import com.matriz.domain.user.User;

/** Input port: task use cases. */
public interface TaskUseCases {

    /** Any member can see the project's tasks; {@code null} filters are ignored (FR-32). */
    List<TaskDetails> list(UUID projectId, UUID userId, TaskStatus status, TaskPriority priority);

    TaskDetails create(UUID projectId, UUID userId, String title, String description, TaskPriority priority,
            LocalDate dueDate);

    TaskDetails get(UUID projectId, UUID taskId, UUID userId);

    Progress progress(UUID projectId, UUID userId);

    /** Only the Manager can delete tasks (FR-25). */
    void delete(UUID projectId, UUID taskId, UUID userId);

    /** Members and the Manager can edit any field and change the status (FR-21, FR-23). Last write wins (EC-01). */
    TaskDetails update(UUID projectId, UUID taskId, UUID userId, TaskChanges changes);

    /** Any member can assign an active project member (FR-24); duplicates are refused. */
    User assign(UUID projectId, UUID taskId, UUID userId, UUID assigneeId);

    void unassign(UUID projectId, UUID taskId, UUID userId, UUID assigneeId);
}
