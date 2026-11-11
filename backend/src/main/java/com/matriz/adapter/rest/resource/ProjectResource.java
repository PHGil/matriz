package com.matriz.adapter.rest.resource;

import java.net.URI;
import java.util.List;
import java.util.UUID;

import com.matriz.adapter.rest.auth.CurrentUser;
import com.matriz.adapter.rest.mapper.ProjectMapper;
import com.matriz.adapter.rest.to.project.MemberCreateTO;
import com.matriz.adapter.rest.to.project.MemberTO;
import com.matriz.adapter.rest.to.project.ProjectCreateTO;
import com.matriz.adapter.rest.to.project.ProjectProgressTO;
import com.matriz.adapter.rest.to.project.ProjectSummaryTO;
import com.matriz.adapter.rest.to.project.ProjectTO;
import com.matriz.adapter.rest.to.project.ProjectUpdateTO;
import com.matriz.application.port.in.MemberUseCases;
import com.matriz.application.port.in.ProjectUseCases;
import com.matriz.application.port.in.TaskUseCases;
import com.matriz.domain.exception.ValidationException;

import io.quarkus.security.Authenticated;
import jakarta.enterprise.context.RequestScoped;
import jakarta.ws.rs.core.Response;

import lombok.RequiredArgsConstructor;

@RequestScoped
@Authenticated
@RequiredArgsConstructor
public class ProjectResource implements ProjectRouter {

    private final CurrentUser currentUser;
    private final ProjectUseCases projectUseCases;
    private final MemberUseCases memberUseCases;
    private final TaskUseCases taskUseCases;
    private final ProjectMapper mapper;

    @Override
    public List<ProjectSummaryTO> list(String q) {
        return mapper.toSummaryTOs(projectUseCases.list(currentUser.id(), q));
    }

    @Override
    public Response create(ProjectCreateTO body) {
        if (body == null) {
            throw new ValidationException("name", "O nome é obrigatório.");
        }
        ProjectTO created = mapper.toTO(projectUseCases.create(currentUser.id(), body.name(), body.description()));
        return Response.created(URI.create("projects/" + created.id())).entity(created).build();
    }

    @Override
    public ProjectTO get(UUID projectId) {
        return mapper.toTO(projectUseCases.get(projectId, currentUser.id()));
    }

    @Override
    public ProjectTO update(UUID projectId, ProjectUpdateTO body) {
        ProjectUpdateTO changes = body == null ? new ProjectUpdateTO() : body;
        return mapper.toTO(projectUseCases.update(projectId, currentUser.id(), changes.name, changes.description));
    }

    @Override
    public void delete(UUID projectId) {
        projectUseCases.delete(projectId, currentUser.id());
    }

    @Override
    public ProjectProgressTO progress(UUID projectId) {
        return mapper.toProgressTO(projectId, taskUseCases.progress(projectId, currentUser.id()));
    }

    @Override
    public List<MemberTO> listMembers(UUID projectId) {
        return mapper.toMemberTOs(memberUseCases.list(projectId, currentUser.id()));
    }

    @Override
    public Response addMember(UUID projectId, MemberCreateTO body) {
        MemberTO added = mapper.toMemberTO(
                memberUseCases.add(projectId, currentUser.id(), body == null ? null : body.userId()));
        return Response.status(Response.Status.CREATED).entity(added).build();
    }

    @Override
    public void removeMember(UUID projectId, UUID userId) {
        memberUseCases.remove(projectId, currentUser.id(), userId);
    }
}
