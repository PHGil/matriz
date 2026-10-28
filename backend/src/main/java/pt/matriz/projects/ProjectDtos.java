package pt.matriz.projects;

import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.UUID;

/** Request and response bodies of the projects API (openapi.yaml). */
public final class ProjectDtos {

    private ProjectDtos() {
    }

    public record ProjectCreate(String name, String description) {
    }

    /** Absent field = unchanged; {@code "description": null} clears it. */
    public record ProjectUpdate(Optional<String> name, Optional<String> description) {
    }

    public record StatusCounts(long todo, long inProgress, long done, long total) {
    }

    public record ProjectSummary(UUID id, String name, String description, ProjectRole myRole, StatusCounts counts) {
    }

    public record ProjectView(UUID id, String name, String description, ProjectRole myRole,
            OffsetDateTime createdAt, OffsetDateTime updatedAt) {

        static ProjectView of(Project p, ProjectRole role) {
            return new ProjectView(p.id, p.name, p.description, role, p.createdAt, p.updatedAt);
        }
    }

    public record UserSummary(UUID id, String name, boolean active) {
    }

    public record MemberView(UserSummary user, ProjectRole role, OffsetDateTime addedAt) {
    }

    public record UserRef(UUID userId) {
    }
}
