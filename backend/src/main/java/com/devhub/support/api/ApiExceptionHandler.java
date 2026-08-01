package com.devhub.support.api;

import com.devhub.support.domain.DomainException;
import com.devhub.support.domain.InvalidInputException;
import com.devhub.support.domain.NotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class ApiExceptionHandler {

    @ExceptionHandler(DomainException.class)
    ProblemDetail handle(DomainException e) {
        return ProblemDetail.forStatusAndDetail(statusOf(e), e.getMessage());
    }

    private HttpStatus statusOf(DomainException e) {
        return switch (e) {
            case InvalidInputException _ -> HttpStatus.BAD_REQUEST;
            case NotFoundException _ -> HttpStatus.NOT_FOUND;
        };
    }
}
