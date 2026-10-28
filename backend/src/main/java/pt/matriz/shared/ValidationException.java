package pt.matriz.shared;

import java.util.List;

/** Input rejected by business validation; mapped to 400 with field errors. */
public class ValidationException extends RuntimeException {

    private final transient List<Problem.FieldError> errors;

    public ValidationException(List<Problem.FieldError> errors) {
        super("Validation failed");
        this.errors = List.copyOf(errors);
    }

    public ValidationException(String field, String message) {
        this(List.of(new Problem.FieldError(field, message)));
    }

    public List<Problem.FieldError> errors() {
        return errors;
    }
}
