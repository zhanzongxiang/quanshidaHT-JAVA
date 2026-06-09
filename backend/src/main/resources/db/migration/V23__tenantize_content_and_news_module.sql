alter table site_content_page
    add column tenant_id bigint null after id;

alter table news_article
    add column tenant_id bigint null after id;

update site_content_page
set tenant_id = (select id from tenant where tenant_code = 'default' limit 1)
where tenant_id is null;

update news_article
set tenant_id = (select id from tenant where tenant_code = 'default' limit 1)
where tenant_id is null;

alter table site_content_page
    modify column tenant_id bigint not null after id;

alter table news_article
    modify column tenant_id bigint not null after id;

alter table site_content_page
    drop index uk_site_content_page,
    add unique key uk_site_content_page_tenant_code(tenant_id, page_code),
    add key idx_site_content_page_tenant_status(tenant_id, status),
    add key idx_site_content_page_tenant_page_code(tenant_id, page_code);

alter table news_article
    drop index idx_news_article_publish_lookup,
    add key idx_news_article_tenant_publish_lookup(tenant_id, deleted, status, published_at, id),
    add key idx_news_article_tenant_status(tenant_id, status),
    add key idx_news_article_tenant_deleted(tenant_id, deleted);
