package com.matriz.domain.project;

import com.matriz.domain.user.User;

/** A project membership together with the member's user data. */
public record MemberDetails(ProjectMember member, User user) {
}
