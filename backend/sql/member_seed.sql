SET NAMES utf8mb4;

-- 会员系统示例数据
-- 说明：
-- 1. 当前项目会员密码为明文比对，示例密码可直接用于联调
-- 2. 可按手机号或微信绑定状态筛选查看管理端效果

INSERT INTO member_user (
    id,
    phone,
    wechat_openid,
    wechat_unionid,
    wechat_bind_time,
    password_hash,
    nickname,
    full_name,
    avatar_url,
    status,
    remark,
    last_login_at,
    deleted,
    created_at,
    updated_at
) VALUES
    (
        10001,
        '13800000001',
        'wx-openid-demo-10001',
        'wx-unionid-demo-10001',
        '2026-05-18 10:20:00',
        '123456',
        '陈小雅',
        '陈小雅',
        'https://example.com/avatar/member-10001.png',
        'active',
        '联调示例会员：已绑定微信，可直接测试支付和资料页',
        '2026-05-20 09:15:00',
        0,
        '2026-05-18 10:00:00',
        '2026-05-20 09:15:00'
    ),
    (
        10002,
        '13800000002',
        null,
        null,
        null,
        '123456',
        '李承运',
        '李承运',
        'https://example.com/avatar/member-10002.png',
        'pending',
        '联调示例会员：未绑定微信，可测试手工绑定和待审核状态',
        null,
        0,
        '2026-05-18 11:00:00',
        '2026-05-18 11:00:00'
    ),
    (
        10003,
        '13800000003',
        null,
        null,
        null,
        '123456',
        '王海东',
        '王海东',
        'https://example.com/avatar/member-10003.png',
        'disabled',
        '联调示例会员：已禁用，可测试状态展示与边界提示',
        '2026-05-17 16:40:00',
        0,
        '2026-05-17 16:00:00',
        '2026-05-19 08:30:00'
    )
ON DUPLICATE KEY UPDATE
    phone = VALUES(phone),
    wechat_openid = VALUES(wechat_openid),
    wechat_unionid = VALUES(wechat_unionid),
    wechat_bind_time = VALUES(wechat_bind_time),
    password_hash = VALUES(password_hash),
    nickname = VALUES(nickname),
    full_name = VALUES(full_name),
    avatar_url = VALUES(avatar_url),
    status = VALUES(status),
    remark = VALUES(remark),
    last_login_at = VALUES(last_login_at),
    deleted = VALUES(deleted),
    updated_at = VALUES(updated_at);

ALTER TABLE member_user AUTO_INCREMENT = 10004;
