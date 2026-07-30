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

    private final NamedParameterJdbcTemplate jdbcTemplate;

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
