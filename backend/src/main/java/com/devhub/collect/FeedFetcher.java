package com.devhub.collect;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.time.Duration;
import java.util.zip.GZIPInputStream;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Slf4j
@Component
public class FeedFetcher {

    private static final String USER_AGENT =
            "devhub-feed-collector/1.0 (+https://github.com/gnsdp99/devhub)";
    private static final Duration CONNECT_TIMEOUT = Duration.ofSeconds(5);
    private static final Duration READ_TIMEOUT = Duration.ofSeconds(10);
    private static final int MAX_BODY_BYTES = 10 * 1024 * 1024;

    private final RestClient restClient;

    public FeedFetcher(RestClient.Builder builder) {
        HttpClient httpClient = HttpClient.newBuilder()
                .connectTimeout(CONNECT_TIMEOUT)
                .followRedirects(HttpClient.Redirect.NORMAL)
                .build();
        JdkClientHttpRequestFactory requestFactory = new JdkClientHttpRequestFactory(httpClient);
        requestFactory.setReadTimeout(READ_TIMEOUT);
        this.restClient = builder.requestFactory(requestFactory).build();
    }

    public FetchResult fetch(String feedUrl, String etag, String lastModified) {
        return restClient.get()
                .uri(URI.create(feedUrl))
                .header(HttpHeaders.USER_AGENT, USER_AGENT)
                .header(HttpHeaders.ACCEPT_ENCODING, "gzip")
                .headers(headers -> {
                    if (etag != null) {
                        headers.set(HttpHeaders.IF_NONE_MATCH, etag);
                    }
                    if (lastModified != null) {
                        headers.set(HttpHeaders.IF_MODIFIED_SINCE, lastModified);
                    }
                })
                .exchange((request, response) -> toResult(feedUrl, response));
    }

    private FetchResult toResult(String feedUrl, ClientHttpResponse response) throws IOException {
        HttpStatus status = HttpStatus.resolve(response.getStatusCode().value());
        if (status == HttpStatus.NOT_MODIFIED) {
            log.debug("변경되지 않았습니다. url={}", feedUrl);
            return new FetchResult.NotModified();
        }
        if (status == null || !status.is2xxSuccessful()) {
            throw new FeedFetchException("피드 응답이 " + response.getStatusCode() + "입니다: " + feedUrl);
        }

        HttpHeaders headers = response.getHeaders();
        byte[] body = readBody(feedUrl, response);
        return new FetchResult.Fetched(
                body, headers.getETag(), headers.getFirst(HttpHeaders.LAST_MODIFIED));
    }

    private byte[] readBody(String feedUrl, ClientHttpResponse response) throws IOException {
        boolean gzipped = "gzip".equalsIgnoreCase(
                response.getHeaders().getFirst(HttpHeaders.CONTENT_ENCODING));
        try (InputStream body = response.getBody();
                InputStream stream = gzipped ? new GZIPInputStream(body) : body) {
            byte[] read = stream.readNBytes(MAX_BODY_BYTES + 1);
            if (read.length > MAX_BODY_BYTES) {
                throw new FeedFetchException(
                        "피드 본문이 " + MAX_BODY_BYTES + "바이트를 넘습니다: " + feedUrl);
            }
            return read;
        }
    }
}