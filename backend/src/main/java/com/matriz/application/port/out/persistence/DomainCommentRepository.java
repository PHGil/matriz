package com.matriz.application.port.out.persistence;

import java.util.List;
import java.util.UUID;

import com.matriz.domain.task.Comment;

/** Output port: storage of task comments (FR-27). */
public interface DomainCommentRepository {

    /** Oldest first. */
    List<Comment> findByTask(UUID taskId);

    /** Inserts; returns the stored state (with the creation date). */
    Comment add(Comment comment);
}
