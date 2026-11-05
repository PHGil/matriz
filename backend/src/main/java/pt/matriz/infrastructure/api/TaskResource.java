package pt.matriz.infrastructure.api;

import java.net.URI;
import java.util.List;
import java.util.UUID;

import io.quarkus.security.Authenticated;
import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import pt.matriz.application.task.TaskService;
import pt.matriz.domain.shared.ValidationException;
import pt.matriz.infrastructure.api.TaskDtos.TaskCreate;
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
    public List<TaskView> list(@PathParam("projectId") UUID projectId) {
        return tasks.list(projectId, currentUser.id(), null, null).stream().map(TaskView::of).toList();
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
}
