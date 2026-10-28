package pt.matriz.shared;

/** Request conflicts with the current state (duplicate, removing the Manager...); mapped to 409. */
public class ConflictException extends RuntimeException {

    public ConflictException(String detail) {
        super(detail);
    }
}
