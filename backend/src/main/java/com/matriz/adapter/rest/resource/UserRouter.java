package com.matriz.adapter.rest.resource;

import java.util.List;

import com.matriz.adapter.rest.to.user.UserDirectoryEntryTO;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;

/** Directory of active users, used by Managers to add members (US-04). */
@Path("/users")
@Produces(MediaType.APPLICATION_JSON)
public interface UserRouter {

    @GET
    List<UserDirectoryEntryTO> search(@QueryParam("q") String q);
}
