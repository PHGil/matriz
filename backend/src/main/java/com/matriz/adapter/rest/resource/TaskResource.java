package com.matriz.adapter.rest.resource;

import java.net.URI;
import java.util.List;
import java.util.UUID;

import com.matriz.adapter.rest.auth.CurrentUser;
import com.matriz.adapter.rest.mapper.TaskMapper;
import com.matriz.adapter.rest.mapper.UserMapper;
import com.matriz.adapter.rest.to.task.AssigneeCreateTO;
import com.matriz.adapter.rest.to.task.TaskCreateTO;
import com.matriz.adapter.rest.to.task.TaskTO;
import com.matriz.adapter.rest.to.task.TaskUpdateTO;
import com.matriz.adapter.rest.to.user.UserSummaryTO;
import com.matriz.application.port.in.TaskUseCases;
import com.matriz.domain.exception.ValidationException;
import com.matriz.domain.task.TaskChanges;
import com.matriz.domain.task.TaskPriority;
import com.matriz.domain.task.TaskStatus;

import io.quarkus.security.Authenticated;
import jakarta.enterprise.context.RequestScoped;
import jakarta.ws.rs.core.Response;

import lombok.RequiredArgsConstructor;

@RequestScoped
@Authenticated
@RequiredArgsConstructor
public class TaskResource implements TaskRouter {

    private final CurrentUser currentUser;
    private final TaskUseCases taskUseCases;
    private final TaskMapper mapper;
    private final UserMapper userMapper;

    @Override
    public List<TaskTO> list(UUID projectId, String status, String priority) {
        return mapper.toTOs(taskUseCases.list(projectId, currentUser.id(),
                parse(TaskStatus.class, "status", status), parse(TaskPriority.class, "priority", priority)));
    }

    @Override
    public Response create(UUID projectId, TaskCreateTO body) {
        if (body == null) {
            throw new ValidationException("title", "O título é obrigatório.");
        }
        TaskTO created = mapper.toTO(taskUseCases.create(projectId, currentUser.id(), body.title(),
                body.description(), body.priority(), body.dueDate()));
        return Response.created(URI.create("projects/" + projectId + "/tasks/" + created.id()))
                .entity(created).build();
    }

    @Override
    public TaskTO get(UUID projectId, UUID taskId) {
        return mapper.toTO(taskUseCases.get(projectId, taskId, currentUser.id()));
    }

    @Override
    public TaskTO update(UUID projectId, UUID taskId, TaskUpdateTO body) {
        TaskChanges changes = mapper.toChanges(body);
        return mapper.toTO(taskUseCases.update(projectId, taskId, currentUser.id(), changes));
    }

    @Override
    public void delete(UUID projectId, UUID taskId) {
        taskUseCases.delete(projectId, taskId, currentUser.id());
    }

    @Override
    public Response addAssignee(UUID projectId, UUID taskId, AssigneeCreateTO body) {
        UserSummaryTO assignee = userMapper.toSummaryTO(
                taskUseCases.assign(projectId, taskId, currentUser.id(), body == null ? null : body.userId()));
        return Response.status(Response.Status.CREATED).entity(assignee).build();
    }

    @Override
    public void removeAssignee(UUID projectId, UUID taskId, UUID userId) {
        taskUseCases.unassign(projectId, taskId, currentUser.id(), userId);
    }

    /** Unknown filter values are a 400, not the 404 JAX-RS would give for a failed query param conversion. */
    private static <E extends Enum<E>> E parse(Class<E> type, String field, String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            return Enum.valueOf(type, value.strip());
        } catch (IllegalArgumentException e) {
            throw new ValidationException(field, "Valor de filtro inválido.");
        }
    }
}
