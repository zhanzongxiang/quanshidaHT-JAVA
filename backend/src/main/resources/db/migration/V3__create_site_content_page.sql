create table if not exists site_content_page (
    id bigint primary key auto_increment,
    page_code varchar(64) not null,
    status varchar(32) not null default 'draft',
    form_json longtext not null,
    published_at datetime null,
    created_at datetime not null default current_timestamp,
    updated_at datetime not null default current_timestamp on update current_timestamp,
    unique key uk_site_content_page(page_code)
);
