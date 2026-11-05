package pt.matriz.domain.task;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.Objects;
import java.util.UUID;

import pt.matriz.domain.shared.FieldValidator;
import pt.matriz.domain.shared.ValidationException;

/** A unit of work in a project (FR-20 – FR-26). Title 3–150 characters, description up to 2000. */
public class Task {

    static final int TITLE_MIN = 3;
    static final int TITLE_MAX = 150;
    static final int DESCRIPTION_MAX = 2000;

    private final UUID id;
    private final UUID projectId;
    private String title;
    private String description;
    private TaskStatus status;
    private TaskPriority priority;
    private LocalDate dueDate;
    private final OffsetDateTime createdAt;
    private final OffsetDateTime updatedAt;

    /** Rebuilds a stored task; no validation. */
    public Task(UUID id, UUID projectId, String title, String description, TaskStatus status, TaskPriority priority,
            LocalDate dueDate, OffsetDateTime createdAt, OffsetDateTime updatedAt) {
        this.id = Objects.requireNonNull(id);
        this.projectId = Objects.requireNonNull(projectId);
        this.title = title;
        this.description = description;
        this.status = status;
        this.priority = priority;
        this.dueDate = dueDate;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    /** New tasks start as TODO; priority defaults to MEDIUM; past due dates are allowed (EC-09). */
    public static Task create(UUID projectId, String title, String description, TaskPriority priority,
            LocalDate dueDate) {
        FieldValidator v = new FieldValidator();
        String validTitle = v.requiredText("title", title, TITLE_MIN, TITLE_MAX, "O título");
        String validDescription = v.optionalText("description", description, DESCRIPTION_MAX, "A descrição");
        v.throwIfInvalid();
        return new Task(UUID.randomUUID(), projectId, validTitle, validDescription, TaskStatus.TODO,
                priority == null ? TaskPriority.MEDIUM : priority, dueDate, null, null);
    }

    /** Applies a partial change; any status transition is allowed, including reopening (US-06 AC2). */
    public void change(TaskChanges changes) {
        if (changes == null || changes.isEmpty()) {
            throw new ValidationException("title", "Indique pelo menos um campo a alterar.");
        }
        FieldValidator v = new FieldValidator();
        String validTitle = changes.title() == null ? null
                : v.requiredText("title", changes.title().orElse(null), TITLE_MIN, TITLE_MAX, "O título");
        String validDescription = changes.description() == null ? null
                : v.optionalText("description", changes.description().orElse(null), DESCRIPTION_MAX, "A descrição");
        v.throwIfInvalid();
        if (changes.status() != null && changes.status().isEmpty()) {
            throw new ValidationException("status", "O estado é obrigatório.");
        }
        if (changes.priority() != null && changes.priority().isEmpty()) {
            throw new ValidationException("priority", "A prioridade é obrigatória.");
        }
        if (changes.title() != null) {
            title = validTitle;
        }
        if (changes.description() != null) {
            description = validDescription;
        }
        if (changes.status() != null) {
            status = changes.status().get();
        }
        if (changes.priority() != null) {
            priority = changes.priority().get();
        }
        if (changes.dueDate() != null) {
            dueDate = changes.dueDate().orElse(null);
        }
    }

    public UUID id() {
        return id;
    }

    public UUID projectId() {
        return projectId;
    }

    public String title() {
        return title;
    }

    public String description() {
        return description;
    }

    public TaskStatus status() {
        return status;
    }

    public TaskPriority priority() {
        return priority;
    }

    public LocalDate dueDate() {
        return dueDate;
    }

    public OffsetDateTime createdAt() {
        return createdAt;
    }

    public OffsetDateTime updatedAt() {
        return updatedAt;
    }
}
