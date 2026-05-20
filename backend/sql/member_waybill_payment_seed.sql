SET NAMES utf8mb4;

-- 会员 / 运单 / 支付 联调示例数据
-- 依赖：
-- 1. 已执行会员、运单、支付、商户相关迁移
-- 2. 建议先执行 backend/sql/member_seed.sql
-- 3. 当前示例以 member_user.id = 10001 / 10002 / 10003 为基准

INSERT INTO pay_merchant_config (
    id,
    merchant_name,
    merchant_code,
    mch_id,
    app_id,
    app_secret,
    notify_url,
    api_v3_key,
    private_key_path,
    merchant_serial_no,
    platform_certificate_path,
    enabled,
    active,
    remark,
    deleted,
    created_at,
    updated_at
) VALUES (
    10001,
    '联调测试商户',
    'debug_merchant',
    '1900000109',
    'wxdebugminiapp001',
    'debug-app-secret',
    'http://localhost:8080/api/payment/callback/wechat',
    'debug-api-v3-key',
    'D:/wechat-pay/apiclient_key.pem',
    'DEBUG-SERIAL-001',
    'D:/wechat-pay/platform_cert.pem',
    1,
    1,
    '本地联调用默认商户，可直接用于支付列表和详情展示',
    0,
    '2026-05-19 09:00:00',
    '2026-05-20 09:30:00'
)
ON DUPLICATE KEY UPDATE
    merchant_name = VALUES(merchant_name),
    merchant_code = VALUES(merchant_code),
    mch_id = VALUES(mch_id),
    app_id = VALUES(app_id),
    app_secret = VALUES(app_secret),
    notify_url = VALUES(notify_url),
    api_v3_key = VALUES(api_v3_key),
    private_key_path = VALUES(private_key_path),
    merchant_serial_no = VALUES(merchant_serial_no),
    platform_certificate_path = VALUES(platform_certificate_path),
    enabled = VALUES(enabled),
    active = VALUES(active),
    remark = VALUES(remark),
    deleted = VALUES(deleted),
    updated_at = VALUES(updated_at);

UPDATE pay_merchant_config
SET active = CASE WHEN id = 10001 THEN 1 ELSE 0 END,
    updated_at = '2026-05-20 09:30:00'
WHERE deleted = 0;

INSERT INTO waybill_order (
    id,
    main_tracking_no,
    reference_no,
    customer_name,
    customer_phone,
    origin_warehouse,
    destination_country,
    destination_city,
    member_id,
    route_type,
    current_status,
    current_node,
    cargo_description,
    package_count,
    weight_kg,
    remark,
    deleted,
    created_at,
    updated_at
) VALUES
    (
        10001,
        'QSDTW2026050001',
        'REF-TW-0001',
        '陈小雅',
        '13800000001',
        '深圳集运仓',
        '中国台湾',
        '台北市',
        10001,
        'direct',
        'delivering',
        '台北派送站',
        '服饰样品与文件',
        2,
        3.50,
        '会员已付款，适合联调已支付运单',
        0,
        '2026-05-18 09:00:00',
        '2026-05-20 08:40:00'
    ),
    (
        10002,
        'QSDAF2026050002',
        'REF-AF-0002',
        '李承运',
        '13800000002',
        '广州南沙仓',
        '肯尼亚',
        '内罗毕',
        10002,
        'transfer',
        'customs_clearance',
        '内罗毕清关中',
        '电子配件',
        1,
        1.20,
        '会员未支付，适合联调待支付运单',
        0,
        '2026-05-18 10:30:00',
        '2026-05-20 07:55:00'
    ),
    (
        10003,
        'QSDEX2026050003',
        'REF-EXP-0003',
        '王海东',
        '13800000003',
        '上海浦东仓',
        '新加坡',
        '新加坡',
        10003,
        'direct',
        'exception',
        '目的港异常待处理',
        '高值电子产品',
        1,
        2.00,
        '禁用会员历史异常运单，可验证边界场景',
        0,
        '2026-05-17 14:00:00',
        '2026-05-19 12:15:00'
    )
ON DUPLICATE KEY UPDATE
    reference_no = VALUES(reference_no),
    customer_name = VALUES(customer_name),
    customer_phone = VALUES(customer_phone),
    origin_warehouse = VALUES(origin_warehouse),
    destination_country = VALUES(destination_country),
    destination_city = VALUES(destination_city),
    member_id = VALUES(member_id),
    route_type = VALUES(route_type),
    current_status = VALUES(current_status),
    current_node = VALUES(current_node),
    cargo_description = VALUES(cargo_description),
    package_count = VALUES(package_count),
    weight_kg = VALUES(weight_kg),
    remark = VALUES(remark),
    deleted = VALUES(deleted),
    updated_at = VALUES(updated_at);

DELETE FROM member_waybill_relation
WHERE member_id IN (10001, 10002, 10003)
  AND waybill_id IN (10001, 10002, 10003);

INSERT INTO member_waybill_relation (
    id,
    member_id,
    waybill_id,
    created_at
) VALUES
    (10001, 10001, 10001, '2026-05-18 09:05:00'),
    (10002, 10002, 10002, '2026-05-18 10:35:00'),
    (10003, 10003, 10003, '2026-05-17 14:05:00');

DELETE FROM waybill_leg
WHERE waybill_id IN (10001, 10002, 10003);

INSERT INTO waybill_leg (
    id,
    waybill_id,
    leg_no,
    leg_type,
    carrier_name,
    tracking_no,
    from_node,
    to_node,
    leg_status,
    transfer_flag,
    departure_time,
    arrival_time,
    remark,
    created_at,
    updated_at
) VALUES
    (
        10001,
        10001,
        1,
        'pickup',
        '全时达揽收',
        'QSDTW2026050001-1',
        '深圳集运仓',
        '台湾分拨中心',
        'arrived',
        0,
        '2026-05-18 12:00:00',
        '2026-05-19 09:30:00',
        '主干段已完成',
        '2026-05-18 12:00:00',
        '2026-05-19 09:30:00'
    ),
    (
        10002,
        10001,
        2,
        'delivery',
        '台湾末端派送',
        'QSDTW2026050001-2',
        '台湾分拨中心',
        '台北派送站',
        'in_transit',
        0,
        '2026-05-19 14:00:00',
        null,
        '末端派送中',
        '2026-05-19 14:00:00',
        '2026-05-20 08:40:00'
    ),
    (
        10003,
        10002,
        1,
        'air',
        '广州国际航线',
        'QSDAF2026050002-1',
        '广州南沙仓',
        '内罗毕国际机场',
        'arrived',
        1,
        '2026-05-18 18:20:00',
        '2026-05-19 23:10:00',
        '已到达目的港',
        '2026-05-18 18:20:00',
        '2026-05-19 23:10:00'
    ),
    (
        10004,
        10003,
        1,
        'air',
        '国际快递承运商',
        'QSDEX2026050003-1',
        '上海浦东仓',
        '新加坡转运站',
        'exception',
        0,
        '2026-05-17 20:00:00',
        null,
        '资料不全触发异常',
        '2026-05-17 20:00:00',
        '2026-05-19 12:15:00'
    );

DELETE FROM waybill_track_event
WHERE waybill_id IN (10001, 10002, 10003);

INSERT INTO waybill_track_event (
    id,
    waybill_id,
    leg_id,
    event_time,
    event_status,
    event_description,
    event_location,
    visible_to_customer,
    sort_no,
    created_at
) VALUES
    (10001, 10001, 10001, '2026-05-18 11:30:00', 'received', '货物已入深圳集运仓', '深圳', 1, 10, '2026-05-18 11:30:00'),
    (10002, 10001, 10001, '2026-05-19 09:30:00', 'in_transit', '干线运输完成，进入台湾分拨中心', '台北', 1, 20, '2026-05-19 09:30:00'),
    (10003, 10001, 10002, '2026-05-20 08:40:00', 'delivering', '末端派送中，预计今日送达', '台北市', 1, 30, '2026-05-20 08:40:00'),
    (10004, 10002, 10003, '2026-05-18 16:00:00', 'processing', '货物完成出库，等待航班起飞', '广州', 1, 10, '2026-05-18 16:00:00'),
    (10005, 10002, 10003, '2026-05-19 23:10:00', 'customs_clearance', '货物已到达目的港，等待清关', '内罗毕', 1, 20, '2026-05-19 23:10:00'),
    (10006, 10003, 10004, '2026-05-18 08:00:00', 'processing', '货物已发出，等待中转', '上海', 1, 10, '2026-05-18 08:00:00'),
    (10007, 10003, 10004, '2026-05-19 12:15:00', 'exception', '清关资料缺失，等待补件', '新加坡', 1, 20, '2026-05-19 12:15:00');

INSERT INTO pay_order (
    id,
    order_no,
    member_id,
    merchant_config_id,
    merchant_name,
    merchant_mch_id,
    merchant_app_id,
    waybill_id,
    business_type,
    scene_type,
    channel,
    currency,
    amount_total,
    amount_paid,
    status,
    description,
    external_transaction_no,
    paid_at,
    expired_at,
    closed_at,
    refunded_at,
    remark,
    deleted,
    created_at,
    updated_at
) VALUES
    (
        10001,
        'PO202605200001',
        10001,
        10001,
        '联调测试商户',
        '1900000109',
        'wxdebugminiapp001',
        10001,
        'waybill',
        'mini_program',
        'wechat_pay',
        'CNY',
        88.50,
        88.50,
        'paid',
        '运单支付 QSDTW2026050001',
        '420000250520260520000001',
        '2026-05-20 09:05:00',
        '2026-05-20 09:20:00',
        null,
        null,
        '已支付示例订单',
        0,
        '2026-05-20 09:00:00',
        '2026-05-20 09:05:00'
    ),
    (
        10002,
        'PO202605200002',
        10002,
        10001,
        '联调测试商户',
        '1900000109',
        'wxdebugminiapp001',
        10002,
        'waybill',
        'mini_program',
        'wechat_pay',
        'CNY',
        120.00,
        0.00,
        'pending',
        '运单支付 QSDAF2026050002',
        null,
        null,
        '2026-05-20 18:00:00',
        null,
        null,
        '待支付示例订单',
        0,
        '2026-05-20 08:50:00',
        '2026-05-20 08:50:00'
    ),
    (
        10003,
        'PO202605190003',
        10003,
        10001,
        '联调测试商户',
        '1900000109',
        'wxdebugminiapp001',
        10003,
        'waybill',
        'mini_program',
        'wechat_pay',
        'CNY',
        66.00,
        66.00,
        'refunded',
        '运单支付 QSDEX2026050003',
        '420000250519260519000003',
        '2026-05-19 10:10:00',
        '2026-05-19 10:30:00',
        null,
        '2026-05-20 11:20:00',
        '已退款示例订单',
        0,
        '2026-05-19 10:00:00',
        '2026-05-20 11:20:00'
    )
ON DUPLICATE KEY UPDATE
    member_id = VALUES(member_id),
    merchant_config_id = VALUES(merchant_config_id),
    merchant_name = VALUES(merchant_name),
    merchant_mch_id = VALUES(merchant_mch_id),
    merchant_app_id = VALUES(merchant_app_id),
    waybill_id = VALUES(waybill_id),
    business_type = VALUES(business_type),
    scene_type = VALUES(scene_type),
    channel = VALUES(channel),
    currency = VALUES(currency),
    amount_total = VALUES(amount_total),
    amount_paid = VALUES(amount_paid),
    status = VALUES(status),
    description = VALUES(description),
    external_transaction_no = VALUES(external_transaction_no),
    paid_at = VALUES(paid_at),
    expired_at = VALUES(expired_at),
    closed_at = VALUES(closed_at),
    refunded_at = VALUES(refunded_at),
    remark = VALUES(remark),
    deleted = VALUES(deleted),
    updated_at = VALUES(updated_at);

DELETE FROM pay_transaction
WHERE pay_order_id IN (10001, 10002, 10003);

INSERT INTO pay_transaction (
    id,
    pay_order_id,
    transaction_type,
    transaction_status,
    request_payload,
    response_payload,
    external_transaction_no,
    external_out_trade_no,
    success_time,
    created_at
) VALUES
    (
        10001,
        10001,
        'pay',
        'success',
        '{"orderNo":"PO202605200001","amountTotal":88.50}',
        '{"prepayId":"wx-prepay-10001","result":"success"}',
        '420000250520260520000001',
        'PO202605200001',
        '2026-05-20 09:05:00',
        '2026-05-20 09:00:30'
    ),
    (
        10002,
        10002,
        'pay',
        'pending',
        '{"orderNo":"PO202605200002","amountTotal":120.00}',
        '{"prepayId":"wx-prepay-10002","result":"pending"}',
        null,
        'PO202605200002',
        null,
        '2026-05-20 08:50:20'
    ),
    (
        10003,
        10003,
        'pay',
        'success',
        '{"orderNo":"PO202605190003","amountTotal":66.00}',
        '{"prepayId":"wx-prepay-10003","result":"success"}',
        '420000250519260519000003',
        'PO202605190003',
        '2026-05-19 10:10:00',
        '2026-05-19 10:00:20'
    );

INSERT INTO refund_order (
    id,
    refund_no,
    pay_order_id,
    amount_refund,
    status,
    reason,
    external_refund_no,
    refunded_at,
    created_at,
    updated_at
) VALUES
    (
        10001,
        'RF202605200001',
        10003,
        66.00,
        'succeeded',
        '客户取消发运',
        'REFUND-WX-10001',
        '2026-05-20 11:20:00',
        '2026-05-20 11:00:00',
        '2026-05-20 11:20:00'
    )
ON DUPLICATE KEY UPDATE
    pay_order_id = VALUES(pay_order_id),
    amount_refund = VALUES(amount_refund),
    status = VALUES(status),
    reason = VALUES(reason),
    external_refund_no = VALUES(external_refund_no),
    refunded_at = VALUES(refunded_at),
    updated_at = VALUES(updated_at);

DELETE FROM pay_notify_log
WHERE pay_order_id IN (10001, 10002, 10003);

INSERT INTO pay_notify_log (
    id,
    pay_order_id,
    notify_type,
    resource_id,
    notify_status,
    raw_payload,
    process_result,
    notified_at,
    created_at
) VALUES
    (
        10001,
        10001,
        'wechat_pay',
        'pay-resource-10001',
        'received',
        '{"orderNo":"PO202605200001","status":"paid"}',
        '支付回调处理成功',
        '2026-05-20 09:05:10',
        '2026-05-20 09:05:10'
    ),
    (
        10002,
        10003,
        'wechat_pay',
        'pay-resource-10003',
        'received',
        '{"orderNo":"PO202605190003","status":"paid"}',
        '支付回调处理成功',
        '2026-05-19 10:10:10',
        '2026-05-19 10:10:10'
    );

DELETE FROM refund_notify_log
WHERE refund_order_id = 10001;

INSERT INTO refund_notify_log (
    id,
    refund_order_id,
    notify_type,
    resource_id,
    notify_status,
    raw_payload,
    process_result,
    notified_at,
    created_at
) VALUES
    (
        10001,
        10001,
        'wechat_refund',
        'refund-resource-10001',
        'received',
        '{"refundNo":"RF202605200001","status":"succeeded"}',
        '退款回调处理成功',
        '2026-05-20 11:20:10',
        '2026-05-20 11:20:10'
    );

INSERT INTO pay_reconcile_record (
    id,
    reconcile_date,
    channel,
    reconcile_status,
    diff_count,
    summary,
    created_at,
    updated_at
) VALUES
    (
        10001,
        '2026-05-19',
        'wechat_pay',
        'matched',
        0,
        '5 月 19 日账单已核对完成，无差异',
        '2026-05-20 08:00:00',
        '2026-05-20 08:05:00'
    ),
    (
        10002,
        '2026-05-20',
        'wechat_pay',
        'diff_found',
        1,
        '存在 1 笔待复核订单，用于联调对账差异展示',
        '2026-05-20 12:00:00',
        '2026-05-20 12:10:00'
    )
ON DUPLICATE KEY UPDATE
    channel = VALUES(channel),
    reconcile_status = VALUES(reconcile_status),
    diff_count = VALUES(diff_count),
    summary = VALUES(summary),
    updated_at = VALUES(updated_at);

ALTER TABLE pay_merchant_config AUTO_INCREMENT = 10002;
ALTER TABLE waybill_order AUTO_INCREMENT = 10004;
ALTER TABLE member_waybill_relation AUTO_INCREMENT = 10004;
ALTER TABLE waybill_leg AUTO_INCREMENT = 10005;
ALTER TABLE waybill_track_event AUTO_INCREMENT = 10008;
ALTER TABLE pay_order AUTO_INCREMENT = 10004;
ALTER TABLE pay_transaction AUTO_INCREMENT = 10004;
ALTER TABLE refund_order AUTO_INCREMENT = 10002;
ALTER TABLE pay_notify_log AUTO_INCREMENT = 10003;
ALTER TABLE refund_notify_log AUTO_INCREMENT = 10002;
ALTER TABLE pay_reconcile_record AUTO_INCREMENT = 10003;
