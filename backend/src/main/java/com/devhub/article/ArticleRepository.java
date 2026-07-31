package com.devhub.article;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.jdbc.core.simple.JdbcClient.StatementSpec;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class ArticleRepository {

    private static final String INSERT_SQL = """
            insert into article (feed_id, source_id, guid, url, url_hash, title, summary, author, published_at)
            select f.id, f.source_id, :guid, :url, :urlHash, :title, :summary, :author, :publishedAt
              from feed f
             where f.id = :feedId
            on conflict (url_hash) do nothing
            """;

    private static final String SELECT_SQL = """
            select a.id, a.title, a.url, a.summary, a.published_at,
                   f.slug as feed, s.slug as source, s.name as source_name
              from article a
              join feed f on f.id = a.feed_id
              join source s on s.id = a.source_id
            """;

    private static final String ORDER_AND_LIMIT_SQL = """
             order by a.published_at desc, a.id desc
             limit :limit
            """;

    private static final String CURSOR_WHERE_SQL = """
             where (a.published_at, a.id) < (:publishedAt, :id)
            """;

    private static final String SOURCE_WHERE_SQL = """
             where a.source_id = :sourceId
            """;

    private static final String SOURCE_AND_CURSOR_WHERE_SQL = """
             where a.source_id = :sourceId
               and (a.published_at, a.id) < (:publishedAt, :id)
            """;

    private static final String FIRST_PAGE_SQL = SELECT_SQL + ORDER_AND_LIMIT_SQL;

    private static final String NEXT_PAGE_SQL = SELECT_SQL + CURSOR_WHERE_SQL + ORDER_AND_LIMIT_SQL;

    private static final String FIRST_PAGE_BY_SOURCE_SQL =
            SELECT_SQL + SOURCE_WHERE_SQL + ORDER_AND_LIMIT_SQL;

    private static final String NEXT_PAGE_BY_SOURCE_SQL =
            SELECT_SQL + SOURCE_AND_CURSOR_WHERE_SQL + ORDER_AND_LIMIT_SQL;

    private final NamedParameterJdbcTemplate jdbcTemplate;
    private final JdbcClient jdbcClient;

    /**
     * @param cursor   이 커서보다 오래된 기사만 가져온다. 첫 페이지면 null
     * @param sourceId 이 소스의 기사만 가져온다. 소스를 가리지 않으면 null
     */
    public List<Article> findPage(ArticleCursor cursor, Long sourceId, int limit) {
        StatementSpec statement = jdbcClient
                .sql(pageSqlOf(cursor, sourceId))
                .param("limit", limit);
        if (cursor != null) {
            statement = statement
                    .param("publishedAt", toColumnValue(cursor.publishedAt()))
                    .param("id", cursor.id());
        }
        if (sourceId != null) {
            statement = statement.param("sourceId", sourceId);
        }
        return statement.query(Article.class).list();
    }

    /**
     * @return 새로 저장한 건수. 이미 있던 기사는 세지 않는다.
     */
    public int insertNew(List<NewArticle> articles) {
        if (articles.isEmpty()) {
            return 0;
        }
        SqlParameterSource[] params = articles.stream()
                .map(this::paramsOf)
                .toArray(SqlParameterSource[]::new);
        return Arrays.stream(jdbcTemplate.batchUpdate(INSERT_SQL, params)).sum();
    }

    private String pageSqlOf(ArticleCursor cursor, Long sourceId) {
        if (sourceId == null) {
            return cursor == null ? FIRST_PAGE_SQL : NEXT_PAGE_SQL;
        }
        return cursor == null ? FIRST_PAGE_BY_SOURCE_SQL : NEXT_PAGE_BY_SOURCE_SQL;
    }

    private SqlParameterSource paramsOf(NewArticle article) {
        return new MapSqlParameterSource()
                .addValue("feedId", article.feedId())
                .addValue("guid", article.guid())
                .addValue("url", article.url())
                .addValue("urlHash", article.urlHash())
                .addValue("title", article.title())
                .addValue("summary", article.summary())
                .addValue("author", article.author())
                .addValue("publishedAt", toColumnValue(article.publishedAt()));
    }

    private OffsetDateTime toColumnValue(Instant publishedAt) {
        return OffsetDateTime.ofInstant(publishedAt.truncatedTo(ChronoUnit.MILLIS), ZoneOffset.UTC);
    }
}