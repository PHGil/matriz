package pt.matriz.shared;

import org.jboss.resteasy.reactive.server.ServerExceptionMapper;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.exc.MismatchedInputException;

import io.quarkus.security.AuthenticationFailedException;
import io.quarkus.security.ForbiddenException;
import io.quarkus.security.UnauthorizedException;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.Response;

/** Maps security, validation and lookup failures to problem+json responses (requires non-proactive auth). */
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

    @ServerExceptionMapper
    public Response validation(ValidationException e) {
        return problem(new Problem("Validation failed", 400, null, e.errors())).build();
    }

    @ServerExceptionMapper
    public Response conflict(ConflictException e) {
        return problem(new Problem("Conflict", 409, e.getMessage(), null)).build();
    }

    @ServerExceptionMapper({ JsonProcessingException.class, MismatchedInputException.class })
    public Response invalidBody(Exception e) {
        return problem(new Problem("Invalid request body", 400, null, null)).build();
    }

    @ServerExceptionMapper
    public Response webApplication(WebApplicationException e) {
        Response.StatusType status = e.getResponse().getStatusInfo();
        return problem(Problem.of(status.getStatusCode(), status.getReasonPhrase())).build();
    }

    public static Response.ResponseBuilder problem(Problem body) {
        return Response.status(body.status()).type(Problem.MEDIA_TYPE).entity(body);
    }
}
