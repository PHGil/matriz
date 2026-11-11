package com.matriz.application.port.out.persistence;

import java.util.List;
import java.util.UUID;

import com.matriz.domain.project.ProjectSummary;

/** Output port: read model for the dashboard and project list (FR-13, FR-16, FR-34). */
public interface DomainProjectSummaryRepository {

    /** Projects the user belongs to, optionally filtered by name, with counts over all their tasks. */
    List<ProjectSummary> findByUser(UUID userId, String nameContains);
}
