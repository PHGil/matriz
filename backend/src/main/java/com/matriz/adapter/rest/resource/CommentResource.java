package com.matriz.adapter.rest.resource;

import java.util.List;
import java.util.UUID;

import com.matriz.adapter.rest.auth.CurrentUser;
import com.matriz.adapter.rest.mapper.CommentMapper;
import com.matriz.adapter.rest.to.task.CommentCreateTO;
import com.matriz.adapter.rest.to.task.CommentTO;
import com.matriz.application.port.in.CommentUseCases;

import io.quarkus.security.Authenticated;
import jakarta.enterprise.context.RequestScoped;
import jakarta.ws.rs.core.Response;

import lombok.RequiredArgsConstructor;

@RequestScoped
@Authenticated
@RequiredArgsConstructor
public class CommentResource implements CommentRouter {

    private final CurrentUser currentUser;
    private final CommentUseCases commentUseCases;
    private final CommentMapper mapper;

    @Override
    public List<CommentTO> list(UUID projectId, UUID taskId) {
        return mapper.toTOs(commentUseCases.list(projectId, taskId, currentUser.id()));
    }

    @Override
    public Response add(UUID projectId, UUID taskId, CommentCreateTO body) {
        CommentTO created = mapper.toTO(
                commentUseCases.add(projectId, taskId, currentUser.id(), body == null ? null : body.content()));
        return Response.status(Response.Status.CREATED).entity(created).build();
    }
}
