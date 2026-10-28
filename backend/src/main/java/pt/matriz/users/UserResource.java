package pt.matriz.users;

import java.util.List;
import java.util.UUID;

import io.quarkus.panache.common.Page;
import io.quarkus.panache.common.Sort;
import io.quarkus.security.Authenticated;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import pt.matriz.shared.Validation;

/** Directory of active users, used by Managers to add members (US-04). */
@Path("/users")
@Authenticated
@Produces(MediaType.APPLICATION_JSON)
public class UserResource {

    private static final int MAX_RESULTS = 20;

    public record UserDirectoryEntry(UUID id, String name, String email) {
    }

    @GET
    public List<UserDirectoryEntry> search(@QueryParam("q") String q) {
        var query = (q == null || q.isBlank())
                ? AppUser.<AppUser>find("active = true", Sort.by("name"))
                : AppUser.<AppUser>find(
                        "active = true and (lower(name) like ?1 escape '\\' or lower(email) like ?1 escape '\\')",
                        Sort.by("name"), Validation.containsPattern(q));
        return query.page(Page.ofSize(MAX_RESULTS)).list().stream()
                .map(u -> new UserDirectoryEntry(u.id, u.name, u.email))
                .toList();
    }
}
