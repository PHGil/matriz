package com.matriz.application.port.out.persistence;

import java.util.Optional;
import java.util.UUID;

import com.matriz.domain.project.Project;

/** Output port: storage of projects. */
public interface DomainProjectRepository {

    Optional<Project> findById(UUID id);

    /** Inserts or updates; returns the stored state (with timestamps). */
    Project save(Project project);

    /** Deletes the project with its members, tasks, assignments and comments (FR-15, BR-11). */
    void delete(UUID id);
}
