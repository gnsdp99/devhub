package com.devhub.collect;

import static com.devhub.support.StubHttpServer.respond;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.devhub.support.StubHttpServer;
import com.sun.net.httpserver.Headers;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.atomic.AtomicReference;
import java.util.zip.GZIPOutputStream;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.web.client.RestClient;

class FeedFetcherTest {

    private static final String FEED_BODY = "<rss version=\"2.0\"><channel/></rss>";

    private final FeedFetcher fetcher = new FeedFetcher(RestClient.builder());

    private StubHttpServer server;

    @BeforeEach
    void startServer() throws IOException {
        server = StubHttpServer.start();
    }

    @AfterEach
    void stopServer() {
        server.stop();
    }

    @Test
    @DisplayName("200이면 본문과 조건부 요청 헤더를 함께 돌려준다")
    void returnsBodyWithConditionalHeaders() {
        String url = server.serve("/feed", exchange -> {
            exchange.getResponseHeaders().set("ETag", "\"v1\"");
            exchange.getResponseHeaders().set("Last-Modified", "Wed, 29 Jul 2026 07:18:24 GMT");
            respond(exchange, 200, utf8(FEED_BODY));
        });

        FetchResult result = fetcher.fetch(url, null, null);

        assertThat(result).isInstanceOfSatisfying(FetchResult.Fetched.class, fetched -> {
            assertThat(fetched.body()).asString(StandardCharsets.UTF_8).isEqualTo(FEED_BODY);
            assertThat(fetched.etag()).isEqualTo("\"v1\"");
            assertThat(fetched.lastModified()).isEqualTo("Wed, 29 Jul 2026 07:18:24 GMT");
        });
    }

    @Test
    @DisplayName("304면 본문을 읽지 않고 NotModified를 돌려준다")
    void returnsNotModified() {
        String url = server.serve("/feed", exchange -> respond(exchange, 304, new byte[0]));

        assertThat(fetcher.fetch(url, "\"v1\"", null))
                .isInstanceOf(FetchResult.NotModified.class);
    }

    @Test
    @DisplayName("저장해 둔 ETag와 Last-Modified를 조건부 요청으로 실어 보낸다")
    void sendsConditionalRequestHeaders() {
        AtomicReference<Headers> sent = new AtomicReference<>();
        String url = server.serve("/feed", exchange -> {
            sent.set(exchange.getRequestHeaders());
            respond(exchange, 304, new byte[0]);
        });

        fetcher.fetch(url, "\"v1\"", "Wed, 29 Jul 2026 07:18:24 GMT");

        assertThat(sent.get().getFirst("If-None-Match")).isEqualTo("\"v1\"");
        assertThat(sent.get().getFirst("If-Modified-Since"))
                .isEqualTo("Wed, 29 Jul 2026 07:18:24 GMT");
    }

    @Test
    @DisplayName("저장해 둔 값이 없으면 조건부 요청 헤더를 보내지 않는다")
    void omitsConditionalHeadersWhenUnknown() {
        AtomicReference<Headers> sent = new AtomicReference<>();
        String url = server.serve("/feed", exchange -> {
            sent.set(exchange.getRequestHeaders());
            respond(exchange, 200, utf8(FEED_BODY));
        });

        fetcher.fetch(url, null, null);

        assertThat(sent.get().getFirst("If-None-Match")).isNull();
        assertThat(sent.get().getFirst("If-Modified-Since")).isNull();
    }

    @Test
    @DisplayName("User-Agent로 수집기를 밝힌다")
    void sendsUserAgent() {
        AtomicReference<String> sent = new AtomicReference<>();
        String url = server.serve("/feed", exchange -> {
            sent.set(exchange.getRequestHeaders().getFirst("User-Agent"));
            respond(exchange, 200, utf8(FEED_BODY));
        });

        fetcher.fetch(url, null, null);

        assertThat(sent.get()).startsWith("devhub-feed-collector/");
    }

    @Test
    @DisplayName("301을 따라가 최종 URL의 본문을 가져온다")
    void followsRedirect() {
        String target = server.serve("/moved", exchange -> respond(exchange, 200, utf8(FEED_BODY)));
        String url = server.serve("/feed", exchange -> {
            exchange.getResponseHeaders().set("Location", target);
            respond(exchange, 301, new byte[0]);
        });

        assertThat(fetcher.fetch(url, null, null))
                .isInstanceOfSatisfying(FetchResult.Fetched.class, fetched ->
                        assertThat(fetched.body())
                                .asString(StandardCharsets.UTF_8)
                                .isEqualTo(FEED_BODY));
    }

    @Test
    @DisplayName("gzip으로 온 본문을 풀어서 돌려준다")
    void decompressesGzip() {
        String url = server.serve("/feed", exchange -> {
            exchange.getResponseHeaders().set("Content-Encoding", "gzip");
            respond(exchange, 200, gzip(utf8(FEED_BODY)));
        });

        assertThat(fetcher.fetch(url, null, null))
                .isInstanceOfSatisfying(FetchResult.Fetched.class, fetched ->
                        assertThat(fetched.body())
                                .asString(StandardCharsets.UTF_8)
                                .isEqualTo(FEED_BODY));
    }

    @Test
    @DisplayName("5xx면 FeedFetchException을 던진다")
    void failsOnServerError() {
        String url = server.serve("/feed", exchange -> respond(exchange, 500, utf8("boom")));

        assertThatThrownBy(() -> fetcher.fetch(url, null, null))
                .isInstanceOf(FeedFetchException.class)
                .hasMessageContaining("500");
    }

    @Test
    @DisplayName("압축을 푼 본문이 상한을 넘으면 FeedFetchException을 던진다")
    void failsOnOversizedBody() {
        String url = server.serve("/feed", exchange -> {
            exchange.getResponseHeaders().set("Content-Encoding", "gzip");
            respond(exchange, 200, gzip(new byte[11 * 1024 * 1024]));
        });

        assertThatThrownBy(() -> fetcher.fetch(url, null, null))
                .isInstanceOf(FeedFetchException.class)
                .hasMessageContaining("바이트");
    }

    private static byte[] utf8(String text) {
        return text.getBytes(StandardCharsets.UTF_8);
    }

    private static byte[] gzip(byte[] raw) throws IOException {
        ByteArrayOutputStream compressed = new ByteArrayOutputStream();
        try (GZIPOutputStream gzip = new GZIPOutputStream(compressed)) {
            gzip.write(raw);
        }
        return compressed.toByteArray();
    }
}