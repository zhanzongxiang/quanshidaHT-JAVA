create table if not exists member_user (
    id bigint primary key auto_increment,
    phone varchar(32) not null,
    password_hash varchar(255) not null,
    nickname varchar(64) null,
    full_name varchar(64) null,
    avatar_url varchar(500) null,
    status varchar(32) not null default 'active',
    remark varchar(500) null,
    last_login_at datetime null,
    deleted tinyint not null default 0,
    created_at datetime not null default current_timestamp,
    updated_at datetime not null default current_timestamp on update current_timestamp,
    unique key uk_member_user_phone(phone),
    key idx_member_user_status(status)
);

create table if not exists member_waybill_relation (
    id bigint primary key auto_increment,
    member_id bigint not null,
    waybill_id bigint not null,
    created_at datetime not null default current_timestamp,
    unique key uk_member_waybill(member_id, waybill_id),
    key idx_member_waybill_member(member_id),
    key idx_member_waybill_waybill(waybill_id)
);

alter table waybill_order add column member_id bigint null after destination_city;
create index idx_waybill_order_member_id on waybill_order(member_id);

insert into sys_dict_item(dict_type, dict_name, item_label, item_value, sort_no, enabled, builtin, remark)
values
    ('member_status', 'Member Status', 'Active', 'active', 10, 1, 1, 'Member can log in'),
    ('member_status', 'Member Status', 'Pending', 'pending', 20, 1, 1, 'Member is waiting for approval'),
    ('member_status', 'Member Status', 'Disabled', 'disabled', 30, 1, 1, 'Member cannot log in')
on duplicate key update
    dict_name = values(dict_name),
    item_label = values(item_label),
    sort_no = values(sort_no),
    enabled = values(enabled),
    builtin = values(builtin),
    remark = values(remark);

insert into permission(perm_code, perm_name)
values ('member:view', 'View members'),
       ('member:edit', 'Edit members')
on duplicate key update perm_name = values(perm_name);

insert into admin_menu(parent_id, name, path, component, icon, sort_no)
select 0, 'Members', '/members', 'Members', 'User', 25
from dual
where not exists (select 1 from admin_menu where path = '/members');

insert into admin_role_permission(role_id, permission_id)
select r.id, p.id
from admin_role r
join permission p on p.perm_code in ('member:view', 'member:edit')
where r.role_code = 'super_admin'
on duplicate key update role_id = values(role_id);

insert into admin_role_menu(role_id, menu_id)
select r.id, m.id
from admin_role r
join admin_menu m on m.path = '/members'
where r.role_code = 'super_admin'
on duplicate key update role_id = values(role_id);
