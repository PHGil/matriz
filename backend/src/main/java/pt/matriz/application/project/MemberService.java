package pt.matriz.application.project;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import pt.matriz.domain.project.MemberRepository;
import pt.matriz.domain.project.ProjectMember;
import pt.matriz.domain.shared.ConflictException;
import pt.matriz.domain.shared.NotFoundException;
import pt.matriz.domain.shared.ValidationException;
import pt.matriz.domain.task.TaskAssigneeRepository;
import pt.matriz.domain.user.User;
import pt.matriz.domain.user.UserRepository;

@ApplicationScoped
public class MemberService {

    public record MemberDetails(ProjectMember member, User user) {
    }

    @Inject
    MemberRepository members;

    @Inject
    UserRepository users;

    @Inject
    TaskAssigneeRepository assignees;

    @Inject
    ProjectAccess access;

    /** Manager first, then by name. Also the list of eligible assignees (FR-24). */
    public List<MemberDetails> list(UUID projectId, UUID userId) {
        access.requireMember(projectId, userId);
        List<ProjectMember> projectMembers = members.findByProject(projectId);
        Map<UUID, User> byId = users.findAllById(projectMembers.stream().map(ProjectMember::userId).toList())
                .stream().collect(Collectors.toMap(User::id, Function.identity()));
        return projectMembers.stream()
                .map(m -> new MemberDetails(m, byId.get(m.userId())))
                .sorted(Comparator.<MemberDetails, Boolean>comparing(d -> !d.member().isManager())
                        .thenComparing(d -> d.user().name().toLowerCase()))
                .toList();
    }

    @Transactional
    public MemberDetails add(UUID projectId, UUID userId, UUID newMemberId) {
        access.requireManager(projectId, userId);
        if (newMemberId == null) {
            throw new ValidationException("userId", "Indique o utilizador.");
        }
        User user = users.findById(newMemberId)
                .filter(User::isActive)
                .orElseThrow(() -> new ValidationException("userId", "Utilizador inexistente ou inativo."));
        if (members.find(projectId, newMemberId).isPresent()) {
            throw new ConflictException("O utilizador já é membro do projeto.");
        }
        return new MemberDetails(members.add(ProjectMember.member(projectId, newMemberId)), user);
    }

    /** Also removes the member's task assignments in this project (EC-02, BR-06). */
    @Transactional
    public void remove(UUID projectId, UUID userId, UUID memberId) {
        access.requireManager(projectId, userId);
        ProjectMember member = members.find(projectId, memberId).orElseThrow(NotFoundException::new);
        member.ensureRemovable();
        assignees.removeUserFromProjectTasks(projectId, memberId);
        members.remove(projectId, memberId);
    }
}
