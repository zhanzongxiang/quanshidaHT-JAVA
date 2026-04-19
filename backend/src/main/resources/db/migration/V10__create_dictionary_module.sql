create table if not exists sys_dict_item (
    id bigint primary key auto_increment,
    dict_type varchar(64) not null,
    dict_name varchar(64) not null,
    item_label varchar(64) not null,
    item_value varchar(64) not null,
    sort_no int not null default 0,
    enabled tinyint not null default 1,
    builtin tinyint not null default 0,
    remark varchar(255) null,
    created_at datetime not null default current_timestamp,
    updated_at datetime not null default current_timestamp on update current_timestamp,
    unique key uk_sys_dict_item_type_value(dict_type, item_value),
    key idx_sys_dict_item_type_sort(dict_type, sort_no, id)
);

insert into sys_dict_item(dict_type, dict_name, item_label, item_value, sort_no, enabled, builtin, remark)
values
    ('waybill_route_type', '运单线路类型', '直达', 'direct', 10, 1, 1, '主运单线路类型'),
    ('waybill_route_type', '运单线路类型', '中转', 'transfer', 20, 1, 1, '主运单线路类型'),
    ('waybill_status', '运单状态', '已创建', 'created', 10, 1, 1, '运单和轨迹状态'),
    ('waybill_status', '运单状态', '已收货', 'received', 20, 1, 1, '运单和轨迹状态'),
    ('waybill_status', '运单状态', '处理中', 'processing', 30, 1, 1, '运单和轨迹状态'),
    ('waybill_status', '运单状态', '运输中', 'in_transit', 40, 1, 1, '运单和轨迹状态'),
    ('waybill_status', '运单状态', '中转中', 'transferred', 50, 1, 1, '运单和轨迹状态'),
    ('waybill_status', '运单状态', '清关中', 'customs_clearance', 60, 1, 1, '运单和轨迹状态'),
    ('waybill_status', '运单状态', '派送中', 'delivering', 70, 1, 1, '运单和轨迹状态'),
    ('waybill_status', '运单状态', '已签收', 'signed', 80, 1, 1, '运单和轨迹状态'),
    ('waybill_status', '运单状态', '运输异常', 'exception', 90, 1, 1, '运单和轨迹状态'),
    ('waybill_leg_status', '运单分段状态', '待处理', 'pending', 10, 1, 1, '运单分段状态'),
    ('waybill_leg_status', '运单分段状态', '运输中', 'in_transit', 20, 1, 1, '运单分段状态'),
    ('waybill_leg_status', '运单分段状态', '已到达', 'arrived', 30, 1, 1, '运单分段状态'),
    ('waybill_leg_status', '运单分段状态', '运输异常', 'exception', 40, 1, 1, '运单分段状态')
on duplicate key update
    dict_name = values(dict_name),
    item_label = values(item_label),
    sort_no = values(sort_no),
    enabled = values(enabled),
    builtin = values(builtin),
    remark = values(remark);

insert into permission(perm_code, perm_name)
values ('dict:view', '查看字典'),
       ('dict:edit', '编辑字典')
on duplicate key update perm_name = values(perm_name);

set @settings_parent_id := (select id from admin_menu where path = '/settings' limit 1);

insert into admin_menu(parent_id, name, path, component, icon, sort_no)
select @settings_parent_id, '字典管理', '/settings/dictionaries', 'DictionarySettings', 'Collection', 34
from dual
where not exists (select 1 from admin_menu where path = '/settings/dictionaries');

insert into admin_role_permission(role_id, permission_id)
select r.id, p.id
from admin_role r
join permission p on p.perm_code in ('dict:view', 'dict:edit')
where r.role_code = 'super_admin'
on duplicate key update role_id = values(role_id);

insert into admin_role_menu(role_id, menu_id)
select r.id, m.id
from admin_role r
join admin_menu m on m.path = '/settings/dictionaries'
where r.role_code = 'super_admin'
on duplicate key update role_id = values(role_id);
