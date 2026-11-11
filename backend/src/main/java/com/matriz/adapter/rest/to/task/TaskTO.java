package com.matriz.adapter.rest.to.task;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

import com.matriz.adapter.rest.to.user.UserSummaryTO;
import com.matriz.domain.task.TaskPriority;
import com.matriz.domain.task.TaskStatus;

public record TaskTO(UUID id, UUID projectId, String title, String description, TaskStatus status,
        TaskPriority priority, LocalDate dueDate, List<UserSummaryTO> assignees, OffsetDateTime createdAt,
        OffsetDateTime updatedAt) {
}
