create table source (
    id         bigserial primary key,
    slug       text        not null unique,
    name       text        not null,
    category   text        not null check (category in ('NEWS', 'BLOG', 'OPENSOURCE')),
    site_url   text,
    enabled    boolean     not null default true,
    created_at timestamptz not null default now(),
    updated_at timestamptz not null default now()
);

create table feed (
    id                   bigserial primary key,
    source_id            bigint      not null references source (id),
    slug                 text        not null unique,
    name                 text        not null,
    feed_url             text        not null unique,
    enabled              boolean     not null default true,
    etag                 text,
    last_modified        text,
    last_fetched_at      timestamptz,
    last_success_at      timestamptz,
    consecutive_failures integer     not null default 0,
    created_at           timestamptz not null default now(),
    updated_at           timestamptz not null default now()
);

create index idx_feed_source on feed (source_id);

create table article (
    id           bigserial primary key,
    feed_id      bigint      not null references feed (id),
    source_id    bigint      not null references source (id),
    guid         text,
    url          text        not null,
    url_hash     bytea       not null,
    title        text        not null,
    summary      text,
    author       text,
    published_at timestamptz not null,
    collected_at timestamptz not null default now(),
    constraint uq_article_url_hash unique (url_hash)
);

create index idx_article_recent on article (published_at desc, id desc);
create index idx_article_by_source on article (source_id, published_at desc, id desc);
