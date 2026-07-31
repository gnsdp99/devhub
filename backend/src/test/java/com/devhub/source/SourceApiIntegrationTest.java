package com.devhub.source;

import static org.assertj.core.api.Assertions.assertThat;

import com.devhub.support.AbstractIntegrationTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.assertj.MockMvcTester;

@AutoConfigureMockMvc
class SourceApiIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    private MockMvcTester mvc;

    @BeforeEach
    void setUp() {
        mvc = MockMvcTester.create(mockMvc);
    }

    @Test
    @DisplayName("활성 소스를 이름 알파벳순으로 돌려준다")
    void returnsEveryEnabledSourceOrderedByName() {
        assertThat(mvc.get().uri("/api/sources"))
                .hasStatusOk()
                .bodyJson()
                .extractingPath("$[*].name")
                .asArray()
                .hasSize(20)
                .startsWith("AWS", "ByteByteGo", "Cloudflare")
                .endsWith("요즘IT");
    }

    @Test
    @DisplayName("소스마다 slug, 이름, 카테고리, 사이트 주소를 담아 돌려준다")
    void returnsTheFieldsOfEachSource() {
        assertThat(mvc.get().uri("/api/sources"))
                .hasStatusOk()
                .bodyJson()
                .hasPathSatisfying(
                        "$[?(@.slug == 'hackernews')].name",
                        it -> assertThat(it).asArray().containsExactly("Hacker News"))
                .hasPathSatisfying(
                        "$[?(@.slug == 'hackernews')].category",
                        it -> assertThat(it).asArray().containsExactly("NEWS"))
                .hasPathSatisfying(
                        "$[?(@.slug == 'hackernews')].siteUrl",
                        it -> assertThat(it).asArray().containsExactly("https://news.ycombinator.com"));
    }
}