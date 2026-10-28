package pt.matriz.infrastructure.persistence;

import java.util.List;
import java.util.UUID;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import pt.matriz.application.project.ProjectQueries;
import pt.matriz.domain.project.ProjectRole;

/** SQL read model: one query for the project list with task counts per status. */
@ApplicationScoped
public class SqlProjectQueries implements ProjectQueries {

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

    @Override
    @SuppressWarnings("unchecked")
    public List<ProjectSummary> summariesFor(UUID userId, String nameContains) {
        boolean search = nameContains != null;
        var query = em.createNativeQuery(SUMMARY_SQL.formatted(search ? "where lower(p.name) like :pattern escape '\\'" : ""))
                .setParameter("userId", userId);
        if (search) {
            query.setParameter("pattern", LikePattern.contains(nameContains));
        }
        return ((List<Object[]>) query.getResultList()).stream()
                .map(r -> new ProjectSummary((UUID) r[0], (String) r[1], (String) r[2],
                        ProjectRole.valueOf((String) r[3]),
                        new StatusCounts(toLong(r[4]), toLong(r[5]), toLong(r[6]), toLong(r[7]))))
                .toList();
    }

    private static long toLong(Object value) {
        return ((Number) value).longValue();
    }
}
