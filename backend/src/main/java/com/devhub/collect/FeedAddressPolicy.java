package com.devhub.collect;

import java.net.URI;

/**
 * 피드를 가져오기 전에 대상 주소를 검사한다. 리다이렉트할 때마다 다음 주소로 다시 호출된다.
 */
public interface FeedAddressPolicy {

    /**
     * @throws FeedFetchException 요청해서는 안 되는 주소면
     */
    void check(URI uri);
}