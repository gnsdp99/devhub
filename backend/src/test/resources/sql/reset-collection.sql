delete from article_source;
delete from article;
delete from feed where source_id = (select id from source where slug = 'test-source');
delete from source where slug = 'test-source';

update feed
   set enabled = true,
       etag = null,
       last_modified = null,
       last_fetched_at = null,
       last_success_at = null,
       consecutive_failures = 0;