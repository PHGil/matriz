package pt.matriz.infrastructure.api;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import pt.matriz.application.task.TaskService.TaskDetails;
import pt.matriz.domain.task.TaskChanges;
import pt.matriz.domain.task.TaskPriority;
import pt.matriz.domain.task.TaskStatus;
import pt.matriz.infrastructure.api.ProjectDtos.UserSummary;

/** Request and response bodies of the tasks API (openapi.yaml). */
public final class TaskDtos {

    private TaskDtos() {
    }

    public record TaskCreate(String title, String description, TaskPriority priority, LocalDate dueDate) {
    }

    /**
     * Absent field = unchanged ({@code null}); {@code null} clears description/dueDate and is rejected for the
     * others. A class, not a record: Jackson would turn absent record components into {@code Optional.empty()}.
     */
    public static class TaskUpdate {
        public Optional<String> title;
        public Optional<String> description;
        public Optional<TaskStatus> status;
        public Optional<TaskPriority> priority;
        public Optional<LocalDate> dueDate;

        TaskChanges toChanges() {
            return new TaskChanges(title, description, status, priority, dueDate);
        }
    }

    public record TaskView(UUID id, UUID projectId, String title, String description, TaskStatus status,
            TaskPriority priority, LocalDate dueDate, List<UserSummary> assignees, OffsetDateTime createdAt,
            OffsetDateTime updatedAt) {

        static TaskView of(TaskDetails d) {
            var t = d.task();
            return new TaskView(t.id(), t.projectId(), t.title(), t.description(), t.status(), t.priority(),
                    t.dueDate(),
                    d.assignees().stream().map(u -> new UserSummary(u.id(), u.name(), u.isActive())).toList(),
                    t.createdAt(), t.updatedAt());
        }
    }
}
