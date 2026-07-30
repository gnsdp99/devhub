package com.devhub.collect;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class FeedRepository {

    private final JdbcClient jdbcClient;

    /**
     * @return 수집 대상 피드. 소스가 비활성이면 그 소스의 피드는 제외된다.
     */
    public List<Feed> findCollectible() {
        return jdbcClient.sql("""
                        select f.id, f.slug, f.feed_url, f.etag, f.last_modified
                          from feed f
                          join source s on s.id = f.source_id
                         where f.enabled and s.enabled
                         order by f.id
                        """)
                .query(Feed.class)
                .list();
    }

    public void markCollected(long feedId, String etag, String lastModified) {
        jdbcClient.sql("""
                        update feed
                           set etag = :etag,
                               last_modified = :lastModified,
                               last_fetched_at = now(),
                               last_success_at = now(),
                               consecutive_failures = 0,
                               updated_at = now()
                         where id = :id
                        """)
                .param("id", feedId)
                .param("etag", etag)
                .param("lastModified", lastModified)
                .update();
    }

    public void markUnchanged(long feedId) {
        jdbcClient.sql("""
                        update feed
                           set last_fetched_at = now(),
                               last_success_at = now(),
                               consecutive_failures = 0,
                               updated_at = now()
                         where id = :id
                        """)
                .param("id", feedId)
                .update();
    }

    public void markFailed(long feedId) {
        jdbcClient.sql("""
                        update feed
                           set last_fetched_at = now(),
                               consecutive_failures = consecutive_failures + 1,
                               updated_at = now()
                         where id = :id
                        """)
                .param("id", feedId)
                .update();
    }
}