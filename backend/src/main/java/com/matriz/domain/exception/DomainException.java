package com.matriz.domain.exception;

/** Base type for business rule violations; the API layer maps each subtype to an HTTP status. */
public abstract class DomainException extends RuntimeException {

    protected DomainException(String message) {
        super(message);
    }
}
