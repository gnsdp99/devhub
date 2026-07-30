package com.devhub;

import static org.assertj.core.api.Assertions.assertThat;

import com.devhub.support.AbstractIntegrationTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class SourceSeedIntegrationTest extends AbstractIntegrationTest {

    @Test
    @DisplayName("시드가 큐레이션한 소스 21개와 피드 26개를 모두 넣는다")
    void seedInsertsEveryCuratedSourceAndFeed() {
        long sources = count("select count(*) from source");
        long feeds = count("select count(*) from feed");

        assertThat(sources).isEqualTo(21);
        assertThat(feeds).isEqualTo(26);
    }

    @Test
    @DisplayName("피드가 여러 개인 소스는 자기 피드를 전부 갖는다")
    void sourcesWithMultipleFeedsGetAllOfThem() {
        assertThat(feedCountOf("google")).isEqualTo(5);
        assertThat(feedCountOf("aws")).isEqualTo(2);
        assertThat(feedCountOf("github")).isEqualTo(2);
    }

    @Test
    @DisplayName("Anthropic은 공식 피드가 없으므로 비활성 소스로만 들어가고 피드는 없다")
    void anthropicIsSeededDisabledAndHasNoFeed() {
        boolean enabled = jdbcClient.sql("select enabled from source where slug = 'anthropic'")
                .query(Boolean.class)
                .single();

        assertThat(enabled).isFalse();
        assertThat(feedCountOf("anthropic")).isZero();
    }

    @Test
    @DisplayName("모든 피드 URL이 https를 쓴다")
    void everyFeedUrlIsHttps() {
        long nonHttps = count("select count(*) from feed where feed_url not like 'https://%'");

        assertThat(nonHttps).isZero();
    }

    private long feedCountOf(String sourceSlug) {
        return jdbcClient.sql("""
                        select count(*)
                          from feed f
                          join source s on s.id = f.source_id
                         where s.slug = :slug
                        """)
                .param("slug", sourceSlug)
                .query(Long.class)
                .single();
    }
}