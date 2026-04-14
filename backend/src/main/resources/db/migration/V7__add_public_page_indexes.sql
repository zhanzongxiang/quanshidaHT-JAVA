create index idx_news_article_publish_lookup
    on news_article (deleted, status, published_at, id);
