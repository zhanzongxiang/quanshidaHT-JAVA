insert into permission(perm_code, perm_name)
values ('waybill:view', '查看运单'),
       ('waybill:edit', '编辑运单'),
       ('dict:view', '查看字典'),
       ('dict:edit', '编辑字典'),
       ('member:view', '查看会员'),
       ('member:edit', '编辑会员'),
       ('payment:view', '查看支付'),
       ('payment:edit', '管理支付')
on duplicate key update perm_name = values(perm_name);

update permission set perm_name = '查看运单' where perm_code = 'waybill:view';
update permission set perm_name = '编辑运单' where perm_code = 'waybill:edit';
update permission set perm_name = '查看字典' where perm_code = 'dict:view';
update permission set perm_name = '编辑字典' where perm_code = 'dict:edit';
update permission set perm_name = '查看会员' where perm_code = 'member:view';
update permission set perm_name = '编辑会员' where perm_code = 'member:edit';
update permission set perm_name = '查看支付' where perm_code = 'payment:view';
update permission set perm_name = '管理支付' where perm_code = 'payment:edit';

set @pages_parent_id := (select id from admin_menu where path = '/pages' limit 1);
set @settings_parent_id := (select id from admin_menu where path = '/settings' limit 1);

insert into admin_menu(parent_id, name, path, component, icon, sort_no)
select @pages_parent_id, '运单管理', '/waybills', 'Waybill', 'Van', 24
from dual
where not exists (select 1 from admin_menu where path = '/waybills');

insert into admin_menu(parent_id, name, path, component, icon, sort_no)
select 0, '会员管理', '/members', 'Members', 'User', 25
from dual
where not exists (select 1 from admin_menu where path = '/members');

insert into admin_menu(parent_id, name, path, component, icon, sort_no)
select 0, '支付管理', '/payments', 'Payments', 'CreditCard', 26
from dual
where not exists (select 1 from admin_menu where path = '/payments');

insert into admin_menu(parent_id, name, path, component, icon, sort_no)
select @settings_parent_id, '字典管理', '/settings/dictionaries', 'DictionarySettings', 'Collection', 34
from dual
where not exists (select 1 from admin_menu where path = '/settings/dictionaries');

update admin_menu set name = '运单管理' where path = '/waybills';
update admin_menu set name = '会员管理' where path = '/members';
update admin_menu set name = '支付管理' where path = '/payments';
update admin_menu set name = '字典管理' where path = '/settings/dictionaries';

insert into admin_role_permission(role_id, permission_id)
select r.id, p.id
from admin_role r
join permission p on p.perm_code in (
    'waybill:view',
    'waybill:edit',
    'dict:view',
    'dict:edit',
    'member:view',
    'member:edit',
    'payment:view',
    'payment:edit'
)
where r.role_code = 'super_admin'
on duplicate key update role_id = values(role_id);

insert into admin_role_menu(role_id, menu_id)
select r.id, m.id
from admin_role r
join admin_menu m on m.path in (
    '/waybills',
    '/members',
    '/payments',
    '/settings/dictionaries'
)
where r.role_code = 'super_admin'
on duplicate key update role_id = values(role_id);
