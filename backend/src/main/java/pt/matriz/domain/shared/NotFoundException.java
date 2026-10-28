package pt.matriz.domain.shared;

/** The resource does not exist or is not visible to the user (EC-05: both look the same). */
public class NotFoundException extends DomainException {

    public NotFoundException() {
        super("Not found");
    }
}
