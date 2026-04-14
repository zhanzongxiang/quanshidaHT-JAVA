create table if not exists waybill_order (
    id bigint primary key auto_increment,
    main_tracking_no varchar(64) not null,
    reference_no varchar(64) null,
    customer_name varchar(64) not null,
    customer_phone varchar(32) null,
    origin_warehouse varchar(128) null,
    destination_country varchar(64) not null,
    destination_city varchar(64) null,
    route_type varchar(32) not null default 'direct',
    current_status varchar(32) not null default 'created',
    current_node varchar(128) null,
    cargo_description varchar(255) null,
    package_count int not null default 1,
    weight_kg decimal(10, 2) null,
    remark varchar(500) null,
    deleted tinyint not null default 0,
    created_at datetime not null default current_timestamp,
    updated_at datetime not null default current_timestamp on update current_timestamp,
    unique key uk_waybill_order_main_tracking_no(main_tracking_no),
    key idx_waybill_order_status(current_status),
    key idx_waybill_order_updated(updated_at)
);

create table if not exists waybill_leg (
    id bigint primary key auto_increment,
    waybill_id bigint not null,
    leg_no int not null,
    leg_type varchar(32) not null,
    carrier_name varchar(64) null,
    tracking_no varchar(64) not null,
    from_node varchar(128) null,
    to_node varchar(128) null,
    leg_status varchar(32) not null default 'pending',
    transfer_flag tinyint not null default 0,
    departure_time datetime null,
    arrival_time datetime null,
    remark varchar(500) null,
    created_at datetime not null default current_timestamp,
    updated_at datetime not null default current_timestamp on update current_timestamp,
    unique key uk_waybill_leg_tracking_no(tracking_no),
    unique key uk_waybill_leg_waybill_leg_no(waybill_id, leg_no),
    key idx_waybill_leg_waybill_id(waybill_id)
);

create table if not exists waybill_track_event (
    id bigint primary key auto_increment,
    waybill_id bigint not null,
    leg_id bigint null,
    event_time datetime not null,
    event_status varchar(64) not null,
    event_description varchar(255) not null,
    event_location varchar(128) null,
    visible_to_customer tinyint not null default 1,
    sort_no int not null default 0,
    created_at datetime not null default current_timestamp,
    key idx_waybill_track_event_waybill_id(waybill_id),
    key idx_waybill_track_event_time(waybill_id, event_time, sort_no)
);

insert into permission(perm_code, perm_name)
values ('waybill:view', 'View waybills'),
       ('waybill:edit', 'Edit waybills')
on duplicate key update perm_name = values(perm_name);

set @pages_parent_id := (select id from admin_menu where path = '/pages' limit 1);

insert into admin_menu(parent_id, name, path, component, icon, sort_no)
select @pages_parent_id, 'Waybills', '/waybills', 'Waybill', 'Van', 24
from dual
where not exists (select 1 from admin_menu where path = '/waybills');

insert into admin_role_permission(role_id, permission_id)
select r.id, p.id
from admin_role r
join permission p on p.perm_code in ('waybill:view', 'waybill:edit')
where r.role_code = 'super_admin'
on duplicate key update role_id = values(role_id);

insert into admin_role_menu(role_id, menu_id)
select r.id, m.id
from admin_role r
join admin_menu m on m.path = '/waybills'
where r.role_code = 'super_admin'
on duplicate key update role_id = values(role_id);
