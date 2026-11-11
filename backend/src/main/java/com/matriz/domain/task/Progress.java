package com.matriz.domain.task;

/** Task counts per status and the percentage done (FR-31, BR-09). */
public record Progress(long todo, long inProgress, long done) {

    public long total() {
        return todo + inProgress + done;
    }

    /** round(100 × done / total), half up; {@code null} when there are no tasks (EC-06). */
    public Integer percentDone() {
        long total = total();
        return total == 0 ? null : (int) Math.round(100.0 * done / total);
    }
}
