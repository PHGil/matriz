package com.matriz.domain.project;

/** A project as seen by a given user. */
public record ProjectWithRole(Project project, ProjectRole myRole) {
}
