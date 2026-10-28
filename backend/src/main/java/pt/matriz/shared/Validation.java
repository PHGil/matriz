package pt.matriz.shared;

import java.util.ArrayList;
import java.util.List;

/** Collects field errors; text is trimmed before checking so whitespace-only values are rejected. */
public final class Validation {

    private final List<Problem.FieldError> errors = new ArrayList<>();

    /** Returns the trimmed value; {@code label} starts the message, e.g. "O nome". */
    public String requiredText(String field, String value, int min, int max, String label) {
        String trimmed = value == null ? "" : value.strip();
        if (trimmed.isEmpty()) {
            errors.add(new Problem.FieldError(field, label + " é obrigatório."));
        } else if (trimmed.length() < min || trimmed.length() > max) {
            errors.add(new Problem.FieldError(field,
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
            errors.add(new Problem.FieldError(field, label + " não pode exceder " + max + " caracteres."));
        }
        return trimmed;
    }

    public void error(String field, String message) {
        errors.add(new Problem.FieldError(field, message));
    }

    public void throwIfInvalid() {
        if (!errors.isEmpty()) {
            throw new ValidationException(errors);
        }
    }

    /** Lower-case LIKE pattern matching {@code q} anywhere; escapes \ % _ (use with {@code escape '\'}). */
    public static String containsPattern(String q) {
        String escaped = q.strip().toLowerCase()
                .replace("\\", "\\\\").replace("%", "\\%").replace("_", "\\_");
        return "%" + escaped + "%";
    }
}
