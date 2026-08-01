package com.devhub.collect.app.port.out;

import com.devhub.collect.domain.Feed;
import java.util.List;

public interface FeedRepository {

    /**
     * @return 수집 대상 피드. 소스가 비활성이면 그 소스의 피드는 제외된다.
     */
    List<Feed> findCollectible();

    void markCollected(long feedId, String etag, String lastModified);

    void markUnchanged(long feedId);

    void markFailed(long feedId);
}
