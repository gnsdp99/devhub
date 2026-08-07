package com.devhub.source.api;

import static org.assertj.core.api.Assertions.assertThat;

import com.devhub.support.AbstractApiIntegrationTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.transaction.annotation.Transactional;

@Transactional
class SourceApiIntegrationTest extends AbstractApiIntegrationTest {

    @Test
    @DisplayName("활성 소스를 이름 알파벳순으로 돌려준다")
    void returnsEveryEnabledSourceOrderedByName() {
        assertThat(mvc.get().uri("/api/sources"))
                .hasStatusOk()
                .bodyJson()
                .extractingPath("$[*].name")
                .asArray()
                .startsWith(".NET", "AWS", "Agoda")
                .endsWith("한글과컴퓨터");
    }

    @Test
    @DisplayName("비활성 소스는 돌려주지 않는다")
    void leavesOutDisabledSources() {
        update("update source set enabled = false where slug = 'redis'");

        assertThat(mvc.get().uri("/api/sources"))
                .hasStatusOk()
                .bodyJson()
                .extractingPath("$[*].slug")
                .asArray()
                .contains("cloudflare")
                .doesNotContain("redis");
    }

    @Test
    @DisplayName("소스마다 slug, 이름, 사이트 주소, 로고 주소를 담아 돌려준다")
    void returnsTheFieldsOfEachSource() {
        assertThat(mvc.get().uri("/api/sources"))
                .hasStatusOk()
                .bodyJson()
                .hasPathSatisfying(
                        "$[?(@.slug == 'cloudflare')].name",
                        it -> assertThat(it).asArray().containsExactly("Cloudflare"))
                .hasPathSatisfying(
                        "$[?(@.slug == 'cloudflare')].siteUrl",
                        it -> assertThat(it).asArray().containsExactly("https://blog.cloudflare.com"))
                .hasPathSatisfying(
                        "$[?(@.slug == 'cloudflare')].logoUrl",
                        it -> assertThat(it).asArray().containsExactly("/logos/cloudflare.svg"));
    }
}