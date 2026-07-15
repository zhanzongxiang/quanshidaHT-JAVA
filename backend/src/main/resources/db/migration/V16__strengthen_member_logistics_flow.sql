alter table member_shipment
    add column waybill_id bigint null after address_id,
    add column recipient_name varchar(64) null after total_weight,
    add column recipient_phone varchar(32) null after recipient_name,
    add column destination_country varchar(64) null after recipient_phone,
    add column destination_province varchar(64) null after destination_country,
    add column destination_city varchar(64) null after destination_province,
    add column destination_district varchar(64) null after destination_city,
    add column destination_address varchar(255) null after destination_district,
    add column postal_code varchar(32) null after destination_address,
    add key idx_member_shipment_waybill_id(waybill_id);

insert into sys_dict_item(dict_type, dict_name, item_label, item_value, sort_no, enabled, builtin, remark)
values
    ('waybill_status', '运单状态', '已取消', 'cancelled', 100, 1, 1, '运单和轨迹状态'),
    ('waybill_leg_status', '运单分段状态', '已取消', 'cancelled', 50, 1, 1, '运单分段状态')
on duplicate key update
    item_label = values(item_label),
    sort_no = values(sort_no),
    enabled = values(enabled),
    builtin = values(builtin),
    remark = values(remark);
