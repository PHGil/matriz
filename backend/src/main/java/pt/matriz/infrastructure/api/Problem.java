package pt.matriz.infrastructure.api;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;

/** RFC 9457 problem details body. */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record Problem(String title, int status, String detail, List<FieldError> errors) {

    public static final String MEDIA_TYPE = "application/problem+json";

    public record FieldError(String field, String message) {
    }

    public static Problem of(int status, String title) {
        return new Problem(title, status, null, null);
    }
}
