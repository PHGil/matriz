package com.matriz.domain.task;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import com.matriz.domain.exception.ValidationException;

class TaskTest {

    private final UUID projectId = UUID.randomUUID();

    @Test
    void newTaskStartsAsTodoWithMediumPriority() {
        Task task = Task.create(projectId, "  Rever proposta  ", " ", null, null);
        assertEquals("Rever proposta", task.getTitle());
        assertNull(task.getDescription());
        assertEquals(TaskStatus.TODO, task.getStatus());
        assertEquals(TaskPriority.MEDIUM, task.getPriority());
    }

    @Test
    void pastDueDateIsAccepted() {
        LocalDate past = LocalDate.of(2020, 1, 1);
        assertEquals(past, Task.create(projectId, "Tarefa atrasada", null, TaskPriority.HIGH, past).getDueDate());
    }

    @Test
    void invalidTitleOrDescriptionIsRejected() {
        assertThrows(ValidationException.class, () -> Task.create(projectId, "   ", null, null, null));
        assertThrows(ValidationException.class, () -> Task.create(projectId, "ab", null, null, null));
        assertThrows(ValidationException.class, () -> Task.create(projectId, "x".repeat(151), null, null, null));
        assertThrows(ValidationException.class, () -> Task.create(projectId, "Válida", "d".repeat(2001), null, null));
    }

    @Test
    void anyStatusTransitionIsAllowedIncludingReopening() {
        Task task = Task.create(projectId, "Tarefa", null, null, null);
        task.change(TaskChanges.status(TaskStatus.DONE));
        assertEquals(TaskStatus.DONE, task.getStatus());
        task.change(TaskChanges.status(TaskStatus.TODO));
        assertEquals(TaskStatus.TODO, task.getStatus());
    }

    @Test
    void changeAppliesOnlyGivenFieldsAndRejectsClearingRequiredOnes() {
        Task task = Task.create(projectId, "Tarefa", "Desc", TaskPriority.HIGH, LocalDate.of(2026, 12, 1));
        task.change(new TaskChanges(Optional.of("Novo título"), Optional.empty(), null, null, Optional.empty()));
        assertEquals("Novo título", task.getTitle());
        assertNull(task.getDescription());
        assertNull(task.getDueDate());
        assertEquals(TaskPriority.HIGH, task.getPriority(), "absent fields stay unchanged");

        assertThrows(ValidationException.class,
                () -> task.change(new TaskChanges(null, null, Optional.empty(), null, null)));
        assertThrows(ValidationException.class,
                () -> task.change(new TaskChanges(null, null, null, Optional.empty(), null)));
        assertThrows(ValidationException.class,
                () -> task.change(new TaskChanges(null, null, null, null, null)));
    }
}
