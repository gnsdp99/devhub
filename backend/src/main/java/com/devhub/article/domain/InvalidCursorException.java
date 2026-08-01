package com.devhub.article.domain;

import com.devhub.support.domain.InvalidInputException;

public class InvalidCursorException extends InvalidInputException {

    public InvalidCursorException(String cursor, Throwable cause) {
        super("커서를 해석할 수 없습니다: " + cursor, cause);
    }
}
