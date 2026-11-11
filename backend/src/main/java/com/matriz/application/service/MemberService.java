package com.matriz.application.service;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.matriz.application.port.in.MemberUseCases;
import com.matriz.application.port.out.persistence.DomainMemberRepository;
import com.matriz.application.port.out.persistence.DomainTaskAssigneeRepository;
import com.matriz.application.port.out.persistence.DomainUserRepository;
import com.matriz.domain.exception.ConflictException;
import com.matriz.domain.exception.NotFoundException;
import com.matriz.domain.exception.ValidationException;
import com.matriz.domain.project.MemberDetails;
import com.matriz.domain.project.ProjectMember;
import com.matriz.domain.user.User;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;

import lombok.RequiredArgsConstructor;

@ApplicationScoped
@RequiredArgsConstructor
public class MemberService implements MemberUseCases {

    private final DomainMemberRepository memberRepository;
    private final DomainUserRepository userRepository;
    private final DomainTaskAssigneeRepository assigneeRepository;
    private final ProjectAccess access;

    @Override
    public List<MemberDetails> list(UUID projectId, UUID userId) {
        access.requireMember(projectId, userId);
        List<ProjectMember> projectMembers = memberRepository.findByProject(projectId);
        Map<UUID, User> usersById = userRepository
                .findAllById(projectMembers.stream().map(ProjectMember::getUserId).toList())
                .stream().collect(Collectors.toMap(User::getId, Function.identity()));
        return projectMembers.stream()
                .map(m -> new MemberDetails(m, usersById.get(m.getUserId())))
                .sorted(Comparator.<MemberDetails, Boolean>comparing(d -> !d.member().isManager())
                        .thenComparing(d -> d.user().getName().toLowerCase()))
                .toList();
    }

    @Override
    @Transactional
    public MemberDetails add(UUID projectId, UUID userId, UUID newMemberId) {
        access.requireManager(projectId, userId);
        if (newMemberId == null) {
            throw new ValidationException("userId", "Indique o utilizador.");
        }
        User user = userRepository.findById(newMemberId)
                .filter(User::isActive)
                .orElseThrow(() -> new ValidationException("userId", "Utilizador inexistente ou inativo."));
        if (memberRepository.find(projectId, newMemberId).isPresent()) {
            throw new ConflictException("O utilizador já é membro do projeto.");
        }
        return new MemberDetails(memberRepository.add(ProjectMember.member(projectId, newMemberId)), user);
    }

    @Override
    @Transactional
    public void remove(UUID projectId, UUID userId, UUID memberId) {
        access.requireManager(projectId, userId);
        ProjectMember member = memberRepository.find(projectId, memberId).orElseThrow(NotFoundException::new);
        member.ensureRemovable();
        assigneeRepository.removeUserFromProjectTasks(projectId, memberId);
        memberRepository.remove(projectId, memberId);
    }
}
