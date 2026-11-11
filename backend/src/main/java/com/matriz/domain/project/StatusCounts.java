package com.matriz.domain.project;

/** Number of tasks per status. */
public record StatusCounts(long todo, long inProgress, long done, long total) {
}
