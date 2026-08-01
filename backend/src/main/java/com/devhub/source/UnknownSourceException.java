package com.devhub.source;

import com.devhub.support.InvalidInputException;

public class UnknownSourceException extends InvalidInputException {

    public UnknownSourceException(String slug) {
        super("알 수 없는 소스입니다: " + slug);
    }
}
