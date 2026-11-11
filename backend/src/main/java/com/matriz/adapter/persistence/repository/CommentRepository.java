package com.matriz.adapter.persistence.repository;

import java.util.List;
import java.util.UUID;

import com.matriz.adapter.persistence.entity.TaskCommentEntity;
import com.matriz.adapter.persistence.mapper.CommentEntityMapper;
import com.matriz.application.port.out.persistence.DomainCommentRepository;
import com.matriz.domain.task.Comment;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.EntityManager;

import lombok.RequiredArgsConstructor;

@ApplicationScoped
@RequiredArgsConstructor
public class CommentRepository implements DomainCommentRepository {

    private final EntityManager em;
    private final CommentEntityMapper mapper;

    @Override
    public List<Comment> findByTask(UUID taskId) {
        return em.createQuery("from TaskCommentEntity where taskId = :taskId order by createdAt, id",
                TaskCommentEntity.class)
                .setParameter("taskId", taskId)
                .getResultStream().map(mapper::toModel).toList();
    }

    @Override
    public Comment add(Comment comment) {
        TaskCommentEntity entity = mapper.toEntity(comment);
        em.persist(entity);
        em.flush();
        return mapper.toModel(entity);
    }
}
