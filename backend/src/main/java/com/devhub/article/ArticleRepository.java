package com.devhub.article;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.core.simple.JdbcClient;
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

    private static final String SOURCE_CONDITION = "a.source_id = :sourceId";

    private static final String CURSOR_CONDITION = "(a.published_at, a.id) < (:publishedAt, :id)";

    private final NamedParameterJdbcTemplate jdbcTemplate;
    private final JdbcClient jdbcClient;

    /**
     * @param cursor   이 커서보다 오래된 기사만 가져온다. 첫 페이지면 null
     * @param sourceId 이 소스의 기사만 가져온다. 소스를 가리지 않으면 null
     */
    public List<Article> findPage(ArticleCursor cursor, Long sourceId, int limit) {
        List<String> conditions = new ArrayList<>();
        MapSqlParameterSource params = new MapSqlParameterSource().addValue("limit", limit);

        if (sourceId != null) {
            conditions.add(SOURCE_CONDITION);
            params.addValue("sourceId", sourceId);
        }
        if (cursor != null) {
            conditions.add(CURSOR_CONDITION);
            params.addValue("publishedAt", toColumnValue(cursor.publishedAt()))
                    .addValue("id", cursor.id());
        }

        return jdbcClient.sql(SELECT_SQL + whereOf(conditions) + ORDER_AND_LIMIT_SQL)
                .paramSource(params)
                .query(Article.class)
                .list();
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

    private String whereOf(List<String> conditions) {
        if (conditions.isEmpty()) {
            return "";
        }
        return " where " + String.join("\n   and ", conditions) + "\n";
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