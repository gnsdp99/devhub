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
            %s
             order by a.published_at desc, a.id desc
             limit :limit
            """;

    private static final String CURSOR_CONDITION =
            " where (a.published_at, a.id) < (:publishedAt, :id)";

    private final NamedParameterJdbcTemplate jdbcTemplate;
    private final JdbcClient jdbcClient;

    /**
     * @param cursor 이 커서보다 오래된 기사만 가져온다. 첫 페이지면 null
     */
    public List<Article> findPage(ArticleCursor cursor, int limit) {
        StatementSpec statement = jdbcClient
                .sql(SELECT_SQL.formatted(cursor == null ? "" : CURSOR_CONDITION))
                .param("limit", limit);
        if (cursor != null) {
            statement = statement
                    .param("publishedAt", toColumnValue(cursor.publishedAt()))
                    .param("id", cursor.id());
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
