package pt.matriz.infrastructure.api;

import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.UUID;

import pt.matriz.application.project.MemberService.MemberDetails;
import pt.matriz.application.project.ProjectQueries;
import pt.matriz.application.project.ProjectService.ProjectWithRole;
import pt.matriz.domain.project.ProjectRole;
import pt.matriz.domain.task.Progress;

/** Request and response bodies of the projects API (openapi.yaml). */
public final class ProjectDtos {

    private ProjectDtos() {
    }

    public record ProjectCreate(String name, String description) {
    }

    /**
     * Absent field = unchanged ({@code null}); {@code "description": null} clears it ({@code Optional.empty()}).
     * A class, not a record: Jackson would turn absent record components into {@code Optional.empty()}.
     */
    public static class ProjectUpdate {
        public Optional<String> name;
        public Optional<String> description;
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

    public record ProjectProgress(UUID projectId, long total, long todo, long inProgress, long done,
            Integer percentDone) {

        static ProjectProgress of(UUID projectId, Progress p) {
            return new ProjectProgress(projectId, p.total(), p.todo(), p.inProgress(), p.done(), p.percentDone());
        }
    }
}
