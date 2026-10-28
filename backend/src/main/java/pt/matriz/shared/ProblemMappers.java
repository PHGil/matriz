package pt.matriz.shared;

import org.jboss.resteasy.reactive.server.ServerExceptionMapper;

import io.quarkus.security.AuthenticationFailedException;
import io.quarkus.security.ForbiddenException;
import io.quarkus.security.UnauthorizedException;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.Response;

/** Maps security and lookup failures to problem+json responses (requires non-proactive auth). */
public class ProblemMappers {

    @ServerExceptionMapper({ UnauthorizedException.class, AuthenticationFailedException.class })
    public Response unauthenticated(RuntimeException e) {
        return problem(Problem.of(401, "Unauthenticated"))
                .header(HttpHeaders.WWW_AUTHENTICATE, "Bearer")
                .build();
    }

    @ServerExceptionMapper
    public Response forbidden(ForbiddenException e) {
        return problem(Problem.of(403, "Forbidden")).build();
    }

    @ServerExceptionMapper
    public Response notFound(NotFoundException e) {
        return problem(Problem.of(404, "Not found")).build();
    }

    public static Response.ResponseBuilder problem(Problem body) {
        return Response.status(body.status()).type(Problem.MEDIA_TYPE).entity(body);
    }
}
