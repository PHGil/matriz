package com.matriz.domain.task;

import java.time.LocalDate;
import java.util.Optional;

/**
 * Partial change of a task (PATCH): a {@code null} Optional leaves the field unchanged; an empty Optional clears
 * an optional field (description, due date) and is invalid for required ones (title, status, priority).
 */
public record TaskChanges(Optional<String> title, Optional<String> description, Optional<TaskStatus> status,
        Optional<TaskPriority> priority, Optional<LocalDate> dueDate) {

    public static TaskChanges status(TaskStatus status) {
        return new TaskChanges(null, null, Optional.of(status), null, null);
    }

    boolean isEmpty() {
        return title == null && description == null && status == null && priority == null && dueDate == null;
    }
}
