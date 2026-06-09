alter table waybill_order
    add column tenant_id bigint null after id;

update waybill_order
set tenant_id = (select id from tenant where tenant_code = 'default' limit 1)
where tenant_id is null;

create index idx_waybill_order_tenant_id on waybill_order(tenant_id);

alter table waybill_order
    modify column tenant_id bigint not null after id;

alter table waybill_leg
    add column tenant_id bigint null after id;

update waybill_leg wl
join waybill_order wo on wo.id = wl.waybill_id
set wl.tenant_id = wo.tenant_id
where wl.tenant_id is null;

create index idx_waybill_leg_tenant_id on waybill_leg(tenant_id);

alter table waybill_leg
    modify column tenant_id bigint not null after id;

alter table waybill_track_event
    add column tenant_id bigint null after id;

update waybill_track_event wte
join waybill_order wo on wo.id = wte.waybill_id
set wte.tenant_id = wo.tenant_id
where wte.tenant_id is null;

create index idx_waybill_track_event_tenant_id on waybill_track_event(tenant_id);

alter table waybill_track_event
    modify column tenant_id bigint not null after id;

alter table waybill_order
    drop index uk_waybill_order_main_tracking_no,
    add unique key uk_waybill_order_tenant_main_tracking_no(tenant_id, main_tracking_no);

alter table waybill_leg
    drop index uk_waybill_leg_tracking_no,
    drop index uk_waybill_leg_waybill_leg_no,
    add unique key uk_waybill_leg_tenant_tracking_no(tenant_id, tracking_no),
    add unique key uk_waybill_leg_tenant_waybill_leg_no(tenant_id, waybill_id, leg_no);
