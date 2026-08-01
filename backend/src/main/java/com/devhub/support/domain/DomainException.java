package com.devhub.support.domain;

public abstract sealed class DomainException extends RuntimeException
        permits InvalidInputException, NotFoundException {

    protected DomainException(String message) {
        super(message);
    }

    protected DomainException(String message, Throwable cause) {
        super(message, cause);
    }
}
