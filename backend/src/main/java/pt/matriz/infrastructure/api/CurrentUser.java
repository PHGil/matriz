package pt.matriz.infrastructure.api;

import java.util.UUID;

import org.eclipse.microprofile.jwt.JsonWebToken;

import io.quarkus.security.UnauthorizedException;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import pt.matriz.application.user.UserService;
import pt.matriz.domain.user.User;

/** The authenticated user of the current request, resolved from the validated access token. */
@RequestScoped
public class CurrentUser {

    @Inject
    JsonWebToken jwt;

    @Inject
    UserService userService;

    private User user;

    public User get() {
        if (user == null) {
            String sub = jwt.getSubject();
            if (sub == null) {
                throw new UnauthorizedException();
            }
            String email = firstNonBlank(jwt.getClaim("email"), jwt.getClaim("preferred_username"));
            String name = firstNonBlank(jwt.getClaim("name"), email);
            user = userService.registerLogin(UUID.fromString(sub), name, email);
        }
        return user;
    }

    public UUID id() {
        return get().id();
    }

    private static String firstNonBlank(Object first, Object second) {
        if (first != null && !first.toString().isBlank()) {
            return first.toString();
        }
        return second == null ? null : second.toString();
    }
}
