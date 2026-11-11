package com.matriz.adapter.rest.resource;

import java.util.List;
import java.util.UUID;

import com.matriz.adapter.rest.to.project.MemberCreateTO;
import com.matriz.adapter.rest.to.project.MemberTO;
import com.matriz.adapter.rest.to.project.ProjectCreateTO;
import com.matriz.adapter.rest.to.project.ProjectProgressTO;
import com.matriz.adapter.rest.to.project.ProjectSummaryTO;
import com.matriz.adapter.rest.to.project.ProjectTO;
import com.matriz.adapter.rest.to.project.ProjectUpdateTO;

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

/** REST endpoints of projects, their progress and their members. */
@Path("/projects")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public interface ProjectRouter {

    @GET
    List<ProjectSummaryTO> list(@QueryParam("q") String q);

    @POST
    Response create(ProjectCreateTO body);

    @GET
    @Path("/{projectId}")
    ProjectTO get(@PathParam("projectId") UUID projectId);

    @PATCH
    @Path("/{projectId}")
    ProjectTO update(@PathParam("projectId") UUID projectId, ProjectUpdateTO body);

    @DELETE
    @Path("/{projectId}")
    void delete(@PathParam("projectId") UUID projectId);

    @GET
    @Path("/{projectId}/progress")
    ProjectProgressTO progress(@PathParam("projectId") UUID projectId);

    @GET
    @Path("/{projectId}/members")
    List<MemberTO> listMembers(@PathParam("projectId") UUID projectId);

    @POST
    @Path("/{projectId}/members")
    Response addMember(@PathParam("projectId") UUID projectId, MemberCreateTO body);

    @DELETE
    @Path("/{projectId}/members/{userId}")
    void removeMember(@PathParam("projectId") UUID projectId, @PathParam("userId") UUID userId);
}
