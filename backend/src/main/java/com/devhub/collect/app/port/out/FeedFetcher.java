package com.devhub.collect.app.port.out;

import com.devhub.collect.domain.FeedFetchException;
import com.devhub.collect.domain.FetchResult;

public interface FeedFetcher {

    /**
     * @throws FeedFetchException 피드를 가져오지 못하면
     */
    FetchResult fetch(String feedUrl, String etag, String lastModified);
}
