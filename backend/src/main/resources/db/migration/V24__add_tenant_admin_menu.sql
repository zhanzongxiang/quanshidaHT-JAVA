insert into admin_menu(parent_id, name, path, component, icon, sort_no)
select 0, 'Tenants', '/tenants', 'Tenants', 'OfficeBuilding', 27
from dual
where not exists (select 1 from admin_menu where path = '/tenants');

insert into admin_role_menu(role_id, menu_id)
select r.id, m.id
from admin_role r
join admin_menu m on m.path = '/tenants'
where r.role_code = 'super_admin'
on duplicate key update role_id = values(role_id);
