insert into admin_menu(parent_id, name, path, component, icon, sort_no)
select 0, 'Page Management', '/pages', 'MenuGroup', 'FolderOpened', 20
from dual
where not exists (select 1 from admin_menu where path = '/pages');

set @pages_parent_id := (select id from admin_menu where path = '/pages' limit 1);

update admin_menu
set parent_id = @pages_parent_id,
    name = 'Home Config',
    path = '/pages/home',
    component = 'HomeContent',
    icon = 'House',
    sort_no = 21
where path = '/content';

insert into admin_menu(parent_id, name, path, component, icon, sort_no)
select @pages_parent_id, 'Home Config', '/pages/home', 'HomeContent', 'House', 21
from dual
where not exists (select 1 from admin_menu where path = '/pages/home');

update admin_menu
set parent_id = @pages_parent_id,
    name = 'News',
    component = 'News',
    icon = 'Tickets',
    sort_no = 23
where path = '/news';

insert into admin_menu(parent_id, name, path, component, icon, sort_no)
select @pages_parent_id, 'Service Lines', '/pages/service-lines', 'ServiceLines', 'Files', 22
from dual
where not exists (select 1 from admin_menu where path = '/pages/service-lines');

insert into admin_menu(parent_id, name, path, component, icon, sort_no)
select 0, 'Global Settings', '/settings', 'MenuGroup', 'Setting', 30
from dual
where not exists (select 1 from admin_menu where path = '/settings');

set @settings_parent_id := (select id from admin_menu where path = '/settings' limit 1);

insert into admin_menu(parent_id, name, path, component, icon, sort_no)
select @settings_parent_id, 'Navigation', '/settings/navigation', 'NavigationSettings', 'Guide', 31
from dual
where not exists (select 1 from admin_menu where path = '/settings/navigation');

insert into admin_menu(parent_id, name, path, component, icon, sort_no)
select @settings_parent_id, 'Footer', '/settings/footer', 'FooterSettings', 'Bottom', 32
from dual
where not exists (select 1 from admin_menu where path = '/settings/footer');

insert into admin_menu(parent_id, name, path, component, icon, sort_no)
select @settings_parent_id, 'Contact', '/settings/contact', 'ContactSettings', 'Phone', 33
from dual
where not exists (select 1 from admin_menu where path = '/settings/contact');

insert into permission(perm_code, perm_name)
values ('settings:view', 'View settings'),
       ('settings:edit', 'Edit settings')
on duplicate key update perm_name = values(perm_name);

insert into admin_role_permission(role_id, permission_id)
select r.id, p.id
from admin_role r
join permission p on p.perm_code in ('settings:view', 'settings:edit')
where r.role_code = 'super_admin'
on duplicate key update role_id = values(role_id);

insert into admin_role_menu(role_id, menu_id)
select r.id, m.id
from admin_role r
join admin_menu m on m.path in (
    '/pages',
    '/pages/home',
    '/pages/service-lines',
    '/news',
    '/settings',
    '/settings/navigation',
    '/settings/footer',
    '/settings/contact'
)
where r.role_code = 'super_admin'
on duplicate key update role_id = values(role_id);
