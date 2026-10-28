package pt.matriz.application.project;

import java.util.List;
import java.util.UUID;

import pt.matriz.domain.project.ProjectRole;

/** Port: read model for the dashboard and project list (FR-13, FR-16, FR-34). */
public interface ProjectQueries {

    record StatusCounts(long todo, long inProgress, long done, long total) {
    }

    record ProjectSummary(UUID id, String name, String description, ProjectRole myRole, StatusCounts counts) {
    }

    /** Projects the user belongs to, optionally filtered by name, with counts over all their tasks. */
    List<ProjectSummary> summariesFor(UUID userId, String nameContains);
}
