package com.devhub.support;

public abstract non-sealed class NotFoundException extends DomainException {

    protected NotFoundException(String message) {
        super(message);
    }

    protected NotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}