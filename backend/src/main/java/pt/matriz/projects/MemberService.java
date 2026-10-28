package pt.matriz.projects;

import java.util.List;
import java.util.UUID;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.NotFoundException;
import pt.matriz.projects.ProjectDtos.MemberView;
import pt.matriz.projects.ProjectDtos.UserRef;
import pt.matriz.projects.ProjectDtos.UserSummary;
import pt.matriz.shared.ConflictException;
import pt.matriz.shared.ValidationException;
import pt.matriz.users.AppUser;

@ApplicationScoped
public class MemberService {

    @Inject
    EntityManager em;

    @Inject
    ProjectAccess access;

    /** Manager first, then by name. Also the list of eligible assignees (FR-24). */
    public List<MemberView> list(UUID projectId, UUID userId) {
        access.requireMember(projectId, userId);
        return em.createQuery("""
                select m, u from ProjectMember m, AppUser u
                 where u.id = m.id.userId and m.id.projectId = :projectId
                 order by case when m.role = pt.matriz.projects.ProjectRole.MANAGER then 0 else 1 end, lower(u.name)
                """, Object[].class)
                .setParameter("projectId", projectId)
                .getResultStream()
                .map(r -> toView((ProjectMember) r[0], (AppUser) r[1]))
                .toList();
    }

    @Transactional
    public MemberView add(UUID projectId, UUID userId, UserRef body) {
        access.requireManager(projectId, userId);
        if (body == null || body.userId() == null) {
            throw new ValidationException("userId", "Indique o utilizador.");
        }
        AppUser target = AppUser.findById(body.userId());
        if (target == null || !target.active) {
            throw new ValidationException("userId", "Utilizador inexistente ou inativo.");
        }
        if (ProjectMember.find(projectId, target.id) != null) {
            throw new ConflictException("O utilizador já é membro do projeto.");
        }
        ProjectMember member = new ProjectMember();
        member.id = new ProjectMember.Key(projectId, target.id);
        member.role = ProjectRole.MEMBER;
        member.persist();
        em.flush();
        return toView(member, target);
    }

    /** Also removes the member's task assignments in this project (EC-02, BR-06). */
    @Transactional
    public void remove(UUID projectId, UUID userId, UUID targetUserId) {
        access.requireManager(projectId, userId);
        ProjectMember target = ProjectMember.find(projectId, targetUserId);
        if (target == null) {
            throw new NotFoundException();
        }
        if (target.role == ProjectRole.MANAGER) {
            throw new ConflictException("O gestor não pode ser removido do projeto.");
        }
        em.createNativeQuery("""
                delete from task_assignee a
                 using task t
                 where a.task_id = t.id and t.project_id = :projectId and a.user_id = :userId
                """)
                .setParameter("projectId", projectId)
                .setParameter("userId", targetUserId)
                .executeUpdate();
        target.delete();
    }

    private static MemberView toView(ProjectMember m, AppUser u) {
        return new MemberView(new UserSummary(u.id, u.name, u.active), m.role, m.addedAt);
    }
}
