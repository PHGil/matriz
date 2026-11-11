package com.matriz.application.port.in;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.matriz.domain.project.ProjectSummary;
import com.matriz.domain.project.ProjectWithRole;

/** Input port: project use cases. */
public interface ProjectUseCases {

    List<ProjectSummary> list(UUID userId, String nameContains);

    ProjectWithRole create(UUID userId, String name, String description);

    ProjectWithRole get(UUID projectId, UUID userId);

    /** A {@code null} Optional leaves the field unchanged (PATCH semantics). */
    ProjectWithRole update(UUID projectId, UUID userId, Optional<String> name, Optional<String> description);

    void delete(UUID projectId, UUID userId);
}
