create table article_source (
    article_id   bigint      not null references article (id) on delete cascade,
    source_id    bigint      not null references source (id),
    feed_id      bigint      not null references feed (id),
    guid         text,
    published_at timestamptz not null,
    collected_at timestamptz not null default now(),
    primary key (article_id, source_id)
);

insert into article_source (article_id, source_id, feed_id, guid, published_at, collected_at)
select id, source_id, feed_id, guid, published_at, collected_at
  from article;

create index idx_article_source_recent on article_source (source_id, published_at desc, article_id desc);

alter table article
    drop column feed_id,
    drop column source_id,
    drop column guid;