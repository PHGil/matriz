package com.matriz.adapter.rest.to.project;

import java.util.UUID;

import com.matriz.domain.project.ProjectRole;

public record ProjectSummaryTO(UUID id, String name, String description, ProjectRole myRole,
        StatusCountsTO counts) {
}
