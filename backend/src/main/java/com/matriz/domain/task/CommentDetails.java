package com.matriz.domain.task;

import com.matriz.domain.user.User;

/** A comment with its author. */
public record CommentDetails(Comment comment, User author) {
}
