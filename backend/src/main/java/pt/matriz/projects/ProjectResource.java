package pt.matriz.projects;

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
import pt.matriz.auth.CurrentUser;
import pt.matriz.projects.ProjectDtos.MemberView;
import pt.matriz.projects.ProjectDtos.ProjectCreate;
import pt.matriz.projects.ProjectDtos.ProjectSummary;
import pt.matriz.projects.ProjectDtos.ProjectUpdate;
import pt.matriz.projects.ProjectDtos.ProjectView;
import pt.matriz.projects.ProjectDtos.UserRef;

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
        return projects.list(currentUser.id(), q);
    }

    @POST
    public Response create(ProjectCreate body) {
        ProjectView created = projects.create(currentUser.id(), body);
        return Response.created(URI.create("projects/" + created.id())).entity(created).build();
    }

    @GET
    @Path("/{projectId}")
    public ProjectView get(@PathParam("projectId") UUID projectId) {
        return projects.get(projectId, currentUser.id());
    }

    @PATCH
    @Path("/{projectId}")
    public ProjectView update(@PathParam("projectId") UUID projectId, ProjectUpdate body) {
        return projects.update(projectId, currentUser.id(), body);
    }

    @DELETE
    @Path("/{projectId}")
    public void delete(@PathParam("projectId") UUID projectId) {
        projects.delete(projectId, currentUser.id());
    }

    @GET
    @Path("/{projectId}/members")
    public List<MemberView> listMembers(@PathParam("projectId") UUID projectId) {
        return members.list(projectId, currentUser.id());
    }

    @POST
    @Path("/{projectId}/members")
    public Response addMember(@PathParam("projectId") UUID projectId, UserRef body) {
        return Response.status(Response.Status.CREATED)
                .entity(members.add(projectId, currentUser.id(), body))
                .build();
    }

    @DELETE
    @Path("/{projectId}/members/{userId}")
    public void removeMember(@PathParam("projectId") UUID projectId, @PathParam("userId") UUID userId) {
        members.remove(projectId, currentUser.id(), userId);
    }
}
