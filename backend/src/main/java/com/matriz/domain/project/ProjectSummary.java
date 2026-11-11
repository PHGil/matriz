package com.matriz.domain.project;

import java.util.UUID;

/** Read model of a project in the list and dashboard (FR-13, FR-16, FR-34). */
public record ProjectSummary(UUID id, String name, String description, ProjectRole myRole, StatusCounts counts) {
}
