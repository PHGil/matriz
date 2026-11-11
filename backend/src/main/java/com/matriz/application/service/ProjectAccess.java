package com.matriz.application.service;

import java.util.UUID;

import com.matriz.application.port.out.persistence.DomainMemberRepository;
import com.matriz.domain.exception.AccessDeniedException;
import com.matriz.domain.exception.NotFoundException;
import com.matriz.domain.project.ProjectMember;

import jakarta.enterprise.context.ApplicationScoped;

import lombok.RequiredArgsConstructor;

/**
 * Project-level authorisation (S-03). Non-members get "not found" so the project's existence is not revealed
 * (EC-05); members without the required role get "access denied".
 */
@ApplicationScoped
@RequiredArgsConstructor
public class ProjectAccess {

    private final DomainMemberRepository memberRepository;

    public ProjectMember requireMember(UUID projectId, UUID userId) {
        return memberRepository.find(projectId, userId).orElseThrow(NotFoundException::new);
    }

    public ProjectMember requireManager(UUID projectId, UUID userId) {
        ProjectMember member = requireMember(projectId, userId);
        if (!member.isManager()) {
            throw new AccessDeniedException();
        }
        return member;
    }
}
