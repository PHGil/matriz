package pt.matriz.infrastructure.api;

import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.UUID;

import pt.matriz.application.project.MemberService.MemberDetails;
import pt.matriz.application.project.ProjectQueries;
import pt.matriz.application.project.ProjectService.ProjectWithRole;
import pt.matriz.domain.project.ProjectRole;

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

        static ProjectSummary of(ProjectQueries.ProjectSummary s) {
            var c = s.counts();
            return new ProjectSummary(s.id(), s.name(), s.description(), s.myRole(),
                    new StatusCounts(c.todo(), c.inProgress(), c.done(), c.total()));
        }
    }

    public record ProjectView(UUID id, String name, String description, ProjectRole myRole,
            OffsetDateTime createdAt, OffsetDateTime updatedAt) {

        static ProjectView of(ProjectWithRole p) {
            var project = p.project();
            return new ProjectView(project.id(), project.name(), project.description(), p.myRole(),
                    project.createdAt(), project.updatedAt());
        }
    }

    public record UserSummary(UUID id, String name, boolean active) {
    }

    public record MemberView(UserSummary user, ProjectRole role, OffsetDateTime addedAt) {

        static MemberView of(MemberDetails d) {
            return new MemberView(new UserSummary(d.user().id(), d.user().name(), d.user().isActive()),
                    d.member().role(), d.member().addedAt());
        }
    }

    public record UserRef(UUID userId) {
    }
}
