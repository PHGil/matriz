package pt.matriz.domain.shared;

/** The operation conflicts with the current state (duplicate, removing the Manager...). */
public class ConflictException extends DomainException {

    public ConflictException(String message) {
        super(message);
    }
}
