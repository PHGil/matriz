package com.matriz.adapter.rest.resource;

import java.util.List;
import java.util.UUID;

import com.matriz.adapter.rest.to.task.AssigneeCreateTO;
import com.matriz.adapter.rest.to.task.TaskCreateTO;
import com.matriz.adapter.rest.to.task.TaskTO;
import com.matriz.adapter.rest.to.task.TaskUpdateTO;

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

/** REST endpoints of the tasks of a project. */
@Path("/projects/{projectId}/tasks")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public interface TaskRouter {

    @GET
    List<TaskTO> list(@PathParam("projectId") UUID projectId, @QueryParam("status") String status,
            @QueryParam("priority") String priority);

    @POST
    Response create(@PathParam("projectId") UUID projectId, TaskCreateTO body);

    @GET
    @Path("/{taskId}")
    TaskTO get(@PathParam("projectId") UUID projectId, @PathParam("taskId") UUID taskId);

    @PATCH
    @Path("/{taskId}")
    TaskTO update(@PathParam("projectId") UUID projectId, @PathParam("taskId") UUID taskId, TaskUpdateTO body);

    @DELETE
    @Path("/{taskId}")
    void delete(@PathParam("projectId") UUID projectId, @PathParam("taskId") UUID taskId);

    @POST
    @Path("/{taskId}/assignees")
    Response addAssignee(@PathParam("projectId") UUID projectId, @PathParam("taskId") UUID taskId,
            AssigneeCreateTO body);

    @DELETE
    @Path("/{taskId}/assignees/{userId}")
    void removeAssignee(@PathParam("projectId") UUID projectId, @PathParam("taskId") UUID taskId,
            @PathParam("userId") UUID userId);
}
