package com.matriz.adapter.rest.to.task;

import java.time.LocalDate;

import com.matriz.domain.task.TaskPriority;

public record TaskCreateTO(String title, String description, TaskPriority priority, LocalDate dueDate) {
}
