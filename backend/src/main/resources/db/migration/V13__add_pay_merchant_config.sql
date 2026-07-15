create table if not exists pay_merchant_config (
    id bigint primary key auto_increment,
    merchant_name varchar(64) not null,
    merchant_code varchar(64) not null,
    mch_id varchar(64) not null,
    app_id varchar(64) not null,
    notify_url varchar(255) not null,
    api_v3_key varchar(128) null,
    private_key_path varchar(255) null,
    merchant_serial_no varchar(128) null,
    platform_certificate_path varchar(255) null,
    enabled tinyint not null default 1,
    active tinyint not null default 0,
    remark varchar(500) null,
    deleted tinyint not null default 0,
    created_at datetime not null default current_timestamp,
    updated_at datetime not null default current_timestamp on update current_timestamp,
    unique key uk_pay_merchant_config_code(merchant_code),
    unique key uk_pay_merchant_config_mch_id(mch_id),
    key idx_pay_merchant_config_active(active),
    key idx_pay_merchant_config_enabled(enabled)
);

alter table pay_order
    add column merchant_config_id bigint null after member_id,
    add column merchant_name varchar(64) null after merchant_config_id,
    add column merchant_mch_id varchar(64) null after merchant_name,
    add column merchant_app_id varchar(64) null after merchant_mch_id;

create index idx_pay_order_merchant_config on pay_order(merchant_config_id);
create index idx_pay_order_merchant_mch_id on pay_order(merchant_mch_id);

insert into pay_merchant_config(
    merchant_name,
    merchant_code,
    mch_id,
    app_id,
    notify_url,
    enabled,
    active,
    remark,
    deleted
)
select
    'Default Merchant',
    'default_merchant',
    'demo-mch',
    'wx-demo-miniapp',
    'http://localhost:8080/api/payment/callback/wechat',
    1,
    1,
    'Migrated default merchant configuration',
    0
from dual
where not exists (
    select 1
    from pay_merchant_config
    where deleted = 0
);

update pay_order po
join (
    select id, merchant_name, mch_id, app_id
    from pay_merchant_config
    where deleted = 0
    order by active desc, id asc
    limit 1
) pmc on 1 = 1
set po.merchant_config_id = coalesce(po.merchant_config_id, pmc.id),
    po.merchant_name = coalesce(po.merchant_name, pmc.merchant_name),
    po.merchant_mch_id = coalesce(po.merchant_mch_id, pmc.mch_id),
    po.merchant_app_id = coalesce(po.merchant_app_id, pmc.app_id)
where po.deleted = 0;

insert into permission(perm_code, perm_name)
values ('payment:merchant', 'Manage payment merchants')
on duplicate key update perm_name = values(perm_name);

insert into admin_role_permission(role_id, permission_id)
select r.id, p.id
from admin_role r
join permission p on p.perm_code = 'payment:merchant'
where r.role_code = 'super_admin'
on duplicate key update role_id = values(role_id);
