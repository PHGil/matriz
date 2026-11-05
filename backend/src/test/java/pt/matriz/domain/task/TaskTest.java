package pt.matriz.domain.task;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import pt.matriz.domain.shared.ValidationException;

class TaskTest {

    private final UUID projectId = UUID.randomUUID();

    @Test
    void newTaskStartsAsTodoWithMediumPriority() {
        Task task = Task.create(projectId, "  Rever proposta  ", " ", null, null);
        assertEquals("Rever proposta", task.title());
        assertNull(task.description());
        assertEquals(TaskStatus.TODO, task.status());
        assertEquals(TaskPriority.MEDIUM, task.priority());
    }

    @Test
    void pastDueDateIsAccepted() {
        LocalDate past = LocalDate.of(2020, 1, 1);
        assertEquals(past, Task.create(projectId, "Tarefa atrasada", null, TaskPriority.HIGH, past).dueDate());
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
        assertEquals(TaskStatus.DONE, task.status());
        task.change(TaskChanges.status(TaskStatus.TODO));
        assertEquals(TaskStatus.TODO, task.status());
    }

    @Test
    void changeAppliesOnlyGivenFieldsAndRejectsClearingRequiredOnes() {
        Task task = Task.create(projectId, "Tarefa", "Desc", TaskPriority.HIGH, LocalDate.of(2026, 12, 1));
        task.change(new TaskChanges(Optional.of("Novo título"), Optional.empty(), null, null, Optional.empty()));
        assertEquals("Novo título", task.title());
        assertNull(task.description());
        assertNull(task.dueDate());
        assertEquals(TaskPriority.HIGH, task.priority(), "absent fields stay unchanged");

        assertThrows(ValidationException.class,
                () -> task.change(new TaskChanges(null, null, Optional.empty(), null, null)));
        assertThrows(ValidationException.class,
                () -> task.change(new TaskChanges(null, null, null, Optional.empty(), null)));
        assertThrows(ValidationException.class,
                () -> task.change(new TaskChanges(null, null, null, null, null)));
    }
}
