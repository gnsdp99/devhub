package com.devhub.collect.infra;

import static java.util.concurrent.TimeUnit.SECONDS;
import static org.assertj.core.api.Assertions.assertThat;

import com.devhub.collect.domain.Feed;
import java.util.List;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

@Timeout(30)
class VirtualThreadFeedCollectionExecutorUnitTest {

    private static final String ONE_HOST = "https://one.example.com/feed%d.xml";
    private static final String HOST_EACH = "https://a%d.example.com/feed.xml";

    @Nested
    @DisplayName("전체 동시 실행 상한")
    class TotalLimit {

        @Test
        @Timeout(10)
        @DisplayName("상한만큼은 동시에 실행한다")
        void runsUpToTheLimitAtOnce() {
            Meeting meeting = new Meeting(3);

            executorWith(3, 1).runAll(feedsOn(HOST_EACH, 12), meeting::arrive);

            assertThat(meeting.met()).isTrue();
        }

        @Test
        @Timeout(10)
        @DisplayName("상한을 넘겨 동시에 실행하지 않는다")
        void neverRunsMoreThanTheLimitAtOnce() {
            Meeting meeting = new Meeting(4);

            executorWith(3, 1).runAll(feedsOn(HOST_EACH, 12), meeting::arrive);

            assertThat(meeting.met()).isFalse();
        }
    }

    @Nested
    @DisplayName("호스트별 동시 실행 상한")
    class HostLimit {

        @Test
        @Timeout(10)
        @DisplayName("상한이 1이면 경로나 대소문자, 포트가 달라도 같은 호스트로 동시에 요청하지 않는다")
        void neverOverlapsOnTheSameHost() {
            Meeting meeting = new Meeting(2);
            List<Feed> feeds = List.of(
                    feed(1, "upper", "https://One.Example.com/a.xml"),
                    feed(2, "port", "https://one.example.com:8443/b.xml"));

            executorWith(8, 1).runAll(feeds, meeting::arrive);

            assertThat(meeting.met()).isFalse();
        }

        @Test
        @Timeout(10)
        @DisplayName("상한이 2면 같은 호스트로 2건까지 동시에 실행한다")
        void honoursTheConfiguredLimit() {
            Meeting meeting = new Meeting(2);

            executorWith(8, 2).runAll(feedsOn(ONE_HOST, 2), meeting::arrive);

            assertThat(meeting.met()).isTrue();
        }

        @Test
        @Timeout(10)
        @DisplayName("한 호스트에 몰린 피드가 기다려도 실행기 용량을 차지하지 않는다")
        void feedsBlockedOnOneHostDoNotConsumeExecutorCapacity() {
            List<Feed> feeds = concat(
                    feedsOn("https://crowded.example.com/feed%d.xml", 16),
                    feedsOn("https://other%d.example.com/feed.xml", 4));
            Meeting meeting = new Meeting(4);

            executorWith(4, 1).runAll(feeds, meeting::arrive);

            assertThat(meeting.met()).isTrue();
        }
    }

    @Nested
    @DisplayName("실행 보장")
    class ExecutionGuarantees {

        @Test
        @Timeout(10)
        @DisplayName("모든 피드를 한 번씩 실행하고 전부 끝난 뒤에 돌아온다")
        void runsEveryFeedExactlyOnceBeforeReturning() {
            List<Feed> feeds = feedsOn(HOST_EACH, 20);
            List<String> ran = new CopyOnWriteArrayList<>();

            executorWith(4, 1).runAll(feeds, f -> ran.add(f.slug()));

            assertThat(ran)
                    .containsExactlyInAnyOrderElementsOf(feeds.stream().map(Feed::slug).toList());
        }

        @Test
        @Timeout(10)
        @DisplayName("한 건이 예외를 던져도 permit을 반납하고 나머지를 실행한다")
        void releasesPermitsHeldByAFailingFeed() {
            AtomicInteger completed = new AtomicInteger();

            executorWith(1, 1).runAll(feedsOn(ONE_HOST, 4), f -> {
                if (f.feedUrl().contains("feed1.")) {
                    throw new IllegalStateException("boom");
                }
                completed.incrementAndGet();
            });

            assertThat(completed).hasValue(3);
        }

        @Test
        @Timeout(10)
        @DisplayName("host를 뽑을 수 없는 URL도 실행한다")
        void runsFeedsWithoutAUsableHost() {
            List<Feed> feeds = List.of(
                    feed(1, "broken", "not a url"),
                    feed(2, "mail", "mailto:editor@example.com"),
                    feed(3, "relative", "/feed.xml"),
                    feed(4, "ok", "https://a.example.com/feed.xml"));
            AtomicInteger completed = new AtomicInteger();

            executorWith(2, 1).runAll(feeds, _ -> completed.incrementAndGet());

            assertThat(completed).hasValue(4);
        }
    }

    private static VirtualThreadFeedCollectionExecutor executorWith(
            int concurrency, int hostConcurrency) {
        return new VirtualThreadFeedCollectionExecutor(
                new FeedExecutionProperties(concurrency, hostConcurrency));
    }

    private static List<Feed> feedsOn(String urlFormat, int count) {
        return IntStream.rangeClosed(1, count)
                .mapToObj(i -> feed(i, "feed-" + i, urlFormat.formatted(i)))
                .toList();
    }

    private static Feed feed(long id, String slug, String feedUrl) {
        return new Feed(id, slug, feedUrl, null, null);
    }

    private static List<Feed> concat(List<Feed> first, List<Feed> second) {
        return Stream.concat(first.stream(), second.stream()).toList();
    }

    private static final class Meeting {

        private static final int TIMEOUT_SECONDS = 2;

        private final CyclicBarrier gate;
        private final AtomicBoolean met = new AtomicBoolean();

        Meeting(int expected) {
            this.gate = new CyclicBarrier(expected);
        }

        void arrive(Feed feed) {
            if (awaited()) {
                met.set(true);
            }
        }

        private boolean awaited() {
            try {
                gate.await(TIMEOUT_SECONDS, SECONDS);
                return true;
            } catch (TimeoutException | BrokenBarrierException e) {
                return false;
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return false;
            }
        }

        boolean met() {
            return met.get();
        }
    }
}