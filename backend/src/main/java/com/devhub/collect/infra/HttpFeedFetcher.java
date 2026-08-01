package com.devhub.collect.infra;

import com.devhub.collect.app.port.out.FeedFetcher;
import com.devhub.collect.domain.FeedAddressPolicy;
import com.devhub.collect.domain.FeedFetchException;
import com.devhub.collect.domain.FetchResult;
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
import org.springframework.web.client.RestClientException;

@Slf4j
@Component
public class HttpFeedFetcher implements FeedFetcher {

    private static final String USER_AGENT =
            "devhub-feed-collector/1.0 (+https://github.com/gnsdp99/devhub)";

    private final RestClient restClient;
    private final FeedAddressPolicy addressPolicy;
    private final FeedHttpProperties http;

    public HttpFeedFetcher(
            RestClient.Builder builder,
            FeedAddressPolicy addressPolicy,
            FeedHttpProperties http) {
        this.http = http;
        HttpClient httpClient = HttpClient.newBuilder()
                .connectTimeout(http.connectTimeout())
                .followRedirects(HttpClient.Redirect.NEVER)
                .build();
        JdkClientHttpRequestFactory requestFactory = new JdkClientHttpRequestFactory(httpClient);
        requestFactory.setReadTimeout(http.readTimeout());
        this.restClient = builder.requestFactory(requestFactory).build();
        this.addressPolicy = addressPolicy;
    }

    /**
     * @throws FeedFetchException 피드를 가져오지 못하면
     */
    @Override
    public FetchResult fetch(String feedUrl, String etag, String lastModified) {
        URI uri = uriOf(feedUrl);
        for (int hop = 0; hop <= http.maxRedirects(); hop++) {
            addressPolicy.check(uri);
            Hop result = exchange(uri, etag, lastModified);
            if (result.fetched() != null) {
                return result.fetched();
            }
            uri = uri.resolve(result.location());
        }
        throw new FeedFetchException("리다이렉트가 " + http.maxRedirects() + "회를 넘습니다: " + feedUrl);
    }

    private URI uriOf(String feedUrl) {
        try {
            return URI.create(feedUrl);
        } catch (IllegalArgumentException e) {
            throw new FeedFetchException("피드 URL 형식이 올바르지 않습니다: " + feedUrl, e);
        }
    }

    private Hop exchange(URI uri, String etag, String lastModified) {
        try {
            return restClient.get()
                    .uri(uri)
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
                    .exchange((request, response) -> toHop(uri, response));
        } catch (RestClientException e) {
            throw new FeedFetchException("피드를 가져오지 못했습니다: " + uri, e);
        }
    }

    private Hop toHop(URI uri, ClientHttpResponse response) throws IOException {
        HttpStatus status = HttpStatus.resolve(response.getStatusCode().value());
        if (status == HttpStatus.NOT_MODIFIED) {
            log.debug("변경되지 않았습니다. url={}", uri);
            return Hop.of(new FetchResult.NotModified());
        }
        if (status != null && status.is3xxRedirection()) {
            return Hop.to(redirectLocation(uri, response));
        }
        if (status == null || !status.is2xxSuccessful()) {
            throw new FeedFetchException("피드 응답이 " + response.getStatusCode() + "입니다: " + uri);
        }

        HttpHeaders headers = response.getHeaders();
        byte[] body = readBody(uri, response);
        return Hop.of(new FetchResult.Fetched(
                body, headers.getETag(), headers.getFirst(HttpHeaders.LAST_MODIFIED)));
    }

    private String redirectLocation(URI uri, ClientHttpResponse response) {
        String location = response.getHeaders().getFirst(HttpHeaders.LOCATION);
        if (location == null || location.isBlank()) {
            throw new FeedFetchException("리다이렉트 응답에 Location이 없습니다: " + uri);
        }
        return location;
    }

    private byte[] readBody(URI uri, ClientHttpResponse response) throws IOException {
        boolean gzipped = "gzip".equalsIgnoreCase(
                response.getHeaders().getFirst(HttpHeaders.CONTENT_ENCODING));
        int maxBodyBytes = Math.toIntExact(http.maxBodySize().toBytes());
        try (InputStream body = response.getBody();
                InputStream stream = gzipped ? new GZIPInputStream(body) : body) {
            byte[] read = stream.readNBytes(maxBodyBytes + 1);
            if (read.length > maxBodyBytes) {
                throw new FeedFetchException(
                        "피드 본문이 " + maxBodyBytes + "바이트를 넘습니다: " + uri);
            }
            return read;
        }
    }

    private record Hop(FetchResult fetched, String location) {

        static Hop of(FetchResult fetched) {
            return new Hop(fetched, null);
        }

        static Hop to(String location) {
            return new Hop(null, location);
        }
    }
}