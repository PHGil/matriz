package com.matriz.application.service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.matriz.application.port.in.ProjectUseCases;
import com.matriz.application.port.out.persistence.DomainMemberRepository;
import com.matriz.application.port.out.persistence.DomainProjectRepository;
import com.matriz.application.port.out.persistence.DomainProjectSummaryRepository;
import com.matriz.domain.exception.NotFoundException;
import com.matriz.domain.project.Project;
import com.matriz.domain.project.ProjectMember;
import com.matriz.domain.project.ProjectRole;
import com.matriz.domain.project.ProjectSummary;
import com.matriz.domain.project.ProjectWithRole;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;

import lombok.RequiredArgsConstructor;

@ApplicationScoped
@RequiredArgsConstructor
public class ProjectService implements ProjectUseCases {

    private final DomainProjectRepository projectRepository;
    private final DomainMemberRepository memberRepository;
    private final DomainProjectSummaryRepository summaryRepository;
    private final ProjectAccess access;

    @Override
    public List<ProjectSummary> list(UUID userId, String nameContains) {
        return summaryRepository.findByUser(userId,
                nameContains == null || nameContains.isBlank() ? null : nameContains.strip());
    }

    @Override
    @Transactional
    public ProjectWithRole create(UUID userId, String name, String description) {
        Project saved = projectRepository.save(Project.create(name, description));
        memberRepository.add(ProjectMember.manager(saved.getId(), userId));
        return new ProjectWithRole(saved, ProjectRole.MANAGER);
    }

    @Override
    public ProjectWithRole get(UUID projectId, UUID userId) {
        ProjectMember member = access.requireMember(projectId, userId);
        return new ProjectWithRole(load(projectId), member.getRole());
    }

    @Override
    @Transactional
    public ProjectWithRole update(UUID projectId, UUID userId, Optional<String> name, Optional<String> description) {
        access.requireManager(projectId, userId);
        Project project = load(projectId);
        project.change(name, description);
        return new ProjectWithRole(projectRepository.save(project), ProjectRole.MANAGER);
    }

    @Override
    @Transactional
    public void delete(UUID projectId, UUID userId) {
        access.requireManager(projectId, userId);
        projectRepository.delete(projectId);
    }

    private Project load(UUID projectId) {
        return projectRepository.findById(projectId).orElseThrow(NotFoundException::new);
    }
}
