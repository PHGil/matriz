package pt.matriz.infrastructure.api;

import java.net.URI;
import java.util.List;
import java.util.UUID;

import io.quarkus.security.Authenticated;
import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.PATCH;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import pt.matriz.application.task.TaskService;
import pt.matriz.domain.shared.ValidationException;
import pt.matriz.domain.task.TaskChanges;
import pt.matriz.domain.task.TaskPriority;
import pt.matriz.domain.task.TaskStatus;
import pt.matriz.infrastructure.api.TaskDtos.TaskCreate;
import pt.matriz.infrastructure.api.TaskDtos.TaskUpdate;
import pt.matriz.infrastructure.api.TaskDtos.TaskView;

@Path("/projects/{projectId}/tasks")
@Authenticated
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class TaskResource {

    @Inject
    CurrentUser currentUser;

    @Inject
    TaskService tasks;

    @GET
    public List<TaskView> list(@PathParam("projectId") UUID projectId, @QueryParam("status") String status,
            @QueryParam("priority") String priority) {
        return tasks.list(projectId, currentUser.id(),
                parse(TaskStatus.class, "status", status), parse(TaskPriority.class, "priority", priority))
                .stream().map(TaskView::of).toList();
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

    @POST
    public Response create(@PathParam("projectId") UUID projectId, TaskCreate body) {
        if (body == null) {
            throw new ValidationException("title", "O título é obrigatório.");
        }
        TaskView created = TaskView.of(tasks.create(projectId, currentUser.id(), body.title(), body.description(),
                body.priority(), body.dueDate()));
        return Response.created(URI.create("projects/" + projectId + "/tasks/" + created.id()))
                .entity(created).build();
    }

    @GET
    @Path("/{taskId}")
    public TaskView get(@PathParam("projectId") UUID projectId, @PathParam("taskId") UUID taskId) {
        return TaskView.of(tasks.get(projectId, taskId, currentUser.id()));
    }

    @PATCH
    @Path("/{taskId}")
    public TaskView update(@PathParam("projectId") UUID projectId, @PathParam("taskId") UUID taskId,
            TaskUpdate body) {
        TaskChanges changes = body == null ? null : body.toChanges();
        return TaskView.of(tasks.update(projectId, taskId, currentUser.id(), changes));
    }

    @DELETE
    @Path("/{taskId}")
    public void delete(@PathParam("projectId") UUID projectId, @PathParam("taskId") UUID taskId) {
        tasks.delete(projectId, taskId, currentUser.id());
    }
}
