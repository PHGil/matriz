package com.matriz.adapter.persistence.repository;

import java.util.Optional;
import java.util.UUID;

import com.matriz.adapter.persistence.entity.ProjectEntity;
import com.matriz.adapter.persistence.mapper.ProjectEntityMapper;
import com.matriz.application.port.out.persistence.DomainProjectRepository;
import com.matriz.domain.project.Project;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.EntityManager;

import lombok.RequiredArgsConstructor;

@ApplicationScoped
@RequiredArgsConstructor
public class ProjectRepository implements DomainProjectRepository {

    private final EntityManager em;
    private final ProjectEntityMapper mapper;

    @Override
    public Optional<Project> findById(UUID id) {
        return Optional.ofNullable(em.find(ProjectEntity.class, id)).map(mapper::toModel);
    }

    @Override
    public Project save(Project project) {
        ProjectEntity entity = em.find(ProjectEntity.class, project.getId());
        if (entity == null) {
            entity = mapper.toEntity(project);
            em.persist(entity);
        } else {
            mapper.update(project, entity);
        }
        em.flush();
        return mapper.toModel(entity);
    }

    /** Members, tasks, assignments and comments go with it through ON DELETE CASCADE (V1__schema.sql). */
    @Override
    public void delete(UUID id) {
        em.createNativeQuery("delete from project where id = :id").setParameter("id", id).executeUpdate();
    }
}
