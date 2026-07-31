package com.devhub.article;

import com.devhub.support.ApiException;
import org.springframework.http.HttpStatus;

public class InvalidCursorException extends ApiException {

    public InvalidCursorException(String cursor, Throwable cause) {
        super(HttpStatus.BAD_REQUEST, "커서를 해석할 수 없습니다: " + cursor, cause);
    }
}
