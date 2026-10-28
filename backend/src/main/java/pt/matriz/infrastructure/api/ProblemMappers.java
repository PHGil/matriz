package pt.matriz.infrastructure.api;

import org.jboss.resteasy.reactive.server.ServerExceptionMapper;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.exc.MismatchedInputException;

import io.quarkus.security.AuthenticationFailedException;
import io.quarkus.security.ForbiddenException;
import io.quarkus.security.UnauthorizedException;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.Response;
import pt.matriz.domain.shared.AccessDeniedException;
import pt.matriz.domain.shared.ConflictException;
import pt.matriz.domain.shared.NotFoundException;
import pt.matriz.domain.shared.ValidationException;

/** Maps domain, security and HTTP errors to problem+json responses (requires non-proactive auth). */
public class ProblemMappers {

    @ServerExceptionMapper({ UnauthorizedException.class, AuthenticationFailedException.class })
    public Response unauthenticated(RuntimeException e) {
        return problem(Problem.of(401, "Unauthenticated"))
                .header(HttpHeaders.WWW_AUTHENTICATE, "Bearer")
                .build();
    }

    @ServerExceptionMapper({ ForbiddenException.class, AccessDeniedException.class })
    public Response forbidden(RuntimeException e) {
        return problem(Problem.of(403, "Forbidden")).build();
    }

    @ServerExceptionMapper({ NotFoundException.class, jakarta.ws.rs.NotFoundException.class })
    public Response notFound(RuntimeException e) {
        return problem(Problem.of(404, "Not found")).build();
    }

    @ServerExceptionMapper
    public Response validation(ValidationException e) {
        var errors = e.errors().stream().map(f -> new Problem.FieldError(f.field(), f.message())).toList();
        return problem(new Problem("Validation failed", 400, null, errors)).build();
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

    private static Response.ResponseBuilder problem(Problem body) {
        return Response.status(body.status()).type(Problem.MEDIA_TYPE).entity(body);
    }
}
