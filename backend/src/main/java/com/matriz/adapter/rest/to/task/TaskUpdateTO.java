package com.matriz.adapter.rest.to.task;

import java.time.LocalDate;
import java.util.Optional;

import com.matriz.domain.task.TaskPriority;
import com.matriz.domain.task.TaskStatus;

/**
 * Absent field = unchanged ({@code null}); {@code null} clears description/dueDate and is rejected for the
 * others. A class, not a record: Jackson would turn absent record components into {@code Optional.empty()}.
 */
public class TaskUpdateTO {

    public Optional<String> title;
    public Optional<String> description;
    public Optional<TaskStatus> status;
    public Optional<TaskPriority> priority;
    public Optional<LocalDate> dueDate;
}
