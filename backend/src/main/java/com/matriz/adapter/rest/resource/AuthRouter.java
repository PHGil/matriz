package com.matriz.adapter.rest.resource;

import com.matriz.adapter.rest.to.user.CurrentUserTO;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

/** REST endpoints of the authenticated user. */
@Path("/auth")
@Produces(MediaType.APPLICATION_JSON)
public interface AuthRouter {

    @GET
    @Path("/me")
    CurrentUserTO me();
}
