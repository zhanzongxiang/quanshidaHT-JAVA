alter table member_user
    add column wechat_openid varchar(64) null after phone,
    add column wechat_unionid varchar(64) null after wechat_openid,
    add column wechat_bind_time datetime null after wechat_unionid;

create unique index uk_member_user_wechat_openid on member_user(wechat_openid);

create table if not exists pay_order (
    id bigint primary key auto_increment,
    order_no varchar(64) not null,
    member_id bigint not null,
    waybill_id bigint null,
    business_type varchar(32) not null default 'waybill',
    scene_type varchar(32) not null default 'mini_program',
    channel varchar(32) not null default 'wechat_pay',
    currency varchar(16) not null default 'CNY',
    amount_total decimal(12, 2) not null default 0.00,
    amount_paid decimal(12, 2) not null default 0.00,
    status varchar(32) not null default 'pending',
    description varchar(255) null,
    external_transaction_no varchar(64) null,
    paid_at datetime null,
    expired_at datetime null,
    closed_at datetime null,
    refunded_at datetime null,
    remark varchar(500) null,
    deleted tinyint not null default 0,
    created_at datetime not null default current_timestamp,
    updated_at datetime not null default current_timestamp on update current_timestamp,
    unique key uk_pay_order_order_no(order_no),
    key idx_pay_order_member(member_id),
    key idx_pay_order_waybill(waybill_id),
    key idx_pay_order_status(status),
    key idx_pay_order_channel(channel)
);

create table if not exists pay_transaction (
    id bigint primary key auto_increment,
    pay_order_id bigint not null,
    transaction_type varchar(32) not null,
    transaction_status varchar(32) not null,
    request_payload text null,
    response_payload text null,
    external_transaction_no varchar(64) null,
    external_out_trade_no varchar(64) null,
    success_time datetime null,
    created_at datetime not null default current_timestamp,
    key idx_pay_transaction_order(pay_order_id),
    key idx_pay_transaction_ext_trade_no(external_transaction_no)
);

create table if not exists refund_order (
    id bigint primary key auto_increment,
    refund_no varchar(64) not null,
    pay_order_id bigint not null,
    amount_refund decimal(12, 2) not null default 0.00,
    status varchar(32) not null default 'pending',
    reason varchar(255) null,
    external_refund_no varchar(64) null,
    refunded_at datetime null,
    created_at datetime not null default current_timestamp,
    updated_at datetime not null default current_timestamp on update current_timestamp,
    unique key uk_refund_order_refund_no(refund_no),
    key idx_refund_order_pay_order(pay_order_id),
    key idx_refund_order_status(status)
);

create table if not exists pay_notify_log (
    id bigint primary key auto_increment,
    pay_order_id bigint null,
    notify_type varchar(32) not null,
    resource_id varchar(128) null,
    notify_status varchar(32) not null default 'received',
    raw_payload text null,
    process_result text null,
    notified_at datetime null,
    created_at datetime not null default current_timestamp,
    key idx_pay_notify_order(pay_order_id),
    key idx_pay_notify_type(notify_type)
);

create table if not exists refund_notify_log (
    id bigint primary key auto_increment,
    refund_order_id bigint null,
    notify_type varchar(32) not null,
    resource_id varchar(128) null,
    notify_status varchar(32) not null default 'received',
    raw_payload text null,
    process_result text null,
    notified_at datetime null,
    created_at datetime not null default current_timestamp,
    key idx_refund_notify_order(refund_order_id),
    key idx_refund_notify_type(notify_type)
);

create table if not exists pay_reconcile_record (
    id bigint primary key auto_increment,
    reconcile_date date not null,
    channel varchar(32) not null default 'wechat_pay',
    reconcile_status varchar(32) not null default 'pending',
    diff_count int not null default 0,
    summary text null,
    created_at datetime not null default current_timestamp,
    updated_at datetime not null default current_timestamp on update current_timestamp,
    unique key uk_pay_reconcile_date_channel(reconcile_date, channel)
);

insert into sys_dict_item(dict_type, dict_name, item_label, item_value, sort_no, enabled, builtin, remark)
values
    ('pay_order_status', 'Pay Order Status', 'Pending', 'pending', 10, 1, 1, 'Created but not paid'),
    ('pay_order_status', 'Pay Order Status', 'Paying', 'paying', 20, 1, 1, 'Waiting for pay confirmation'),
    ('pay_order_status', 'Pay Order Status', 'Paid', 'paid', 30, 1, 1, 'Payment completed'),
    ('pay_order_status', 'Pay Order Status', 'Refunding', 'refunding', 40, 1, 1, 'Refund is processing'),
    ('pay_order_status', 'Pay Order Status', 'Refunded', 'refunded', 50, 1, 1, 'Refund completed'),
    ('pay_order_status', 'Pay Order Status', 'Closed', 'closed', 60, 1, 1, 'Order closed'),
    ('pay_order_status', 'Pay Order Status', 'Exception', 'exception', 70, 1, 1, 'Payment exception'),
    ('pay_channel', 'Pay Channel', 'WeChat Pay', 'wechat_pay', 10, 1, 1, 'WeChat Pay channel'),
    ('refund_status', 'Refund Status', 'Pending', 'pending', 10, 1, 1, 'Refund pending'),
    ('refund_status', 'Refund Status', 'Processing', 'processing', 20, 1, 1, 'Refund processing'),
    ('refund_status', 'Refund Status', 'Succeeded', 'succeeded', 30, 1, 1, 'Refund succeeded'),
    ('refund_status', 'Refund Status', 'Failed', 'failed', 40, 1, 1, 'Refund failed'),
    ('reconcile_status', 'Reconcile Status', 'Pending', 'pending', 10, 1, 1, 'Reconcile pending'),
    ('reconcile_status', 'Reconcile Status', 'Matched', 'matched', 20, 1, 1, 'Reconcile matched'),
    ('reconcile_status', 'Reconcile Status', 'Diff Found', 'diff_found', 30, 1, 1, 'Reconcile diff found')
on duplicate key update
    dict_name = values(dict_name),
    item_label = values(item_label),
    sort_no = values(sort_no),
    enabled = values(enabled),
    builtin = values(builtin),
    remark = values(remark);

insert into permission(perm_code, perm_name)
values ('payment:view', 'View payments'),
       ('payment:edit', 'Manage payments')
on duplicate key update perm_name = values(perm_name);

insert into admin_menu(parent_id, name, path, component, icon, sort_no)
select 0, 'Payments', '/payments', 'Payments', 'CreditCard', 26
from dual
where not exists (select 1 from admin_menu where path = '/payments');

insert into admin_role_permission(role_id, permission_id)
select r.id, p.id
from admin_role r
join permission p on p.perm_code in ('payment:view', 'payment:edit')
where r.role_code = 'super_admin'
on duplicate key update role_id = values(role_id);

insert into admin_role_menu(role_id, menu_id)
select r.id, m.id
from admin_role r
join admin_menu m on m.path = '/payments'
where r.role_code = 'super_admin'
on duplicate key update role_id = values(role_id);
