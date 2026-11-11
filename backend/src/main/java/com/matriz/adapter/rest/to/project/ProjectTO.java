package com.matriz.adapter.rest.to.project;

import java.time.OffsetDateTime;
import java.util.UUID;

import com.matriz.domain.project.ProjectRole;

public record ProjectTO(UUID id, String name, String description, ProjectRole myRole, OffsetDateTime createdAt,
        OffsetDateTime updatedAt) {
}
