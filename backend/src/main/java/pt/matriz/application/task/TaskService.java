package pt.matriz.application.task;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import pt.matriz.application.project.ProjectAccess;
import pt.matriz.domain.shared.NotFoundException;
import pt.matriz.domain.task.Progress;
import pt.matriz.domain.task.Task;
import pt.matriz.domain.task.TaskAssigneeRepository;
import pt.matriz.domain.task.TaskChanges;
import pt.matriz.domain.task.TaskPriority;
import pt.matriz.domain.task.TaskRepository;
import pt.matriz.domain.task.TaskStatus;
import pt.matriz.domain.user.User;
import pt.matriz.domain.user.UserRepository;

@ApplicationScoped
public class TaskService {

    /** A task with its assignees. */
    public record TaskDetails(Task task, List<User> assignees) {
    }

    @Inject
    TaskRepository tasks;

    @Inject
    TaskAssigneeRepository assignees;

    @Inject
    UserRepository users;

    @Inject
    ProjectAccess access;

    /** Any member can see the project's tasks; {@code null} filters are ignored (FR-32). */
    public List<TaskDetails> list(UUID projectId, UUID userId, TaskStatus status, TaskPriority priority) {
        access.requireMember(projectId, userId);
        return withAssignees(tasks.findByProject(projectId, status, priority));
    }

    @Transactional
    public TaskDetails create(UUID projectId, UUID userId, String title, String description, TaskPriority priority,
            LocalDate dueDate) {
        access.requireMember(projectId, userId);
        Task saved = tasks.save(Task.create(projectId, title, description, priority, dueDate));
        return new TaskDetails(saved, List.of());
    }

    public TaskDetails get(UUID projectId, UUID taskId, UUID userId) {
        access.requireMember(projectId, userId);
        return withAssignees(List.of(load(projectId, taskId))).get(0);
    }

    public Progress progress(UUID projectId, UUID userId) {
        access.requireMember(projectId, userId);
        return tasks.progress(projectId);
    }

    /** Only the Manager can delete tasks (FR-25). */
    @Transactional
    public void delete(UUID projectId, UUID taskId, UUID userId) {
        access.requireManager(projectId, userId);
        tasks.delete(load(projectId, taskId).id());
    }

    /** Members and the Manager can edit any field and change the status (FR-21, FR-23). Last write wins (EC-01). */
    @Transactional
    public TaskDetails update(UUID projectId, UUID taskId, UUID userId, TaskChanges changes) {
        access.requireMember(projectId, userId);
        Task task = load(projectId, taskId);
        task.change(changes);
        return withAssignees(List.of(tasks.save(task))).get(0);
    }

    /** The task must belong to the project in the URL, otherwise it is "not found" (EC-05). */
    Task load(UUID projectId, UUID taskId) {
        return tasks.findById(taskId)
                .filter(t -> t.projectId().equals(projectId))
                .orElseThrow(NotFoundException::new);
    }

    List<TaskDetails> withAssignees(List<Task> list) {
        Map<UUID, List<UUID>> idsByTask = assignees.userIdsByTask(list.stream().map(Task::id).toList());
        Collection<UUID> userIds = idsByTask.values().stream().flatMap(List::stream).distinct().toList();
        Map<UUID, User> usersById = users.findAllById(userIds).stream()
                .collect(Collectors.toMap(User::id, Function.identity()));
        return list.stream()
                .map(t -> new TaskDetails(t, idsByTask.getOrDefault(t.id(), List.of()).stream()
                        .map(usersById::get).toList()))
                .toList();
    }
}
