package pt.matriz.auth;

import java.util.UUID;

import io.quarkus.security.Authenticated;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import pt.matriz.users.AppUser;

@Path("/auth")
@Authenticated
@Produces(MediaType.APPLICATION_JSON)
public class AuthResource {

    public record CurrentUserDto(UUID id, String name, String email) {
    }

    @Inject
    CurrentUser currentUser;

    @GET
    @Path("/me")
    public CurrentUserDto me() {
        AppUser user = currentUser.get();
        return new CurrentUserDto(user.id, user.name, user.email);
    }
}
