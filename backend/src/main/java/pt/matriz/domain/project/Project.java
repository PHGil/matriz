package pt.matriz.domain.project;

import java.time.OffsetDateTime;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

import pt.matriz.domain.shared.FieldValidator;
import pt.matriz.domain.shared.ValidationException;

/** A project: name 3–100 characters, optional description up to 1000 (FR-10). */
public class Project {

    static final int NAME_MIN = 3;
    static final int NAME_MAX = 100;
    static final int DESCRIPTION_MAX = 1000;

    private final UUID id;
    private String name;
    private String description;
    private final OffsetDateTime createdAt;
    private final OffsetDateTime updatedAt;

    /** Rebuilds a stored project; no validation. */
    public Project(UUID id, String name, String description, OffsetDateTime createdAt, OffsetDateTime updatedAt) {
        this.id = Objects.requireNonNull(id);
        this.name = name;
        this.description = description;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public static Project create(String name, String description) {
        FieldValidator v = new FieldValidator();
        String validName = v.requiredText("name", name, NAME_MIN, NAME_MAX, "O nome");
        String validDescription = v.optionalText("description", description, DESCRIPTION_MAX, "A descrição");
        v.throwIfInvalid();
        return new Project(UUID.randomUUID(), validName, validDescription, null, null);
    }

    /**
     * Partial change: a {@code null} Optional leaves the field unchanged; an empty description clears it.
     * At least one field must be given.
     */
    public void change(Optional<String> newName, Optional<String> newDescription) {
        if (newName == null && newDescription == null) {
            throw new ValidationException("name", "Indique pelo menos um campo a alterar.");
        }
        FieldValidator v = new FieldValidator();
        String validName = newName == null ? null
                : v.requiredText("name", newName.orElse(null), NAME_MIN, NAME_MAX, "O nome");
        String validDescription = newDescription == null ? null
                : v.optionalText("description", newDescription.orElse(null), DESCRIPTION_MAX, "A descrição");
        v.throwIfInvalid();
        if (newName != null) {
            this.name = validName;
        }
        if (newDescription != null) {
            this.description = validDescription;
        }
    }

    public UUID id() {
        return id;
    }

    public String name() {
        return name;
    }

    public String description() {
        return description;
    }

    public OffsetDateTime createdAt() {
        return createdAt;
    }

    public OffsetDateTime updatedAt() {
        return updatedAt;
    }
}
