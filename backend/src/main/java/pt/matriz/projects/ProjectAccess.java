package pt.matriz.projects;

import java.util.UUID;

import io.quarkus.security.ForbiddenException;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.NotFoundException;

/**
 * Project-level authorisation (S-03). Non-members get 404 so the project's existence is not revealed (EC-05);
 * members without the required role get 403.
 */
@ApplicationScoped
public class ProjectAccess {

    public ProjectMember requireMember(UUID projectId, UUID userId) {
        ProjectMember member = ProjectMember.find(projectId, userId);
        if (member == null) {
            throw new NotFoundException();
        }
        return member;
    }

    public ProjectMember requireManager(UUID projectId, UUID userId) {
        ProjectMember member = requireMember(projectId, userId);
        if (member.role != ProjectRole.MANAGER) {
            throw new ForbiddenException();
        }
        return member;
    }
}
