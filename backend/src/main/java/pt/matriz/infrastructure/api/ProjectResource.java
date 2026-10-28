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
import pt.matriz.application.project.MemberService;
import pt.matriz.application.project.ProjectService;
import pt.matriz.domain.shared.ValidationException;
import pt.matriz.infrastructure.api.ProjectDtos.MemberView;
import pt.matriz.infrastructure.api.ProjectDtos.ProjectCreate;
import pt.matriz.infrastructure.api.ProjectDtos.ProjectSummary;
import pt.matriz.infrastructure.api.ProjectDtos.ProjectUpdate;
import pt.matriz.infrastructure.api.ProjectDtos.ProjectView;
import pt.matriz.infrastructure.api.ProjectDtos.UserRef;

@Path("/projects")
@Authenticated
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class ProjectResource {

    @Inject
    CurrentUser currentUser;

    @Inject
    ProjectService projects;

    @Inject
    MemberService members;

    @GET
    public List<ProjectSummary> list(@QueryParam("q") String q) {
        return projects.list(currentUser.id(), q).stream().map(ProjectSummary::of).toList();
    }

    @POST
    public Response create(ProjectCreate body) {
        if (body == null) {
            throw new ValidationException("name", "O nome é obrigatório.");
        }
        ProjectView created = ProjectView.of(projects.create(currentUser.id(), body.name(), body.description()));
        return Response.created(URI.create("projects/" + created.id())).entity(created).build();
    }

    @GET
    @Path("/{projectId}")
    public ProjectView get(@PathParam("projectId") UUID projectId) {
        return ProjectView.of(projects.get(projectId, currentUser.id()));
    }

    @PATCH
    @Path("/{projectId}")
    public ProjectView update(@PathParam("projectId") UUID projectId, ProjectUpdate body) {
        ProjectUpdate changes = body == null ? new ProjectUpdate(null, null) : body;
        return ProjectView.of(projects.update(projectId, currentUser.id(), changes.name(), changes.description()));
    }

    @DELETE
    @Path("/{projectId}")
    public void delete(@PathParam("projectId") UUID projectId) {
        projects.delete(projectId, currentUser.id());
    }

    @GET
    @Path("/{projectId}/members")
    public List<MemberView> listMembers(@PathParam("projectId") UUID projectId) {
        return members.list(projectId, currentUser.id()).stream().map(MemberView::of).toList();
    }

    @POST
    @Path("/{projectId}/members")
    public Response addMember(@PathParam("projectId") UUID projectId, UserRef body) {
        MemberView added = MemberView.of(members.add(projectId, currentUser.id(), body == null ? null : body.userId()));
        return Response.status(Response.Status.CREATED).entity(added).build();
    }

    @DELETE
    @Path("/{projectId}/members/{userId}")
    public void removeMember(@PathParam("projectId") UUID projectId, @PathParam("userId") UUID userId) {
        members.remove(projectId, currentUser.id(), userId);
    }
}
