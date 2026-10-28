package pt.matriz.infrastructure.api;

import java.util.List;
import java.util.UUID;

import io.quarkus.security.Authenticated;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import pt.matriz.application.user.UserService;

/** Directory of active users, used by Managers to add members (US-04). */
@Path("/users")
@Authenticated
@Produces(MediaType.APPLICATION_JSON)
public class UserResource {

    public record UserDirectoryEntry(UUID id, String name, String email) {
    }

    @Inject
    UserService users;

    @GET
    public List<UserDirectoryEntry> search(@QueryParam("q") String q) {
        return users.searchActive(q).stream()
                .map(u -> new UserDirectoryEntry(u.id(), u.name(), u.email()))
                .toList();
    }
}
