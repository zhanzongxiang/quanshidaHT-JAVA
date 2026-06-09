alter table pay_merchant_config
    add column tenant_id bigint null after id;

update pay_merchant_config
set tenant_id = (select id from tenant where tenant_code = 'default' limit 1)
where tenant_id is null;

create index idx_pay_merchant_config_tenant_id on pay_merchant_config(tenant_id);

alter table pay_merchant_config
    modify column tenant_id bigint not null after id;

alter table pay_order
    add column tenant_id bigint null after id;

update pay_order
set tenant_id = (select id from tenant where tenant_code = 'default' limit 1)
where tenant_id is null;

create index idx_pay_order_tenant_id on pay_order(tenant_id);

alter table pay_order
    modify column tenant_id bigint not null after id;

alter table pay_transaction
    add column tenant_id bigint null after id;

update pay_transaction pt
join pay_order po on po.id = pt.pay_order_id
set pt.tenant_id = po.tenant_id
where pt.tenant_id is null;

create index idx_pay_transaction_tenant_id on pay_transaction(tenant_id);

alter table pay_transaction
    modify column tenant_id bigint not null after id;

alter table refund_order
    add column tenant_id bigint null after id;

update refund_order ro
join pay_order po on po.id = ro.pay_order_id
set ro.tenant_id = po.tenant_id
where ro.tenant_id is null;

create index idx_refund_order_tenant_id on refund_order(tenant_id);

alter table refund_order
    modify column tenant_id bigint not null after id;

alter table pay_notify_log
    add column tenant_id bigint null after id;

update pay_notify_log pnl
join pay_order po on po.id = pnl.pay_order_id
set pnl.tenant_id = po.tenant_id
where pnl.tenant_id is null;

update pay_notify_log
set tenant_id = (select id from tenant where tenant_code = 'default' limit 1)
where tenant_id is null;

create index idx_pay_notify_log_tenant_id on pay_notify_log(tenant_id);

alter table pay_notify_log
    modify column tenant_id bigint not null after id;

alter table refund_notify_log
    add column tenant_id bigint null after id;

update refund_notify_log rnl
join refund_order ro on ro.id = rnl.refund_order_id
set rnl.tenant_id = ro.tenant_id
where rnl.tenant_id is null;

update refund_notify_log
set tenant_id = (select id from tenant where tenant_code = 'default' limit 1)
where tenant_id is null;

create index idx_refund_notify_log_tenant_id on refund_notify_log(tenant_id);

alter table refund_notify_log
    modify column tenant_id bigint not null after id;

alter table pay_reconcile_record
    add column tenant_id bigint null after id;

update pay_reconcile_record
set tenant_id = (select id from tenant where tenant_code = 'default' limit 1)
where tenant_id is null;

create index idx_pay_reconcile_record_tenant_id on pay_reconcile_record(tenant_id);

alter table pay_reconcile_record
    modify column tenant_id bigint not null after id;

alter table pay_order
    drop index uk_pay_order_order_no,
    drop index idx_pay_order_member,
    drop index idx_pay_order_waybill,
    drop index idx_pay_order_status,
    drop index idx_pay_order_channel,
    drop index idx_pay_order_merchant_config,
    drop index idx_pay_order_merchant_mch_id,
    add unique key uk_pay_order_tenant_order_no(tenant_id, order_no),
    add key idx_pay_order_tenant_member(tenant_id, member_id),
    add key idx_pay_order_tenant_waybill(tenant_id, waybill_id),
    add key idx_pay_order_tenant_status(tenant_id, status),
    add key idx_pay_order_tenant_channel(tenant_id, channel),
    add key idx_pay_order_tenant_merchant_config(tenant_id, merchant_config_id),
    add key idx_pay_order_tenant_merchant_mch_id(tenant_id, merchant_mch_id);

alter table pay_transaction
    drop index idx_pay_transaction_order,
    drop index idx_pay_transaction_ext_trade_no,
    add key idx_pay_transaction_tenant_order(tenant_id, pay_order_id),
    add key idx_pay_transaction_tenant_ext_trade_no(tenant_id, external_transaction_no);

alter table refund_order
    drop index uk_refund_order_refund_no,
    drop index idx_refund_order_pay_order,
    drop index idx_refund_order_status,
    add unique key uk_refund_order_tenant_refund_no(tenant_id, refund_no),
    add key idx_refund_order_tenant_pay_order(tenant_id, pay_order_id),
    add key idx_refund_order_tenant_status(tenant_id, status);

alter table pay_notify_log
    drop index idx_pay_notify_order,
    drop index idx_pay_notify_type,
    add key idx_pay_notify_tenant_order(tenant_id, pay_order_id),
    add key idx_pay_notify_tenant_type(tenant_id, notify_type);

alter table refund_notify_log
    drop index idx_refund_notify_order,
    drop index idx_refund_notify_type,
    add key idx_refund_notify_tenant_order(tenant_id, refund_order_id),
    add key idx_refund_notify_tenant_type(tenant_id, notify_type);

alter table pay_reconcile_record
    drop index uk_pay_reconcile_date_channel,
    add unique key uk_pay_reconcile_tenant_date_channel(tenant_id, reconcile_date, channel);

alter table pay_merchant_config
    drop index uk_pay_merchant_config_code,
    drop index uk_pay_merchant_config_mch_id,
    drop index idx_pay_merchant_config_active,
    drop index idx_pay_merchant_config_enabled,
    add unique key uk_pay_merchant_config_tenant_code(tenant_id, merchant_code),
    add unique key uk_pay_merchant_config_tenant_mch_id(tenant_id, mch_id),
    add key idx_pay_merchant_config_tenant_active(tenant_id, active),
    add key idx_pay_merchant_config_tenant_enabled(tenant_id, enabled);
