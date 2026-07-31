package com.devhub.source;

public class UnknownSourceException extends RuntimeException {

    public UnknownSourceException(String slug) {
        super("알 수 없는 소스입니다: " + slug);
    }
}