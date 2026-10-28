package pt.matriz.domain.project;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/** Port: storage of project memberships. */
public interface MemberRepository {

    Optional<ProjectMember> find(UUID projectId, UUID userId);

    List<ProjectMember> findByProject(UUID projectId);

    /** Inserts; returns the stored state (with the date added). */
    ProjectMember add(ProjectMember member);

    void remove(UUID projectId, UUID userId);
}
