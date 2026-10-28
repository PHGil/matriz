package pt.matriz.domain.shared;

import java.util.ArrayList;
import java.util.List;

/** Collects field errors; text is trimmed first so whitespace-only values count as empty. */
public final class FieldValidator {

    private final List<ValidationException.FieldError> errors = new ArrayList<>();

    /** Returns the trimmed value; {@code label} starts the message, e.g. "O nome". */
    public String requiredText(String field, String value, int min, int max, String label) {
        String trimmed = value == null ? "" : value.strip();
        if (trimmed.isEmpty()) {
            errors.add(new ValidationException.FieldError(field, label + " é obrigatório."));
        } else if (trimmed.length() < min || trimmed.length() > max) {
            errors.add(new ValidationException.FieldError(field,
                    label + " deve ter entre " + min + " e " + max + " caracteres."));
        }
        return trimmed;
    }

    /** Returns the trimmed value, or null when absent or blank. */
    public String optionalText(String field, String value, int max, String label) {
        if (value == null || value.isBlank()) {
            return null;
        }
        String trimmed = value.strip();
        if (trimmed.length() > max) {
            errors.add(new ValidationException.FieldError(field, label + " não pode exceder " + max + " caracteres."));
        }
        return trimmed;
    }

    public void throwIfInvalid() {
        if (!errors.isEmpty()) {
            throw new ValidationException(errors);
        }
    }
}
