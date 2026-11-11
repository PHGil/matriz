package com.matriz.adapter.persistence.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.matriz.adapter.persistence.entity.ProjectMemberEntity;
import com.matriz.adapter.persistence.mapper.ProjectMemberEntityMapper;
import com.matriz.application.port.out.persistence.DomainMemberRepository;
import com.matriz.domain.project.ProjectMember;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.EntityManager;

import lombok.RequiredArgsConstructor;

@ApplicationScoped
@RequiredArgsConstructor
public class MemberRepository implements DomainMemberRepository {

    private final EntityManager em;
    private final ProjectMemberEntityMapper mapper;

    @Override
    public Optional<ProjectMember> find(UUID projectId, UUID userId) {
        return Optional.ofNullable(em.find(ProjectMemberEntity.class, new ProjectMemberEntity.Key(projectId, userId)))
                .map(mapper::toModel);
    }

    @Override
    public List<ProjectMember> findByProject(UUID projectId) {
        return em.createQuery("from ProjectMemberEntity where id.projectId = :projectId", ProjectMemberEntity.class)
                .setParameter("projectId", projectId)
                .getResultStream().map(mapper::toModel).toList();
    }

    @Override
    public ProjectMember add(ProjectMember member) {
        ProjectMemberEntity entity = mapper.toEntity(member);
        em.persist(entity);
        em.flush();
        return mapper.toModel(entity);
    }

    @Override
    public void remove(UUID projectId, UUID userId) {
        em.createQuery("delete from ProjectMemberEntity where id.projectId = :projectId and id.userId = :userId")
                .setParameter("projectId", projectId)
                .setParameter("userId", userId)
                .executeUpdate();
    }
}
