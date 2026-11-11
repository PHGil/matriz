package com.matriz.domain.task;

import java.util.List;

import com.matriz.domain.user.User;

/** A task with its assignees. */
public record TaskDetails(Task task, List<User> assignees) {
}
