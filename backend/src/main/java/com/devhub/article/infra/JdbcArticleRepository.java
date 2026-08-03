package com.devhub.article.infra;

import com.devhub.article.app.port.out.ArticleRepository;
import com.devhub.article.domain.Article;
import com.devhub.article.domain.ArticleCursor;
import com.devhub.article.domain.ArticleSource;
import com.devhub.article.domain.NewArticle;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class JdbcArticleRepository implements ArticleRepository {

    private static final String INSERT_SQL = """
            with upserted as (
                insert into article (url, url_hash, title, summary, author, published_at)
                values (:url, :urlHash, :title, :summary, :author, :publishedAt)
                on conflict (url_hash) do nothing
                returning id
            ), resolved as (
                select id from upserted
                union all
                select id from article
                 where url_hash = :urlHash
                   and not exists (select 1 from upserted)
            )
            insert into article_source (article_id, source_id, feed_id, guid, published_at)
            select r.id, f.source_id, f.id, :guid, :publishedAt
              from resolved r, feed f
             where f.id = :feedId
            on conflict (article_id, source_id) do nothing
            """;

    private static final String SELECT_GLOBAL_SQL = """
            select a.id, a.title, a.url, a.summary, a.author, a.published_at
              from article a
            """;

    private static final String SELECT_BY_SOURCE_SQL = """
            select a.id, a.title, a.url, a.summary, a.author, asrc.published_at
              from article_source asrc
              join article a on a.id = asrc.article_id
            """;

    private static final String GLOBAL_ORDER_AND_LIMIT_SQL = """
             order by a.published_at desc, a.id desc
             limit :limit
            """;

    private static final String BY_SOURCE_ORDER_AND_LIMIT_SQL = """
             order by asrc.published_at desc, asrc.article_id desc
             limit :limit
            """;

    private static final String GLOBAL_CURSOR_CONDITION =
            "(a.published_at, a.id) < (:publishedAt, :id)";

    private static final String BY_SOURCE_CONDITION = "asrc.source_id = :sourceId";

    private static final String BY_SOURCE_CURSOR_CONDITION =
            "(asrc.published_at, asrc.article_id) < (:publishedAt, :id)";

    private static final String SOURCES_SQL = """
            select asrc.article_id, f.slug as feed, s.slug as source, s.name as source_name
              from article_source asrc
              join feed f on f.id = asrc.feed_id
              join source s on s.id = asrc.source_id
             where asrc.article_id in (:articleIds)
             order by asrc.article_id, s.name
            """;

    private final NamedParameterJdbcTemplate jdbcTemplate;
    private final JdbcClient jdbcClient;

    /**
     * @param cursor   이 커서보다 오래된 기사만 가져온다. 첫 페이지면 null
     * @param sourceId 이 소스의 기사만 가져온다. 소스를 가리지 않으면 null
     */
    @Override
    public List<Article> findPage(ArticleCursor cursor, Long sourceId, int limit) {
        List<ArticleRow> rows = sourceId == null
                ? findGlobalRows(cursor, limit)
                : findRowsBySource(cursor, sourceId, limit);
        return withSources(rows);
    }

    /**
     * @return 새로 저장한 건수. 그 소스가 이미 가지고 있던 기사는 세지 않는다.
     */
    @Override
    public int insertNew(List<NewArticle> articles) {
        if (articles.isEmpty()) {
            return 0;
        }
        SqlParameterSource[] params = articles.stream()
                .map(this::paramsOf)
                .toArray(SqlParameterSource[]::new);
        return Arrays.stream(jdbcTemplate.batchUpdate(INSERT_SQL, params)).sum();
    }

    private List<ArticleRow> findGlobalRows(ArticleCursor cursor, int limit) {
        List<String> conditions = new ArrayList<>();
        MapSqlParameterSource params = new MapSqlParameterSource().addValue("limit", limit);
        if (cursor != null) {
            conditions.add(GLOBAL_CURSOR_CONDITION);
            addCursor(params, cursor);
        }
        return query(SELECT_GLOBAL_SQL, conditions, GLOBAL_ORDER_AND_LIMIT_SQL, params);
    }

    private List<ArticleRow> findRowsBySource(ArticleCursor cursor, long sourceId, int limit) {
        List<String> conditions = new ArrayList<>();
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("limit", limit)
                .addValue("sourceId", sourceId);
        conditions.add(BY_SOURCE_CONDITION);
        if (cursor != null) {
            conditions.add(BY_SOURCE_CURSOR_CONDITION);
            addCursor(params, cursor);
        }
        return query(SELECT_BY_SOURCE_SQL, conditions, BY_SOURCE_ORDER_AND_LIMIT_SQL, params);
    }

    private List<ArticleRow> query(
            String select, List<String> conditions, String orderAndLimit, SqlParameterSource params) {
        return jdbcClient.sql(select + whereOf(conditions) + orderAndLimit)
                .paramSource(params)
                .query(ArticleRow.class)
                .list();
    }

    private List<Article> withSources(List<ArticleRow> rows) {
        if (rows.isEmpty()) {
            return List.of();
        }
        Map<Long, List<ArticleSource>> sources = sourcesOf(rows.stream().map(ArticleRow::id).toList());
        return rows.stream()
                .map(row -> new Article(
                        row.id(),
                        row.title(),
                        row.url(),
                        row.summary(),
                        row.author(),
                        row.publishedAt(),
                        sources.getOrDefault(row.id(), List.of())))
                .toList();
    }

    private Map<Long, List<ArticleSource>> sourcesOf(List<Long> articleIds) {
        return jdbcClient.sql(SOURCES_SQL)
                .param("articleIds", articleIds)
                .query(SourceRow.class)
                .list()
                .stream()
                .collect(Collectors.groupingBy(
                        SourceRow::articleId,
                        Collectors.mapping(SourceRow::toArticleSource, Collectors.toList())));
    }

    private void addCursor(MapSqlParameterSource params, ArticleCursor cursor) {
        params.addValue("publishedAt", toColumnValue(cursor.publishedAt()))
                .addValue("id", cursor.id());
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

    private record ArticleRow(
            long id, String title, String url, String summary, String author, Instant publishedAt) {
    }

    private record SourceRow(long articleId, String feed, String source, String sourceName) {

        ArticleSource toArticleSource() {
            return new ArticleSource(feed, source, sourceName);
        }
    }
}