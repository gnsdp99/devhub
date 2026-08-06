package com.devhub.source.api;

import static org.assertj.core.api.Assertions.assertThat;

import com.devhub.support.AbstractApiIntegrationTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class SourceApiIntegrationTest extends AbstractApiIntegrationTest {

    @Test
    @DisplayName("활성 소스를 이름 알파벳순으로 돌려준다")
    void returnsEveryEnabledSourceOrderedByName() {
        assertThat(mvc.get().uri("/api/sources"))
                .hasStatusOk()
                .bodyJson()
                .extractingPath("$[*].name")
                .asArray()
                .startsWith("AWS", "ByteByteGo", "Cloudflare")
                .endsWith("요즘IT");
    }

    @Test
    @DisplayName("비활성 소스는 돌려주지 않는다")
    void leavesOutDisabledSources() {
        assertThat(mvc.get().uri("/api/sources"))
                .hasStatusOk()
                .bodyJson()
                .extractingPath("$[*].slug")
                .asArray()
                .contains("hackernews")
                .doesNotContain("anthropic");
    }

    @Test
    @DisplayName("소스마다 slug, 이름, 사이트 주소를 담아 돌려준다")
    void returnsTheFieldsOfEachSource() {
        assertThat(mvc.get().uri("/api/sources"))
                .hasStatusOk()
                .bodyJson()
                .hasPathSatisfying(
                        "$[?(@.slug == 'hackernews')].name",
                        it -> assertThat(it).asArray().containsExactly("Hacker News"))
                .hasPathSatisfying(
                        "$[?(@.slug == 'hackernews')].siteUrl",
                        it -> assertThat(it).asArray().containsExactly("https://news.ycombinator.com"));
    }
}