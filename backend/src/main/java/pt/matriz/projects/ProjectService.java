package pt.matriz.projects;

import java.util.List;
import java.util.UUID;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import pt.matriz.projects.ProjectDtos.ProjectCreate;
import pt.matriz.projects.ProjectDtos.ProjectSummary;
import pt.matriz.projects.ProjectDtos.ProjectUpdate;
import pt.matriz.projects.ProjectDtos.ProjectView;
import pt.matriz.projects.ProjectDtos.StatusCounts;
import pt.matriz.shared.Validation;
import pt.matriz.shared.ValidationException;

@ApplicationScoped
public class ProjectService {

    private static final String SUMMARY_SQL = """
            select p.id, p.name, p.description, m.role,
                   count(t.id) filter (where t.status = 'TODO'),
                   count(t.id) filter (where t.status = 'IN_PROGRESS'),
                   count(t.id) filter (where t.status = 'DONE'),
                   count(t.id)
              from project p
              join project_member m on m.project_id = p.id and m.user_id = :userId
              left join task t on t.project_id = p.id
             %s
             group by p.id, p.name, p.description, m.role
             order by lower(p.name), p.id
            """;

    @Inject
    EntityManager em;

    @Inject
    ProjectAccess access;

    /** Projects the user belongs to, with task counts per status over all tasks (FR-13, FR-16, FR-34). */
    @SuppressWarnings("unchecked")
    public List<ProjectSummary> list(UUID userId, String q) {
        boolean search = q != null && !q.isBlank();
        var query = em.createNativeQuery(SUMMARY_SQL.formatted(search ? "where lower(p.name) like :pattern escape '\\'" : ""))
                .setParameter("userId", userId);
        if (search) {
            query.setParameter("pattern", Validation.containsPattern(q));
        }
        return ((List<Object[]>) query.getResultList()).stream()
                .map(r -> new ProjectSummary((UUID) r[0], (String) r[1], (String) r[2],
                        ProjectRole.valueOf((String) r[3]),
                        new StatusCounts(toLong(r[4]), toLong(r[5]), toLong(r[6]), toLong(r[7]))))
                .toList();
    }

    @Transactional
    public ProjectView create(UUID userId, ProjectCreate body) {
        if (body == null) {
            throw new ValidationException("name", "O nome é obrigatório.");
        }
        Validation v = new Validation();
        String name = v.requiredText("name", body.name(), 3, 100, "O nome");
        String description = v.optionalText("description", body.description(), 1000, "A descrição");
        v.throwIfInvalid();

        Project project = new Project();
        project.id = UUID.randomUUID();
        project.name = name;
        project.description = description;
        project.persist();

        ProjectMember manager = new ProjectMember();
        manager.id = new ProjectMember.Key(project.id, userId);
        manager.role = ProjectRole.MANAGER;
        manager.persist();
        em.flush();
        return ProjectView.of(project, ProjectRole.MANAGER);
    }

    public ProjectView get(UUID projectId, UUID userId) {
        ProjectMember member = access.requireMember(projectId, userId);
        return ProjectView.of(Project.findById(projectId), member.role);
    }

    @Transactional
    public ProjectView update(UUID projectId, UUID userId, ProjectUpdate body) {
        access.requireManager(projectId, userId);
        if (body == null || (body.name() == null && body.description() == null)) {
            throw new ValidationException("name", "Indique pelo menos um campo a alterar.");
        }
        Validation v = new Validation();
        String name = body.name() == null ? null
                : v.requiredText("name", body.name().orElse(null), 3, 100, "O nome");
        String description = body.description() == null ? null
                : v.optionalText("description", body.description().orElse(null), 1000, "A descrição");
        v.throwIfInvalid();

        Project project = Project.findById(projectId);
        if (name != null) {
            project.name = name;
        }
        if (body.description() != null) {
            project.description = description;
        }
        em.flush();
        return ProjectView.of(project, ProjectRole.MANAGER);
    }

    /** Members, tasks, assignments and comments are removed by ON DELETE CASCADE (FR-15, BR-11). */
    @Transactional
    public void delete(UUID projectId, UUID userId) {
        access.requireManager(projectId, userId);
        em.createNativeQuery("delete from project where id = :id").setParameter("id", projectId).executeUpdate();
    }

    private static long toLong(Object value) {
        return ((Number) value).longValue();
    }
}
