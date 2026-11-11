package com.matriz.application.service;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.matriz.application.port.in.CommentUseCases;
import com.matriz.application.port.out.persistence.DomainCommentRepository;
import com.matriz.application.port.out.persistence.DomainUserRepository;
import com.matriz.domain.exception.NotFoundException;
import com.matriz.domain.task.Comment;
import com.matriz.domain.task.CommentDetails;
import com.matriz.domain.user.User;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;

import lombok.RequiredArgsConstructor;

@ApplicationScoped
@RequiredArgsConstructor
public class CommentService implements CommentUseCases {

    private final DomainCommentRepository commentRepository;
    private final DomainUserRepository userRepository;
    private final ProjectAccess access;
    private final TaskLookup taskLookup;

    @Override
    public List<CommentDetails> list(UUID projectId, UUID taskId, UUID userId) {
        access.requireMember(projectId, userId);
        taskLookup.require(projectId, taskId);
        List<Comment> comments = commentRepository.findByTask(taskId);
        Map<UUID, User> authorsById = userRepository
                .findAllById(comments.stream().map(Comment::getAuthorId).distinct().toList())
                .stream().collect(Collectors.toMap(User::getId, Function.identity()));
        return comments.stream().map(c -> new CommentDetails(c, authorsById.get(c.getAuthorId()))).toList();
    }

    @Override
    @Transactional
    public CommentDetails add(UUID projectId, UUID taskId, UUID userId, String content) {
        access.requireMember(projectId, userId);
        taskLookup.require(projectId, taskId);
        Comment saved = commentRepository.add(Comment.create(taskId, userId, content));
        return new CommentDetails(saved, userRepository.findById(userId).orElseThrow(NotFoundException::new));
    }
}
