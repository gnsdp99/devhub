package com.devhub.article;

public class InvalidCursorException extends RuntimeException {

    public InvalidCursorException(String cursor, Throwable cause) {
        super("커서를 해석할 수 없습니다: " + cursor, cause);
    }
}
