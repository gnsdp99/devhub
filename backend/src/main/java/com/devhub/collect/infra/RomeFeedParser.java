package com.devhub.collect.infra;

import com.devhub.collect.app.port.out.FeedParser;
import com.devhub.collect.domain.FeedParseException;
import com.devhub.collect.domain.ParsedArticle;
import com.rometools.rome.feed.synd.SyndContent;
import com.rometools.rome.feed.synd.SyndEntry;
import com.rometools.rome.feed.synd.SyndFeed;
import com.rometools.rome.io.FeedException;
import com.rometools.rome.io.SyndFeedInput;
import com.rometools.rome.io.XmlReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.time.Clock;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class RomeFeedParser implements FeedParser {

    private final Clock clock;

    @Override
    public List<ParsedArticle> parse(byte[] body) {
        SyndFeed feed = read(body);
        Instant now = clock.instant();

        List<ParsedArticle> articles = new ArrayList<>();
        for (SyndEntry entry : feed.getEntries()) {
            String url = trimToNull(entry.getLink());
            String title = trimToNull(entry.getTitle());
            if (url == null || title == null) {
                log.warn("링크나 제목이 없어 건너뜁니다. uri={}", entry.getUri());
                continue;
            }
            articles.add(new ParsedArticle(
                    trimToNull(entry.getUri()),
                    url,
                    title,
                    summaryOf(entry),
                    trimToNull(entry.getAuthor()),
                    publishedAtOf(entry, now)));
        }
        return articles;
    }

    private SyndFeed read(byte[] body) {
        SyndFeedInput input = new SyndFeedInput();
        try (XmlReader reader = new XmlReader(new ByteArrayInputStream(body))) {
            return input.build(reader);
        } catch (IOException | FeedException | IllegalArgumentException e) {
            throw new FeedParseException("피드를 파싱할 수 없습니다.", e);
        }
    }

    private String summaryOf(SyndEntry entry) {
        SyndContent content = entry.getDescription();
        if (content == null && !entry.getContents().isEmpty()) {
            content = entry.getContents().getFirst();
        }
        if (content == null) {
            return null;
        }
        return trimToNull(Jsoup.parse(content.getValue()).text());
    }

    private Instant publishedAtOf(SyndEntry entry, Instant now) {
        Instant published = toInstant(entry.getPublishedDate());
        if (published == null) {
            published = toInstant(entry.getUpdatedDate());
        }
        return published == null || published.isAfter(now) ? now : published;
    }

    private Instant toInstant(Date date) {
        return date == null ? null : date.toInstant();
    }

    private String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String stripped = value.strip();
        return stripped.isEmpty() ? null : stripped;
    }
}