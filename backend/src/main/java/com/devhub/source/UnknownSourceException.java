package com.devhub.source;

import com.devhub.support.ApiException;
import org.springframework.http.HttpStatus;

public class UnknownSourceException extends ApiException {

    public UnknownSourceException(String slug) {
        super(HttpStatus.BAD_REQUEST, "알 수 없는 소스입니다: " + slug);
    }
}
