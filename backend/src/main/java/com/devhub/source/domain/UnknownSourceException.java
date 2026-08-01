package com.devhub.source.domain;

import com.devhub.support.domain.InvalidInputException;

public class UnknownSourceException extends InvalidInputException {

    public UnknownSourceException(String slug) {
        super("알 수 없는 소스입니다: " + slug);
    }
}
