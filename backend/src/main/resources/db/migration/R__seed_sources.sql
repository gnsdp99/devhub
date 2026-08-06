drop table if exists pg_temp.desired_source;
create temporary table desired_source
(
    slug     text primary key,
    name     text not null,
    site_url text,
    feed_url text,
    logo_url text
);

insert into desired_source (slug, name, site_url, feed_url)
values ('openai', 'OpenAI', 'https://openai.com/news', null),
       ('google', 'Google', 'https://blog.google', null),
       ('aws', 'AWS', 'https://aws.amazon.com/blogs', null),
       ('github', 'GitHub', 'https://github.blog', null),

       ('huggingface', 'Hugging Face', 'https://huggingface.co/blog', 'https://huggingface.co/blog/feed.xml'),
       ('cloudflare', 'Cloudflare', 'https://blog.cloudflare.com', 'https://blog.cloudflare.com/rss/'),
       ('spring', 'Spring', 'https://spring.io/blog', 'https://spring.io/blog.atom'),
       ('nextjs', 'Next.js', 'https://nextjs.org/blog', 'https://nextjs.org/feed.xml'),
       ('docker', 'Docker', 'https://www.docker.com/blog', 'https://www.docker.com/feed/'),
       ('kubernetes', 'Kubernetes', 'https://kubernetes.io/blog', 'https://kubernetes.io/feed.xml'),
       ('redis', 'Redis', 'https://redis.io/blog', 'https://redis.io/blog/feed/'),

       ('naver-d2', '네이버 D2', 'https://d2.naver.com', 'https://d2.naver.com/d2.atom'),
       ('gccompany', '여기어때', 'https://techblog.gccompany.co.kr', 'https://techblog.gccompany.co.kr/feed'),
       ('postype', '포스타입', 'https://www.postype.com/@team', 'https://www.postype.com/@team/rss'),
       ('ahnlab-cloudmate', 'AhnLab 클라우드메이트', 'https://techblog.ahnlabcloudmate.com',
        'https://techblog.ahnlabcloudmate.com/rss/'),
       ('channeltalk', '채널톡', 'https://tech.channel.io/ko', 'https://tech.channel.io/rss.xml'),
       ('daangn', '당근', 'https://medium.com/daangn', 'https://medium.com/feed/daangn'),
       ('toss', '토스', 'https://toss.tech', 'https://toss.tech/rss.xml'),
       ('kakao', '카카오', 'https://tech.kakao.com/blog', 'https://tech.kakao.com/feed'),
       ('flex', 'flex', 'https://flex.team/blog', 'https://flex.team/blog/rss.xml'),
       ('lotteon', '롯데온', 'https://techblog.lotteon.com', 'https://techblog.lotteon.com/feed'),
       ('nhn-cloud', 'NHN 클라우드', 'https://meetup.nhncloud.com', 'https://meetup.nhncloud.com/rss'),
       ('line', 'LINE', 'https://techblog.lycorp.co.jp/ko', 'https://techblog.lycorp.co.jp/ko/feed/index.xml'),
       ('kt-cloud', 'kt cloud', 'https://tech.ktcloud.com', 'https://ktcloudplatform.tistory.com/rss'),
       ('delightroom', '딜라이트룸', 'https://medium.com/delightroom', 'https://medium.com/feed/delightroom'),
       ('scatterlab', '스캐터랩', 'https://blog.pingpong.us', 'https://blog.pingpong.us/rss'),
       ('infograb', '인포그랩', 'https://insight.infograb.net/blog', 'https://insight.infograb.net/blog/rss.xml'),
       ('miridih', '미리디', 'https://medium.com/miridih', 'https://medium.com/feed/miridih'),
       ('kakaobank', '카카오뱅크', 'https://tech.kakaobank.com', 'https://tech.kakaobank.com/index.xml'),
       ('balancehero', '밸런스히어로', 'https://blog.balancehero.com', 'https://blog.balancehero.com/rss'),
       ('socar', '쏘카', 'https://tech.socarcorp.kr', 'https://tech.socarcorp.kr/rss.xml'),
       ('oliveyoung', '올리브영', 'https://oliveyoung.tech', 'https://oliveyoung.tech/rss.xml'),
       ('nongshim-cloud', '농심 클라우드', 'https://tech.cloud.nongshim.co.kr', 'https://tech.cloud.nongshim.co.kr/feed/'),
       ('rapportlabs', '라포랩스', 'https://blog.rapportlabs.kr', 'https://blog.rapportlabs.kr/rss'),
       ('imweb', '아임웹', 'https://tech.imweb.me', 'https://tech.imweb.me/feed.xml'),
       ('lunit', '루닛', 'https://medium.com/lunit', 'https://medium.com/feed/lunit'),
       ('boostbrothers', '부스트브라더스', 'https://boostbrothers.github.io', 'https://boostbrothers.github.io/rss'),
       ('ssg', 'SSG닷컴', 'https://medium.com/ssgtech', 'https://medium.com/feed/ssgtech'),
       ('musinsa', '무신사', 'https://medium.com/musinsa-tech', 'https://medium.com/feed/musinsa-tech'),
       ('tving', '티빙', 'https://medium.com/tving-team', 'https://medium.com/feed/tving-team'),
       ('buzzvil', '버즈빌', 'https://tech.buzzvil.com', 'https://tech.buzzvil.com/feed.xml'),
       ('naver-financial', '네이버파이낸셜', 'https://medium.com/naverfinancial', 'https://medium.com/feed/naverfinancial'),
       ('samsung', '삼성전자', 'https://techblog.samsung.com', 'https://techblog.samsung.com/rss'),
       ('hancom', '한글과컴퓨터', 'https://tech.hancom.com', 'https://tech.hancom.com/feed/'),
       ('watcha', '왓챠', 'https://medium.com/watcha', 'https://medium.com/feed/watcha'),
       ('inflab', '인프랩', 'https://tech.inflab.com', 'https://tech.inflab.com/rss.xml'),
       ('kyobodts', '교보DTS', 'https://blog.kyobodts.co.kr', 'https://blog.kyobodts.co.kr/feed/'),
       ('petfriends', '펫프렌즈', 'https://techblog.pet-friends.co.kr', 'https://techblog.pet-friends.co.kr/feed'),
       ('madup', '매드업', 'https://tech.madup.com', 'https://tech.madup.com/feed.xml'),
       ('babitalk', '바비톡', 'https://medium.com/babitalk-blog', 'https://medium.com/feed/babitalk-blog'),
       ('kmong', '크몽', 'https://medium.com/kmong', 'https://medium.com/feed/kmong'),
       ('catchtable', '캐치테이블', 'https://medium.com/catchtable', 'https://medium.com/feed/catchtable'),
       ('devsisters', '데브시스터즈', 'https://tech.devsisters.com', 'https://tech.devsisters.com/rss.xml'),
       ('kurly', '컬리', 'https://helloworld.kurly.com', 'https://helloworld.kurly.com/rss.xml'),
       ('goorm', '구름', 'https://tech.goorm.io', 'https://tech.goorm.io/feed/'),

       ('netflix', 'Netflix', 'https://netflixtechblog.com', 'https://netflixtechblog.com/feed'),
       ('airbnb', 'Airbnb', 'https://medium.com/airbnb-engineering', 'https://medium.com/feed/airbnb-engineering'),
       ('pinterest', 'Pinterest', 'https://medium.com/pinterest-engineering',
        'https://medium.com/feed/pinterest-engineering'),
       ('lyft', 'Lyft', 'https://eng.lyft.com', 'https://eng.lyft.com/feed'),
       ('instacart', 'Instacart', 'https://tech.instacart.com', 'https://tech.instacart.com/feed'),
       ('booking', 'Booking.com', 'https://medium.com/booking-com-development',
        'https://medium.com/feed/booking-com-development'),
       ('expedia', 'Expedia Group', 'https://medium.com/expedia-group-tech',
        'https://medium.com/feed/expedia-group-tech'),
       ('agoda', 'Agoda', 'https://medium.com/agoda-engineering', 'https://medium.com/feed/agoda-engineering'),
       ('criteo', 'Criteo', 'https://medium.com/criteo-engineering', 'https://medium.com/feed/criteo-engineering'),
       ('medium', 'Medium', 'https://medium.engineering', 'https://medium.engineering/feed'),
       ('meta', 'Meta', 'https://engineering.fb.com', 'https://engineering.fb.com/feed/'),
       ('slack', 'Slack', 'https://slack.engineering', 'https://slack.engineering/feed/'),
       ('spotify', 'Spotify', 'https://engineering.atspotify.com', 'https://engineering.atspotify.com/feed'),
       ('dropbox', 'Dropbox', 'https://dropbox.tech', 'https://dropbox.tech/feed'),
       ('canva', 'Canva', 'https://www.canva.dev/blog/engineering', 'https://www.canva.dev/blog/engineering/feed.xml'),
       ('zalando', 'Zalando', 'https://engineering.zalando.com', 'https://engineering.zalando.com/atom.xml'),
       ('grab', 'Grab', 'https://engineering.grab.com', 'https://engineering.grab.com/feed.xml'),
       ('datadog', 'Datadog', 'https://www.datadoghq.com/blog/engineering',
        'https://www.datadoghq.com/blog/engineering/index.xml'),
       ('etsy', 'Etsy', 'https://www.etsy.com/codeascraft', 'https://www.etsy.com/codeascraft/rss'),
       ('ramp', 'Ramp', 'https://builders.ramp.com', 'https://builders.ramp.com/feed.xml'),
       ('squarespace', 'Squarespace', 'https://engineering.squarespace.com',
        'https://engineering.squarespace.com/blog?format=rss'),
       ('wix', 'Wix', 'https://www.wix.engineering', 'https://www.wix.engineering/blog-feed.xml'),
       ('trivago', 'trivago', 'https://tech.trivago.com', 'https://tech.trivago.com/rss.xml'),
       ('yelp', 'Yelp', 'https://engineeringblog.yelp.com', 'https://engineeringblog.yelp.com/feed.xml'),
       ('salesforce', 'Salesforce', 'https://engineering.salesforce.com', 'https://engineering.salesforce.com/feed/'),
       ('block', 'Block', 'https://engineering.block.xyz', 'https://engineering.block.xyz/blog/rss.xml'),
       ('target', 'Target', 'https://tech.target.com', 'https://tech.target.com/rss/feed.xml'),
       ('nvidia', 'NVIDIA', 'https://developer.nvidia.com/ko-kr/blog', 'https://developer.nvidia.com/ko-kr/blog/feed/'),

       ('nodejs', 'Node.js', 'https://nodejs.org/en/blog', 'https://nodejs.org/en/feed/blog.xml'),
       ('jetbrains', 'JetBrains', 'https://blog.jetbrains.com', 'https://blog.jetbrains.com/feed/'),
       ('kotlin', 'Kotlin', 'https://blog.jetbrains.com/kotlin', 'https://blog.jetbrains.com/kotlin/feed/'),
       ('duckdb', 'DuckDB', 'https://duckdb.org/news', 'https://duckdb.org/feed.xml'),
       ('vercel', 'Vercel', 'https://vercel.com/blog', 'https://vercel.com/atom'),
       ('cncf', 'CNCF', 'https://www.cncf.io/blog', 'https://www.cncf.io/feed/'),
       ('clickhouse', 'ClickHouse', 'https://clickhouse.com/blog', 'https://clickhouse.com/rss.xml'),
       ('supabase', 'Supabase', 'https://supabase.com/blog', 'https://supabase.com/rss.xml'),
       ('opensearch', 'OpenSearch', 'https://opensearch.org/blog', 'https://opensearch.org/feed/'),
       ('inside-java', 'Inside.java', 'https://inside.java', 'https://inside.java/feed.xml'),
       ('rust', 'Rust', 'https://blog.rust-lang.org', 'https://blog.rust-lang.org/feed.xml'),
       ('postgresql', 'PostgreSQL', 'https://www.postgresql.org/about/newsarchive',
        'https://www.postgresql.org/news.rss'),
       ('elastic', 'Elastic', 'https://www.elastic.co/blog', 'https://www.elastic.co/blog/feed'),
       ('dotnet', '.NET', 'https://devblogs.microsoft.com/dotnet', 'https://devblogs.microsoft.com/dotnet/feed/'),
       ('django', 'Django', 'https://www.djangoproject.com/weblog', 'https://www.djangoproject.com/rss/weblog/'),
       ('temporal', 'Temporal', 'https://temporal.io/blog', 'https://temporal.io/blog/feed.xml'),
       ('openssf', 'OpenSSF', 'https://openssf.org/blog', 'https://openssf.org/feed/'),
       ('pulumi', 'Pulumi', 'https://www.pulumi.com/blog', 'https://www.pulumi.com/blog/rss.xml'),
       ('gitlab', 'GitLab', 'https://about.gitlab.com/blog', 'https://about.gitlab.com/atom.xml'),
       ('svelte', 'Svelte', 'https://svelte.dev/blog', 'https://svelte.dev/blog/rss.xml'),
       ('angular', 'Angular', 'https://blog.angular.dev', 'https://blog.angular.dev/feed'),
       ('grafana', 'Grafana', 'https://grafana.com/blog', 'https://grafana.com/blog/index.xml'),
       ('astro', 'Astro', 'https://astro.build/blog', 'https://astro.build/rss.xml'),
       ('rails', 'Ruby on Rails', 'https://rubyonrails.org/blog', 'https://rubyonrails.org/feed.xml'),
       ('confluent', 'Confluent', 'https://www.confluent.io/blog', 'https://www.confluent.io/rss.xml'),
       ('laravel', 'Laravel', 'https://blog.laravel.com', 'https://blog.laravel.com/feed'),
       ('python', 'Python', 'https://pyfound.blogspot.com', 'https://pyfound.blogspot.com/feeds/posts/default'),
       ('gradle', 'Gradle', 'https://blog.gradle.org', 'https://feed.gradle.org/blog.atom'),
       ('azure', 'Azure', 'https://azure.microsoft.com/ko-kr/blog', 'https://azure.microsoft.com/ko-kr/blog/feed/'),
       ('nuxt', 'Nuxt', 'https://nuxt.com/blog', 'https://nuxt.com/blog/rss.xml'),
       ('flink', 'Apache Flink', 'https://flink.apache.org/posts', 'https://flink.apache.org/posts/index.xml'),
       ('istio', 'Istio', 'https://istio.io/latest/blog', 'https://istio.io/latest/feed.xml'),
       ('bun', 'Bun', 'https://bun.sh/blog', 'https://bun.sh/rss.xml'),
       ('typescript', 'TypeScript', 'https://devblogs.microsoft.com/typescript',
        'https://devblogs.microsoft.com/typescript/feed/'),
       ('airflow', 'Apache Airflow', 'https://airflow.apache.org/blog', 'https://airflow.apache.org/blog/index.xml'),
       ('prometheus', 'Prometheus', 'https://prometheus.io/blog', 'https://prometheus.io/blog/feed.xml'),
       ('mongodb', 'MongoDB', 'https://www.mongodb.com/company/blog', 'https://www.mongodb.com/blog/rss'),
       ('deno', 'Deno', 'https://deno.com/blog', 'https://deno.com/feed'),
       ('vite', 'Vite', 'https://vite.dev/blog', 'https://vite.dev/blog.rss'),
       ('go', 'Go', 'https://go.dev/blog', 'https://go.dev/blog/feed.atom'),
       ('tailwindcss', 'Tailwind CSS', 'https://tailwindcss.com/blog', 'https://tailwindcss.com/feeds/feed.xml'),
       ('nginx', 'NGINX', 'https://blog.nginx.org', 'https://blog.nginx.org/feed');

update desired_source
   set logo_url = '/logos/' || slug ||
                  case when slug in ('astro', 'cloudflare', 'criteo', 'prometheus', 'vite') then '.svg' else '.png' end;

drop table if exists pg_temp.desired_feed;
create temporary table desired_feed
(
    source_slug text not null,
    slug        text primary key,
    name        text not null,
    feed_url    text not null unique
);

insert into desired_feed (source_slug, slug, name, feed_url)
select slug, slug, name, feed_url
  from desired_source
 where feed_url is not null;

insert into desired_feed (source_slug, slug, name, feed_url)
values ('openai', 'openai', 'OpenAI', 'https://openai.com/news/rss.xml'),
       ('openai', 'openai-developers', 'OpenAI Developers', 'https://developers.openai.com/rss.xml'),

       ('google', 'google-cloud', 'Google Cloud', 'https://cloudblog.withgoogle.com/rss/'),
       ('google', 'google-deepmind', 'Google DeepMind', 'https://deepmind.google/blog/rss.xml'),
       ('google', 'google-research', 'Google Research', 'https://research.google/blog/rss/'),
       ('google', 'google-developers', 'Google Developers', 'https://developers.googleblog.com/feeds/posts/default/'),

       ('aws', 'aws-news', 'AWS News Blog', 'https://aws.amazon.com/blogs/aws/feed/'),
       ('aws', 'aws-architecture', 'AWS Architecture Blog', 'https://aws.amazon.com/blogs/architecture/feed/'),
       ('aws', 'aws-database', 'AWS Database Blog', 'https://aws.amazon.com/blogs/database/feed/'),
       ('aws', 'aws-compute', 'AWS Compute Blog', 'https://aws.amazon.com/blogs/compute/feed/'),
       ('aws', 'aws-containers', 'AWS Containers Blog', 'https://aws.amazon.com/blogs/containers/feed/'),
       ('aws', 'aws-security', 'AWS Security Blog', 'https://aws.amazon.com/blogs/security/feed/'),
       ('aws', 'aws-devops', 'AWS DevOps Blog', 'https://aws.amazon.com/blogs/devops/feed/'),
       ('aws', 'aws-big-data', 'AWS Big Data Blog', 'https://aws.amazon.com/blogs/big-data/feed/'),
       ('aws', 'aws-machine-learning', 'AWS Machine Learning Blog',
        'https://aws.amazon.com/blogs/machine-learning/feed/'),
       ('aws', 'aws-opensource', 'AWS Open Source Blog', 'https://aws.amazon.com/blogs/opensource/feed/'),
       ('aws', 'aws-developer', 'AWS Developer Tools Blog', 'https://aws.amazon.com/blogs/developer/feed/'),

       ('github', 'github-engineering', 'GitHub Engineering', 'https://github.blog/engineering/feed/'),
       ('github', 'github-ai-ml', 'GitHub AI & ML', 'https://github.blog/ai-and-ml/feed/'),
       ('github', 'github-developer-skills', 'GitHub Developer Skills', 'https://github.blog/developer-skills/feed/'),
       ('github', 'github-open-source', 'GitHub Open Source', 'https://github.blog/open-source/feed/'),
       ('github', 'github-security', 'GitHub Security', 'https://github.blog/security/feed/'),
       ('github', 'github-news-insights', 'GitHub News & Insights', 'https://github.blog/news-insights/feed/'),
       ('github', 'github-enterprise', 'GitHub Enterprise', 'https://github.blog/enterprise-software/feed/');

do
$$
    declare
        sources bigint := (select count(*) from desired_source);
        feeds   bigint := (select count(*) from desired_feed);
        unknown bigint := (select count(*)
                             from desired_feed d
                            where not exists (select 1 from desired_source s where s.slug = d.source_slug));
    begin
        if sources <> 125 or feeds <> 145 or unknown <> 0 then
            raise exception 'seed looks wrong: sources=%, feeds=%, feeds with unknown source=%',
                sources, feeds, unknown;
        end if;
    end
$$;

drop table if exists pg_temp.doomed_feed;
create temporary table doomed_feed as
select f.id
  from feed f
 where not exists (select 1 from desired_feed d where d.slug = f.slug)
    or exists (select 1 from desired_feed d where d.feed_url = f.feed_url and d.slug <> f.slug);

update article_source asrc
   set feed_id = (select f.id
                    from feed f
                   where f.source_id = asrc.source_id
                     and f.id not in (select id from doomed_feed)
                   order by f.id
                   limit 1)
 where asrc.feed_id in (select id from doomed_feed)
   and exists (select 1
                 from feed f
                where f.source_id = asrc.source_id
                  and f.id not in (select id from doomed_feed));

delete
  from article_source
 where feed_id in (select id from doomed_feed);

delete
  from article a
 where not exists (select 1 from article_source s where s.article_id = a.id);

delete
  from feed
 where id in (select id from doomed_feed);

insert into source (slug, name, site_url, logo_url)
select slug, name, site_url, logo_url
  from desired_source
on conflict (slug) do update
    set name     = excluded.name,
        site_url = excluded.site_url,
        logo_url = excluded.logo_url
 where source.name is distinct from excluded.name
    or source.site_url is distinct from excluded.site_url
    or source.logo_url is distinct from excluded.logo_url;

insert into feed (source_id, slug, name, feed_url)
select s.id, d.slug, d.name, d.feed_url
  from desired_feed d
       join source s on s.slug = d.source_slug
on conflict (slug) do update
    set source_id     = excluded.source_id,
        name          = excluded.name,
        feed_url      = excluded.feed_url,
        etag          = case when feed.feed_url is distinct from excluded.feed_url
                             then null else feed.etag end,
        last_modified = case when feed.feed_url is distinct from excluded.feed_url
                             then null else feed.last_modified end
 where feed.source_id is distinct from excluded.source_id
    or feed.name is distinct from excluded.name
    or feed.feed_url is distinct from excluded.feed_url;

delete
  from source s
 where not exists (select 1 from desired_source d where d.slug = s.slug);

do
$$
    begin
        if (select count(*) from source) <> (select count(*) from desired_source)
            or (select count(*) from feed) <> (select count(*) from desired_feed) then
            raise exception 'seed count mismatch: sources=%, feeds=%',
                (select count(*) from source), (select count(*) from feed);
        end if;
    end
$$;
