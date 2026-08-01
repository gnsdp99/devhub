package com.devhub.support;

public abstract non-sealed class InvalidInputException extends DomainException {

    protected InvalidInputException(String message) {
        super(message);
    }

    protected InvalidInputException(String message, Throwable cause) {
        super(message, cause);
    }
}