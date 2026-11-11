package com.matriz.domain.task;

import java.time.OffsetDateTime;
import java.util.Objects;
import java.util.UUID;

import com.matriz.domain.validation.FieldValidator;

import lombok.Getter;

/** A comment on a task (FR-27). Content 1–2000 characters; author and date come from the server. */
@Getter
public class Comment {

    static final int CONTENT_MAX = 2000;

    private final UUID id;
    private final UUID taskId;
    private final UUID authorId;
    private final String content;
    private final OffsetDateTime createdAt;

    /** Rebuilds a stored comment; no validation. */
    public Comment(UUID id, UUID taskId, UUID authorId, String content, OffsetDateTime createdAt) {
        this.id = Objects.requireNonNull(id);
        this.taskId = Objects.requireNonNull(taskId);
        this.authorId = Objects.requireNonNull(authorId);
        this.content = content;
        this.createdAt = createdAt;
    }

    public static Comment create(UUID taskId, UUID authorId, String content) {
        FieldValidator v = new FieldValidator();
        String validContent = v.requiredText("content", content, 1, CONTENT_MAX, "O comentário");
        v.throwIfInvalid();
        return new Comment(UUID.randomUUID(), taskId, authorId, validContent, null);
    }
}
