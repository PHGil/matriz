package pt.matriz.application.project;

import java.util.UUID;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import pt.matriz.domain.project.MemberRepository;
import pt.matriz.domain.project.ProjectMember;
import pt.matriz.domain.shared.AccessDeniedException;
import pt.matriz.domain.shared.NotFoundException;

/**
 * Project-level authorisation (S-03). Non-members get "not found" so the project's existence is not revealed
 * (EC-05); members without the required role get "access denied".
 */
@ApplicationScoped
public class ProjectAccess {

    @Inject
    MemberRepository members;

    public ProjectMember requireMember(UUID projectId, UUID userId) {
        return members.find(projectId, userId).orElseThrow(NotFoundException::new);
    }

    public ProjectMember requireManager(UUID projectId, UUID userId) {
        ProjectMember member = requireMember(projectId, userId);
        if (!member.isManager()) {
            throw new AccessDeniedException();
        }
        return member;
    }
}
