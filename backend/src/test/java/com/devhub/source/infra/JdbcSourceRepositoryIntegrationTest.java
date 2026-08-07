package com.devhub.source.infra;

import static org.assertj.core.api.Assertions.assertThat;

import com.devhub.source.domain.Source;
import com.devhub.support.AbstractIntegrationTest;
import java.util.Comparator;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

@Transactional
class SourceRepositoryIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private JdbcSourceRepository repository;

    @Nested
    @DisplayName("활성 소스 목록")
    class Enabled {

        @Test
        @DisplayName("비활성 소스는 빼고 돌려준다")
        void leavesOutDisabledSources() {
            update("update source set enabled = false where slug = 'redis'");

            List<Source> sources = repository.findEnabled();

            assertThat(sources).extracting(Source::slug)
                    .contains("cloudflare")
                    .doesNotContain("redis");
        }

        @Test
        @DisplayName("DB Locale과 무관하게 이름 알파벳순으로 돌려주고 한글 이름은 뒤로 보낸다")
        void ordersSourcesByNameRegardlessOfTheDatabaseLocale() {
            List<Source> sources = repository.findEnabled();

            assertThat(sources).extracting(Source::name)
                    .isSortedAccordingTo(Comparator.naturalOrder())
                    .endsWith("한글과컴퓨터");
        }

        @Test
        @DisplayName("slug, 이름, 사이트 주소, 로고 주소를 채운다")
        void fillsEveryFieldOfASource() {
            List<Source> sources = repository.findEnabled();

            assertThat(sources)
                    .filteredOn(source -> source.slug().equals("cloudflare"))
                    .singleElement()
                    .isEqualTo(new Source(
                            "cloudflare", "Cloudflare", "https://blog.cloudflare.com",
                            "/logos/cloudflare.svg"));
        }
    }

    @Nested
    @DisplayName("slug로 찾기")
    class FindBySlug {

        @Test
        @DisplayName("활성 소스면 id를 돌려준다")
        void findsTheIdOfAnEnabledSource() {
            assertThat(repository.findEnabledIdBySlug("cloudflare")).isPresent();
        }

        @Test
        @DisplayName("비활성 소스면 빈 값을 돌려준다")
        void findsNothingForADisabledSource() {
            update("update source set enabled = false where slug = 'redis'");

            assertThat(repository.findEnabledIdBySlug("redis")).isEmpty();
        }

        @Test
        @DisplayName("그런 slug가 없으면 빈 값을 돌려준다")
        void findsNothingForAnUnknownSlug() {
            assertThat(repository.findEnabledIdBySlug("없는소스")).isEmpty();
        }
    }
}