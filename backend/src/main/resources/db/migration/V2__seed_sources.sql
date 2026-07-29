insert into source (slug, name, category, site_url, enabled)
values ('hackernews', 'Hacker News', 'NEWS', 'https://news.ycombinator.com', true),
       ('devto', 'DEV Community', 'NEWS', 'https://dev.to', true),
       ('yozm', '요즘IT', 'NEWS', 'https://yozm.wishket.com/magazine/', true),
       ('geeknews', 'GeekNews', 'NEWS', 'https://news.hada.io', true),
       ('infoq', 'InfoQ', 'NEWS', 'https://www.infoq.com', true),
       ('techcrunch', 'TechCrunch', 'NEWS', 'https://techcrunch.com', true),

       ('bytebytego', 'ByteByteGo', 'BLOG', 'https://blog.bytebytego.com', true),
       ('pragmatic-engineer', 'The Pragmatic Engineer', 'BLOG', 'https://newsletter.pragmaticengineer.com', true),
       ('cloudflare', 'Cloudflare', 'BLOG', 'https://blog.cloudflare.com', true),
       ('openai', 'OpenAI', 'BLOG', 'https://openai.com/news', true),
       ('huggingface', 'Hugging Face', 'BLOG', 'https://huggingface.co/blog', true),
       ('google', 'Google', 'BLOG', 'https://blog.google', true),
       ('aws', 'AWS', 'BLOG', 'https://aws.amazon.com/blogs', true),
       ('github', 'GitHub', 'BLOG', 'https://github.blog', true),
       ('anthropic', 'Anthropic', 'BLOG', 'https://www.anthropic.com/news', false),

       ('spring', 'Spring', 'OPENSOURCE', 'https://spring.io/blog', true),
       ('react', 'React', 'OPENSOURCE', 'https://react.dev/blog', true),
       ('nextjs', 'Next.js', 'OPENSOURCE', 'https://nextjs.org/blog', true),
       ('docker', 'Docker', 'OPENSOURCE', 'https://www.docker.com/blog', true),
       ('kubernetes', 'Kubernetes', 'OPENSOURCE', 'https://kubernetes.io/blog', true),
       ('redis', 'Redis', 'OPENSOURCE', 'https://redis.io/blog', true);

insert into feed (source_id, slug, name, feed_url)
select s.id, v.slug, v.name, v.feed_url
from (values ('hackernews', 'hackernews', 'Hacker News', 'https://hnrss.org/frontpage'),
             ('devto', 'devto', 'DEV Community', 'https://dev.to/feed'),
             ('yozm', 'yozm', '요즘IT', 'https://yozm.wishket.com/magazine/feed/'),
             ('geeknews', 'geeknews', 'GeekNews', 'https://feeds.feedburner.com/geeknews-feed'),
             ('infoq', 'infoq', 'InfoQ', 'https://feed.infoq.com/'),
             ('techcrunch', 'techcrunch', 'TechCrunch', 'https://techcrunch.com/feed/'),

             ('bytebytego', 'bytebytego', 'ByteByteGo', 'https://blog.bytebytego.com/feed'),
             ('pragmatic-engineer', 'pragmatic-engineer', 'The Pragmatic Engineer',
              'https://newsletter.pragmaticengineer.com/feed'),
             ('cloudflare', 'cloudflare', 'Cloudflare', 'https://blog.cloudflare.com/rss/'),
             ('openai', 'openai', 'OpenAI', 'https://openai.com/news/rss.xml'),
             ('huggingface', 'huggingface', 'Hugging Face', 'https://huggingface.co/blog/feed.xml'),

             ('google', 'google-keyword', 'The Keyword', 'https://blog.google/rss/'),
             ('google', 'google-developers', 'Google Developers',
              'https://developers.googleblog.com/feeds/posts/default/'),
             ('google', 'google-cloud', 'Google Cloud', 'https://cloudblog.withgoogle.com/rss/'),
             ('google', 'google-deepmind', 'Google DeepMind', 'https://deepmind.google/blog/rss.xml'),
             ('google', 'google-research', 'Google Research', 'https://research.google/blog/rss/'),

             ('aws', 'aws-news', 'AWS News Blog', 'https://aws.amazon.com/blogs/aws/feed/'),
             ('aws', 'aws-architecture', 'AWS Architecture Blog', 'https://aws.amazon.com/blogs/architecture/feed/'),

             ('github', 'github-blog', 'GitHub Blog', 'https://github.blog/feed/'),
             ('github', 'github-changelog', 'GitHub Changelog', 'https://github.blog/changelog/feed/'),

             ('spring', 'spring', 'Spring', 'https://spring.io/blog.atom'),
             ('react', 'react', 'React', 'https://react.dev/rss.xml'),
             ('nextjs', 'nextjs', 'Next.js', 'https://nextjs.org/feed.xml'),
             ('docker', 'docker', 'Docker', 'https://www.docker.com/feed/'),
             ('kubernetes', 'kubernetes', 'Kubernetes', 'https://kubernetes.io/feed.xml'),
             ('redis', 'redis', 'Redis', 'https://redis.io/blog/feed/')) as v(source_slug, slug, name, feed_url)
         join source s on s.slug = v.source_slug;
