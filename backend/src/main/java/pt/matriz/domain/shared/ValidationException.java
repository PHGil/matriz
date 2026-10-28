package pt.matriz.domain.shared;

import java.util.List;

/** One or more fields break a business rule. */
public class ValidationException extends DomainException {

    public record FieldError(String field, String message) {
    }

    private final transient List<FieldError> errors;

    public ValidationException(List<FieldError> errors) {
        super("Validation failed");
        this.errors = List.copyOf(errors);
    }

    public ValidationException(String field, String message) {
        this(List.of(new FieldError(field, message)));
    }

    public List<FieldError> errors() {
        return errors;
    }
}
