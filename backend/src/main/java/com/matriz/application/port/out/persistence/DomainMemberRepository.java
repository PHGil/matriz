package com.matriz.application.port.out.persistence;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.matriz.domain.project.ProjectMember;

/** Output port: storage of project memberships. */
public interface DomainMemberRepository {

    Optional<ProjectMember> find(UUID projectId, UUID userId);

    List<ProjectMember> findByProject(UUID projectId);

    /** Inserts; returns the stored state (with the date added). */
    ProjectMember add(ProjectMember member);

    void remove(UUID projectId, UUID userId);
}
