insert into permission(perm_code, perm_name)
values ('member:view', 'View member logistics operations'),
       ('member:operate', 'Operate member logistics operations')
on duplicate key update perm_name = values(perm_name);

set @pages_parent_id := (select id from admin_menu where path = '/pages' limit 1);

insert into admin_menu(parent_id, name, path, component, icon, sort_no)
select @pages_parent_id, '会员物流运营', '/member-operations', 'MemberOperations', 'Box', 25
from dual
where not exists (select 1 from admin_menu where path = '/member-operations');

insert into admin_role_permission(role_id, permission_id)
select r.id, p.id
from admin_role r
join permission p on p.perm_code in ('member:view', 'member:operate')
where r.role_code = 'super_admin'
on duplicate key update role_id = values(role_id);

insert into admin_role_menu(role_id, menu_id)
select r.id, m.id
from admin_role r
join admin_menu m on m.path = '/member-operations'
where r.role_code = 'super_admin'
on duplicate key update role_id = values(role_id);
