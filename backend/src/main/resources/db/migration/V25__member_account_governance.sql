alter table member_user
    add column register_source varchar(64) null after remark,
    add column register_ip varchar(64) null after register_source,
    add column last_login_ip varchar(64) null after last_login_at,
    add column password_updated_at datetime null after last_login_ip;

update member_user
set register_source = 'legacy'
where register_source is null or trim(register_source) = '';

update member_user
set password_updated_at = coalesce(updated_at, created_at)
where password_updated_at is null;

alter table member_user
    modify column register_source varchar(64) not null;

create index idx_member_user_tenant_register_source on member_user(tenant_id, register_source);

create table if not exists member_audit_log (
    id bigint primary key auto_increment,
    tenant_id bigint not null,
    member_id bigint not null,
    action_type varchar(64) not null,
    operator_type varchar(32) not null,
    operator_id bigint null,
    operator_label varchar(128) not null,
    summary varchar(255) not null,
    detail_json text null,
    created_at datetime not null default current_timestamp,
    key idx_member_audit_log_tenant_member_created(tenant_id, member_id, created_at),
    key idx_member_audit_log_tenant_action(tenant_id, action_type)
);
