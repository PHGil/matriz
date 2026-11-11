package com.matriz.application.port.in;

import java.util.List;
import java.util.UUID;

import com.matriz.domain.task.CommentDetails;

/** Input port: task comment use cases (FR-27). Any project member can list and add comments. */
public interface CommentUseCases {

    List<CommentDetails> list(UUID projectId, UUID taskId, UUID userId);

    /** The author is the authenticated user {@code userId}. */
    CommentDetails add(UUID projectId, UUID taskId, UUID userId, String content);
}
