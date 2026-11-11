package com.matriz.domain.exception;

/** The user can see the resource but lacks the role for this operation. */
public class AccessDeniedException extends DomainException {

    public AccessDeniedException() {
        super("Access denied");
    }
}
