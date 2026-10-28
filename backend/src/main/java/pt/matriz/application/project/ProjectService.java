package pt.matriz.application.project;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import pt.matriz.domain.project.MemberRepository;
import pt.matriz.domain.project.Project;
import pt.matriz.domain.project.ProjectMember;
import pt.matriz.domain.project.ProjectRepository;
import pt.matriz.domain.project.ProjectRole;
import pt.matriz.domain.shared.NotFoundException;

@ApplicationScoped
public class ProjectService {

    /** A project as seen by a given user. */
    public record ProjectWithRole(Project project, ProjectRole myRole) {
    }

    @Inject
    ProjectRepository projects;

    @Inject
    MemberRepository members;

    @Inject
    ProjectQueries queries;

    @Inject
    ProjectAccess access;

    public List<ProjectQueries.ProjectSummary> list(UUID userId, String nameContains) {
        return queries.summariesFor(userId, nameContains == null || nameContains.isBlank() ? null : nameContains.strip());
    }

    @Transactional
    public ProjectWithRole create(UUID userId, String name, String description) {
        Project saved = projects.save(Project.create(name, description));
        members.add(ProjectMember.manager(saved.id(), userId));
        return new ProjectWithRole(saved, ProjectRole.MANAGER);
    }

    public ProjectWithRole get(UUID projectId, UUID userId) {
        ProjectMember member = access.requireMember(projectId, userId);
        return new ProjectWithRole(load(projectId), member.role());
    }

    /** A {@code null} Optional leaves the field unchanged (PATCH semantics). */
    @Transactional
    public ProjectWithRole update(UUID projectId, UUID userId, Optional<String> name, Optional<String> description) {
        access.requireManager(projectId, userId);
        Project project = load(projectId);
        project.change(name, description);
        return new ProjectWithRole(projects.save(project), ProjectRole.MANAGER);
    }

    @Transactional
    public void delete(UUID projectId, UUID userId) {
        access.requireManager(projectId, userId);
        projects.delete(projectId);
    }

    private Project load(UUID projectId) {
        return projects.findById(projectId).orElseThrow(NotFoundException::new);
    }
}
