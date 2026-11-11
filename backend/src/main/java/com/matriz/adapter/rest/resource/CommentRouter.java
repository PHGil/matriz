package com.matriz.adapter.rest.resource;

import java.util.List;
import java.util.UUID;

import com.matriz.adapter.rest.to.task.CommentCreateTO;
import com.matriz.adapter.rest.to.task.CommentTO;

import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

/** REST endpoints of the comments of a task. */
@Path("/projects/{projectId}/tasks/{taskId}/comments")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public interface CommentRouter {

    @GET
    List<CommentTO> list(@PathParam("projectId") UUID projectId, @PathParam("taskId") UUID taskId);

    @POST
    Response add(@PathParam("projectId") UUID projectId, @PathParam("taskId") UUID taskId, CommentCreateTO body);
}
