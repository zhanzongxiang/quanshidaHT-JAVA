create table if not exists tenant (
    id bigint primary key auto_increment,
    tenant_code varchar(64) not null,
    tenant_name varchar(128) not null,
    status varchar(32) not null default 'ACTIVE',
    timezone varchar(64) not null default 'Asia/Shanghai',
    locale varchar(32) not null default 'zh-CN',
    remark varchar(500) null,
    deleted tinyint not null default 0,
    created_at datetime not null default current_timestamp,
    updated_at datetime not null default current_timestamp on update current_timestamp,
    unique key uk_tenant_code(tenant_code)
);

create table if not exists tenant_domain (
    id bigint primary key auto_increment,
    tenant_id bigint not null,
    domain varchar(255) not null,
    domain_type varchar(32) not null default 'website',
    enabled tinyint not null default 1,
    created_at datetime not null default current_timestamp,
    unique key uk_tenant_domain(domain),
    key idx_tenant_domain_tenant(tenant_id)
);

create table if not exists tenant_app (
    id bigint primary key auto_increment,
    tenant_id bigint not null,
    app_type varchar(32) not null,
    app_code varchar(64) not null,
    wechat_app_id varchar(64) null,
    wechat_app_secret varchar(255) null,
    status varchar(32) not null default 'ACTIVE',
    created_at datetime not null default current_timestamp,
    updated_at datetime not null default current_timestamp on update current_timestamp,
    unique key uk_tenant_app_code(tenant_id, app_type, app_code),
    key idx_tenant_app_wechat_app_id(wechat_app_id)
);

create table if not exists tenant_setting (
    id bigint primary key auto_increment,
    tenant_id bigint not null,
    setting_group varchar(64) not null,
    setting_key varchar(64) not null,
    setting_value longtext null,
    created_at datetime not null default current_timestamp,
    updated_at datetime not null default current_timestamp on update current_timestamp,
    unique key uk_tenant_setting(tenant_id, setting_group, setting_key)
);

insert into tenant(tenant_code, tenant_name, status, timezone, locale, remark)
values ('default', 'Default Tenant', 'ACTIVE', 'Asia/Shanghai', 'zh-CN', 'Migrated default tenant')
on duplicate key update
    tenant_name = values(tenant_name),
    status = values(status),
    timezone = values(timezone),
    locale = values(locale),
    remark = values(remark);

alter table admin_user
    add column tenant_id bigint null after username;

update admin_user
set tenant_id = (select id from tenant where tenant_code = 'default' limit 1)
where tenant_id is null;

create index idx_admin_user_tenant_id on admin_user(tenant_id);

alter table admin_user
    modify column tenant_id bigint not null after username;

insert into permission(perm_code, perm_name)
values ('tenant:view', 'View tenants'),
       ('tenant:edit', 'Edit tenants')
on duplicate key update perm_name = values(perm_name);

insert into admin_role_permission(role_id, permission_id)
select r.id, p.id
from admin_role r
join permission p on p.perm_code in ('tenant:view', 'tenant:edit')
where r.role_code = 'super_admin'
on duplicate key update role_id = values(role_id);
