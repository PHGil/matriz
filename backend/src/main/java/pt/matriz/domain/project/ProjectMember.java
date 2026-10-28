package pt.matriz.domain.project;

import java.time.OffsetDateTime;
import java.util.Objects;
import java.util.UUID;

import pt.matriz.domain.shared.ConflictException;

/** A user's participation in a project; exactly one Manager per project (A-05). */
public class ProjectMember {

    private final UUID projectId;
    private final UUID userId;
    private final ProjectRole role;
    private final OffsetDateTime addedAt;

    public ProjectMember(UUID projectId, UUID userId, ProjectRole role, OffsetDateTime addedAt) {
        this.projectId = Objects.requireNonNull(projectId);
        this.userId = Objects.requireNonNull(userId);
        this.role = Objects.requireNonNull(role);
        this.addedAt = addedAt;
    }

    /** The project creator becomes its Manager (FR-10, BR-03). */
    public static ProjectMember manager(UUID projectId, UUID userId) {
        return new ProjectMember(projectId, userId, ProjectRole.MANAGER, null);
    }

    public static ProjectMember member(UUID projectId, UUID userId) {
        return new ProjectMember(projectId, userId, ProjectRole.MEMBER, null);
    }

    public boolean isManager() {
        return role == ProjectRole.MANAGER;
    }

    /** A project always keeps its Manager (EC-04, BR-04). */
    public void ensureRemovable() {
        if (isManager()) {
            throw new ConflictException("O gestor não pode ser removido do projeto.");
        }
    }

    public UUID projectId() {
        return projectId;
    }

    public UUID userId() {
        return userId;
    }

    public ProjectRole role() {
        return role;
    }

    public OffsetDateTime addedAt() {
        return addedAt;
    }
}
