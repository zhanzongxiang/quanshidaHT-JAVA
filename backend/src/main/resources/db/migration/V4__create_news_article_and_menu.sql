create table if not exists news_article (
    id bigint primary key auto_increment,
    title varchar(120) not null,
    summary varchar(500) not null,
    cover_image_url varchar(500) null,
    content longtext not null,
    author varchar(64) not null default 'QSD Admin',
    status varchar(32) not null default 'draft',
    published_at datetime null,
    deleted tinyint not null default 0,
    created_at datetime not null default current_timestamp,
    updated_at datetime not null default current_timestamp on update current_timestamp
);

insert into permission(perm_code, perm_name)
values ('news:view', 'View news'),
       ('news:edit', 'Edit news')
on duplicate key update perm_name = values(perm_name);

insert into admin_menu(parent_id, name, path, component, icon, sort_no)
values (0, 'News', '/news', 'News', 'Tickets', 30)
on duplicate key update
    name = values(name),
    component = values(component),
    icon = values(icon),
    sort_no = values(sort_no);

insert into admin_role_permission(role_id, permission_id)
select r.id, p.id
from admin_role r
join permission p on p.perm_code in ('news:view', 'news:edit')
where r.role_code = 'super_admin'
on duplicate key update role_id = values(role_id);

insert into admin_role_menu(role_id, menu_id)
select r.id, m.id
from admin_role r
join admin_menu m on m.path = '/news'
where r.role_code = 'super_admin'
on duplicate key update role_id = values(role_id);

insert into news_article(title, summary, cover_image_url, content, author, status, published_at, deleted)
values
(
    'QSD launches new Asia line',
    'The new service line improves delivery options for Asia-bound cargo.',
    'https://images.unsplash.com/photo-1520607162513-77705c0f0d4a?auto=format&fit=crop&w=1200&q=80',
    'QSD has launched a new Asia service line to improve cargo delivery efficiency and route flexibility for regional customers.',
    'QSD Admin',
    'published',
    now(),
    0
),
(
    'Warehouse upgrade completed',
    'The warehouse upgrade expands sorting capacity and improves shipment turnover.',
    'https://images.unsplash.com/photo-1586528116311-ad8dd3c8310d?auto=format&fit=crop&w=1200&q=80',
    'The latest warehouse upgrade has been completed and is now supporting higher daily throughput and more stable dispatch operations.',
    'QSD Admin',
    'draft',
    null,
    0
);
