package com.matriz.application.service;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.matriz.application.port.in.TaskUseCases;
import com.matriz.application.port.out.persistence.DomainMemberRepository;
import com.matriz.application.port.out.persistence.DomainTaskAssigneeRepository;
import com.matriz.application.port.out.persistence.DomainTaskRepository;
import com.matriz.application.port.out.persistence.DomainUserRepository;
import com.matriz.domain.exception.ConflictException;
import com.matriz.domain.exception.NotFoundException;
import com.matriz.domain.exception.ValidationException;
import com.matriz.domain.task.Progress;
import com.matriz.domain.task.Task;
import com.matriz.domain.task.TaskChanges;
import com.matriz.domain.task.TaskDetails;
import com.matriz.domain.task.TaskPriority;
import com.matriz.domain.task.TaskStatus;
import com.matriz.domain.user.User;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;

import lombok.RequiredArgsConstructor;

@ApplicationScoped
@RequiredArgsConstructor
public class TaskService implements TaskUseCases {

    private final DomainTaskRepository taskRepository;
    private final DomainTaskAssigneeRepository assigneeRepository;
    private final DomainUserRepository userRepository;
    private final DomainMemberRepository memberRepository;
    private final ProjectAccess access;
    private final TaskLookup taskLookup;

    @Override
    public List<TaskDetails> list(UUID projectId, UUID userId, TaskStatus status, TaskPriority priority) {
        access.requireMember(projectId, userId);
        return withAssignees(taskRepository.findByProject(projectId, status, priority));
    }

    @Override
    @Transactional
    public TaskDetails create(UUID projectId, UUID userId, String title, String description, TaskPriority priority,
            LocalDate dueDate) {
        access.requireMember(projectId, userId);
        Task saved = taskRepository.save(Task.create(projectId, title, description, priority, dueDate));
        return new TaskDetails(saved, List.of());
    }

    @Override
    public TaskDetails get(UUID projectId, UUID taskId, UUID userId) {
        access.requireMember(projectId, userId);
        return withAssignees(List.of(load(projectId, taskId))).get(0);
    }

    @Override
    public Progress progress(UUID projectId, UUID userId) {
        access.requireMember(projectId, userId);
        return taskRepository.progress(projectId);
    }

    @Override
    @Transactional
    public void delete(UUID projectId, UUID taskId, UUID userId) {
        access.requireManager(projectId, userId);
        taskRepository.delete(load(projectId, taskId).getId());
    }

    @Override
    @Transactional
    public TaskDetails update(UUID projectId, UUID taskId, UUID userId, TaskChanges changes) {
        access.requireMember(projectId, userId);
        Task task = load(projectId, taskId);
        task.change(changes);
        return withAssignees(List.of(taskRepository.save(task))).get(0);
    }

    @Override
    @Transactional
    public User assign(UUID projectId, UUID taskId, UUID userId, UUID assigneeId) {
        access.requireMember(projectId, userId);
        Task task = load(projectId, taskId);
        if (assigneeId == null) {
            throw new ValidationException("userId", "Indique o utilizador.");
        }
        User assignee = userRepository.findById(assigneeId)
                .filter(User::isActive)
                .filter(u -> memberRepository.find(projectId, u.getId()).isPresent())
                .orElseThrow(() -> new ValidationException("userId", "O utilizador não é membro ativo do projeto."));
        if (assigneeRepository.exists(task.getId(), assigneeId)) {
            throw new ConflictException("O utilizador já é responsável por esta tarefa.");
        }
        assigneeRepository.add(task.getId(), assigneeId);
        return assignee;
    }

    @Override
    @Transactional
    public void unassign(UUID projectId, UUID taskId, UUID userId, UUID assigneeId) {
        access.requireMember(projectId, userId);
        Task task = load(projectId, taskId);
        if (!assigneeRepository.remove(task.getId(), assigneeId)) {
            throw new NotFoundException();
        }
    }

    /** The task must belong to the project in the URL, otherwise it is "not found" (EC-05). */
    private Task load(UUID projectId, UUID taskId) {
        return taskLookup.require(projectId, taskId);
    }

    private List<TaskDetails> withAssignees(List<Task> tasks) {
        Map<UUID, List<UUID>> userIdsByTask = assigneeRepository.userIdsByTask(tasks.stream().map(Task::getId).toList());
        Collection<UUID> userIds = userIdsByTask.values().stream().flatMap(List::stream).distinct().toList();
        Map<UUID, User> usersById = userRepository.findAllById(userIds).stream()
                .collect(Collectors.toMap(User::getId, Function.identity()));
        return tasks.stream()
                .map(t -> new TaskDetails(t, userIdsByTask.getOrDefault(t.getId(), List.of()).stream()
                        .map(usersById::get).toList()))
                .toList();
    }
}
