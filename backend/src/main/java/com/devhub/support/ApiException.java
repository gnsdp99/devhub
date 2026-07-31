package com.devhub.support;

import org.springframework.http.HttpStatus;

public abstract class ApiException extends RuntimeException {

    private final HttpStatus status;

    protected ApiException(HttpStatus status, String detail) {
        this(status, detail, null);
    }

    protected ApiException(HttpStatus status, String detail, Throwable cause) {
        super(detail, cause);
        this.status = status;
    }

    public HttpStatus status() {
        return status;
    }
}
