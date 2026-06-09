alter table member_user
    add column tenant_id bigint null after id;

update member_user
set tenant_id = (select id from tenant where tenant_code = 'default' limit 1)
where tenant_id is null;

create index idx_member_user_tenant_id on member_user(tenant_id);

alter table member_user
    modify column tenant_id bigint not null after id;

alter table member_waybill_relation
    add column tenant_id bigint null after id;

update member_waybill_relation mwr
join member_user mu on mu.id = mwr.member_id
set mwr.tenant_id = mu.tenant_id
where mwr.tenant_id is null;

create index idx_member_waybill_relation_tenant_id on member_waybill_relation(tenant_id);

alter table member_waybill_relation
    modify column tenant_id bigint not null after id;

alter table member_user
    drop index uk_member_user_phone,
    drop index uk_member_user_wechat_openid,
    drop index idx_member_user_status,
    add unique key uk_member_user_tenant_phone(tenant_id, phone),
    add unique key uk_member_user_tenant_wechat_openid(tenant_id, wechat_openid),
    add key idx_member_user_tenant_status(tenant_id, status);

alter table member_waybill_relation
    drop index uk_member_waybill,
    drop index idx_member_waybill_member,
    drop index idx_member_waybill_waybill,
    add unique key uk_member_waybill_tenant_member_waybill(tenant_id, member_id, waybill_id),
    add key idx_member_waybill_tenant_member(tenant_id, member_id),
    add key idx_member_waybill_tenant_waybill(tenant_id, waybill_id);
