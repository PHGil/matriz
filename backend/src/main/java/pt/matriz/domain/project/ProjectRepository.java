package pt.matriz.domain.project;

import java.util.Optional;
import java.util.UUID;

/** Port: storage of projects. */
public interface ProjectRepository {

    Optional<Project> findById(UUID id);

    /** Inserts or updates; returns the stored state (with timestamps). */
    Project save(Project project);

    /** Deletes the project with its members, tasks, assignments and comments (FR-15, BR-11). */
    void delete(UUID id);
}
