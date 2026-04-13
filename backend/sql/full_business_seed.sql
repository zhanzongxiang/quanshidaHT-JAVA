SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- =========================================================
-- qsd-admin 完整业务示例数据
-- 导入前提：
-- 1. 已执行 V1 ~ V6 Flyway 迁移
-- 2. 目标库为 MySQL 8.x
-- 3. 本脚本会覆盖后台示例账号、菜单权限、内容页、新闻和日志数据
-- =========================================================

START TRANSACTION;

DELETE FROM admin_role_menu;
DELETE FROM admin_role_permission;
DELETE FROM admin_user_role;
DELETE FROM login_log;
DELETE FROM operation_log;
DELETE FROM news_article;
DELETE FROM site_content_page;
DELETE FROM admin_menu;
DELETE FROM permission;
DELETE FROM admin_role;
DELETE FROM admin_user;

INSERT INTO admin_user (
    id,
    username,
    password_hash,
    status,
    deleted,
    created_at,
    updated_at
) VALUES (
    1,
    'admin',
    'admin123',
    'ENABLED',
    0,
    '2026-04-12 09:00:00',
    '2026-04-12 09:00:00'
);

INSERT INTO admin_role (
    id,
    role_code,
    role_name
) VALUES (
    1,
    'super_admin',
    '超级管理员'
);

INSERT INTO permission (
    id,
    perm_code,
    perm_name
) VALUES
    (1, 'user:view', '查看用户'),
    (2, 'user:edit', '编辑用户'),
    (3, 'content:view', '查看内容'),
    (4, 'content:edit', '编辑内容'),
    (5, 'news:view', '查看新闻'),
    (6, 'news:edit', '编辑新闻'),
    (7, 'settings:view', '查看设置'),
    (8, 'settings:edit', '编辑设置');

INSERT INTO admin_menu (
    id,
    parent_id,
    name,
    path,
    component,
    icon,
    sort_no
) VALUES
    (1, 0, '工作台', '/dashboard', 'Dashboard', 'DataLine', 10),
    (2, 0, '页面管理', '/pages', 'MenuGroup', 'FolderOpened', 20),
    (3, 2, '首页配置', '/pages/home', 'HomeContent', 'House', 21),
    (4, 2, '线路页面', '/pages/service-lines', 'ServiceLines', 'Files', 22),
    (5, 2, '新闻资讯', '/news', 'News', 'Tickets', 23),
    (6, 0, '全局配置', '/settings', 'MenuGroup', 'Setting', 30),
    (7, 6, '导航设置', '/settings/navigation', 'NavigationSettings', 'Guide', 31),
    (8, 6, '页脚设置', '/settings/footer', 'FooterSettings', 'Bottom', 32),
    (9, 6, '联系方式', '/settings/contact', 'ContactSettings', 'Phone', 33);

INSERT INTO admin_user_role (
    id,
    user_id,
    role_id
) VALUES (
    1,
    1,
    1
);

INSERT INTO admin_role_permission (
    id,
    role_id,
    permission_id
) VALUES
    (1, 1, 1),
    (2, 1, 2),
    (3, 1, 3),
    (4, 1, 4),
    (5, 1, 5),
    (6, 1, 6),
    (7, 1, 7),
    (8, 1, 8);

INSERT INTO admin_role_menu (
    id,
    role_id,
    menu_id
) VALUES
    (1, 1, 1),
    (2, 1, 2),
    (3, 1, 3),
    (4, 1, 4),
    (5, 1, 5),
    (6, 1, 6),
    (7, 1, 7),
    (8, 1, 8),
    (9, 1, 9);

INSERT INTO site_content_page (
    id,
    page_code,
    status,
    form_json,
    published_at,
    created_at,
    updated_at
) VALUES
(
    1,
    'home',
    'published',
    JSON_OBJECT(
        'hero', JSON_OBJECT(
            'slides', JSON_ARRAY(
                JSON_OBJECT(
                    'image', 'https://images.unsplash.com/photo-1494412651409-8963ce7935a7?auto=format&fit=crop&w=1600&q=80',
                    'alt', '首页 Banner 01',
                    'title', '把跨境物流做成可追踪、可复用、可持续放量的日常能力',
                    'subtitle', '覆盖台湾专线、非洲专线和国际快递，围绕时效、履约、异常协同和报价透明度建立标准化交付流程。',
                    'primaryButton', JSON_OBJECT(
                        'text', '立即咨询',
                        'actionType', 'route',
                        'value', '/contact'
                    ),
                    'secondaryButton', JSON_OBJECT(
                        'text', '查看线路',
                        'actionType', 'route',
                        'value', '/taiwan'
                    )
                ),
                JSON_OBJECT(
                    'image', 'https://images.unsplash.com/photo-1519003722824-194d4455a60c?auto=format&fit=crop&w=1600&q=80',
                    'alt', '首页 Banner 02',
                    'title', '',
                    'subtitle', '',
                    'primaryButton', JSON_OBJECT(
                        'text', '',
                        'actionType', 'route',
                        'value', ''
                    ),
                    'secondaryButton', JSON_OBJECT(
                        'text', '',
                        'actionType', 'route',
                        'value', ''
                    )
                )
            )
        ),
        'trackingSection', JSON_OBJECT(
            'title', '运单查询',
            'inputPlaceholder', '请输入运单号 / 参考单号',
            'searchButtonText', '立即查询',
            'emptyText', '输入运单号后可查看最新轨迹。',
            'notFoundText', '未查询到对应运单，请核对单号后重试。',
            'loadingText', '正在查询运单轨迹，请稍候...'
        ),
        'businessSection', JSON_OBJECT(
            'title', '主营业务',
            'subtitle', '围绕跨境专线和高频发运场景，建立更稳定的履约和协同机制。',
            'items', JSON_ARRAY(
                JSON_OBJECT(
                    'title', '台湾专线',
                    'description', '支持海运、空运双清到门，适合电商补货、贸易发运和稳定周发货场景。',
                    'icon', 'mdi-ferry',
                    'url', '/taiwan'
                ),
                JSON_OBJECT(
                    'title', '非洲专线',
                    'description', '聚焦重点目的国门到门交付，支持普货、带电和定制化清关协同。',
                    'icon', 'mdi-earth',
                    'url', '/feizhou'
                ),
                JSON_OBJECT(
                    'title', '国际快递',
                    'description', '整合主流渠道资源，适合高时效样品件、紧急补货和高频小包发运。',
                    'icon', 'mdi-airplane',
                    'url', '/international'
                )
            )
        ),
        'processSection', JSON_OBJECT(
            'title', '一站式服务流程',
            'subtitle', '把报价、收货、清关、运输和签收收敛成一套可复用的标准链路。',
            'steps', JSON_ARRAY(
                JSON_OBJECT('title', '需求沟通', 'description', '确认货型、目的地、时效与清关要求。'),
                JSON_OBJECT('title', '方案报价', 'description', '给出渠道建议、价格区间和风险提示。'),
                JSON_OBJECT('title', '入仓发运', 'description', '安排提货、入仓、分拣或订舱。'),
                JSON_OBJECT('title', '清关交付', 'description', '完成清关、派送并同步关键节点。')
            )
        ),
        'promiseSection', JSON_OBJECT(
            'title', '我们承诺',
            'subtitle', '每一票货都按可视化、可追踪、可复盘的标准执行。',
            'items', JSON_ARRAY(
                JSON_OBJECT(
                    'title', '报价透明',
                    'description', '在报价阶段明确费用结构和附加条件，减少履约过程中的额外争议。',
                    'icon', 'mdi-cash-check',
                    'imageUrl', 'https://images.unsplash.com/photo-1554224155-6726b3ff858f?auto=format&fit=crop&w=1200&q=80'
                ),
                JSON_OBJECT(
                    'title', '时效优先判断',
                    'description', '先判断时效和风险，再推荐渠道，避免只用最低价做错误方案。',
                    'icon', 'mdi-timer-outline',
                    'imageUrl', 'https://images.unsplash.com/photo-1489515217757-5fd1be406fef?auto=format&fit=crop&w=1200&q=80'
                ),
                JSON_OBJECT(
                    'title', '异常主动同步',
                    'description', '异常节点第一时间同步，给出处理建议和预计恢复时间。',
                    'icon', 'mdi-bell-alert-outline',
                    'imageUrl', 'https://images.unsplash.com/photo-1520607162513-77705c0f0d4a?auto=format&fit=crop&w=1200&q=80'
                ),
                JSON_OBJECT(
                    'title', '清关协同',
                    'description', '围绕常见资料和目的国要求提前准备，降低卡关概率。',
                    'icon', 'mdi-file-document-check-outline',
                    'imageUrl', 'https://images.unsplash.com/photo-1454165804606-c3d57bc86b40?auto=format&fit=crop&w=1200&q=80'
                ),
                JSON_OBJECT(
                    'title', '复盘交付',
                    'description', '对高频线路持续复盘，让后续发运方案越来越稳。',
                    'icon', 'mdi-chart-line',
                    'imageUrl', 'https://images.unsplash.com/photo-1551288049-bebda4e38f71?auto=format&fit=crop&w=1200&q=80'
                ),
                JSON_OBJECT(
                    'title', '长期协作',
                    'description', '支持月结、对账和多角色协同，适合企业客户稳定放量。',
                    'icon', 'mdi-account-group-outline',
                    'imageUrl', 'https://images.unsplash.com/photo-1521737604893-d14cc237f11d?auto=format&fit=crop&w=1200&q=80'
                )
            )
        ),
        'newsPreviewSection', JSON_OBJECT(
            'title', '新闻资讯',
            'subtitle', '同步公司动态、线路更新和交付能力进展。',
            'viewAllText', '查看全部',
            'viewAllUrl', '/news',
            'items', JSON_ARRAY()
        ),
        'seo', JSON_OBJECT(
            'title', 'TEX Express | 跨境专线与国际快递综合服务商',
            'description', '围绕台湾专线、非洲专线和国际快递，提供更稳的履约、清关和异常协同服务。',
            'keywords', '跨境物流,台湾专线,非洲专线,国际快递,双清到门'
        )
    ),
    '2026-04-12 10:00:00',
    '2026-04-12 10:00:00',
    '2026-04-12 10:00:00'
),
(
    2,
    'service-line:taiwan',
    'published',
    JSON_OBJECT(
        'key', 'taiwan',
        'eyebrow', 'Taiwan Line',
        'title', '台湾专线服务',
        'subtitle', '双清到门、节点清晰、适合电商补货和常规贸易发运。',
        'description', '围绕台湾专线常见的时效、清关和末端派送需求，把报价、揽收、清关、运输和签收衔接成一套更清晰的服务流程。',
        'heroImage', 'https://images.unsplash.com/photo-1570710891163-6d3b5c47248b?auto=format&fit=crop&w=1600&q=80',
        'heroTags', JSON_ARRAY('双向清关', '固定班次', '全程追踪', '派送到门'),
        'metrics', JSON_ARRAY(
            JSON_OBJECT('label', '常用方案', 'value', '空运 / 海运'),
            JSON_OBJECT('label', '服务特征', 'value', '双清到门'),
            JSON_OBJECT('label', '末端范围', 'value', '台湾全境派送')
        ),
        'highlightsTitle', '为什么客户会优先选择台湾专线',
        'highlightsSubtitle', '把时效稳定、流程清晰和派送能力放在同一套服务逻辑里。',
        'highlights', JSON_ARRAY(
            JSON_OBJECT('title', '时效稳定', 'description', '固定班次和成熟操作节奏，适合周发货。', 'icon', 'mdi-clock-outline'),
            JSON_OBJECT('title', '流程清晰', 'description', '报价、收货、清关和派送节点统一同步。', 'icon', 'mdi-map-marker-path'),
            JSON_OBJECT('title', '适配电商', 'description', '适合补货、样品件和常规贸易发运。', 'icon', 'mdi-storefront-outline'),
            JSON_OBJECT('title', '异常好协同', 'description', '异常节点处理快，便于多角色协作。', 'icon', 'mdi-alert-circle-outline')
        ),
        'processTitle', '标准服务流程',
        'processSubtitle', '把高频线路做成更稳的标准节奏。',
        'processSteps', JSON_ARRAY('咨询报价', '确认下单', '安排提货', '出口报关', '台湾清关', '末端派送'),
        'scopeTitle', '适用货型与业务场景',
        'scopeSubtitle', '适合普货、合规带电、样品件和个人搬家件等常见场景。',
        'scopeImage', 'https://images.unsplash.com/photo-1586528116311-ad8dd3c8310d?auto=format&fit=crop&w=1400&q=80',
        'scopeItems', JSON_ARRAY('服装鞋帽', '日用百货', '合规带电产品', '商业样品'),
        'supportTitle', '更适合哪些客户',
        'supportDescription', '如果货量稳定、希望节点清晰，这类线路更适合长期合作。',
        'supportItems', JSON_ARRAY('跨境电商补货', '两岸贸易订单', '品牌样品寄送', '个人搬家运输'),
        'ctaTitle', '准备开始你的台湾专线发运了吗？',
        'ctaSubtitle', '把货型、目的地和时效需求发给我们，我们会先判断方案，再给报价。'
    ),
    '2026-04-12 10:10:00',
    '2026-04-12 10:10:00',
    '2026-04-12 10:10:00'
),
(
    3,
    'service-line:feizhou',
    'published',
    JSON_OBJECT(
        'key', 'feizhou',
        'eyebrow', 'Africa Line',
        'title', '非洲专线服务',
        'subtitle', '多国门到门可选，适合对清关协同和稳定交付有要求的客户。',
        'description', '聚焦非洲重点市场，把方案判断、资料准备、清关协同和末端交付整合到同一套执行流程里。',
        'heroImage', 'https://images.unsplash.com/photo-1519003722824-194d4455a60c?auto=format&fit=crop&w=1600&q=80',
        'heroTags', JSON_ARRAY('重点国家覆盖', '清关协同', '节点同步', '门到门交付'),
        'metrics', JSON_ARRAY(
            JSON_OBJECT('label', '重点方向', 'value', '东非 / 西非'),
            JSON_OBJECT('label', '服务方式', 'value', '专线门到门'),
            JSON_OBJECT('label', '适配货型', 'value', '普货 / 合规带电')
        ),
        'highlightsTitle', '非洲专线更关注什么',
        'highlightsSubtitle', '把清关准备、节点同步和目的港交付能力做在前面。',
        'highlights', JSON_ARRAY(
            JSON_OBJECT('title', '资料准备更前置', 'description', '围绕目的国要求提前梳理资料。', 'icon', 'mdi-file-check-outline'),
            JSON_OBJECT('title', '目的港协同', 'description', '加强目的港节点对接和异常响应。', 'icon', 'mdi-handshake-outline'),
            JSON_OBJECT('title', '适合阶段放量', 'description', '适合从试发货逐步扩到稳定月发运。', 'icon', 'mdi-chart-timeline-variant'),
            JSON_OBJECT('title', '多节点同步', 'description', '关键节点清晰，便于客户内部传递。', 'icon', 'mdi-message-badge-outline')
        ),
        'processTitle', '专线执行流程',
        'processSubtitle', '把前期资料确认和后段交付协同做扎实。',
        'processSteps', JSON_ARRAY('需求评估', '资料确认', '收货入仓', '国际运输', '目的港清关', '末端签收'),
        'scopeTitle', '适配品类与发运场景',
        'scopeSubtitle', '适合机械配件、建材辅料、日化用品和阶段性补货业务。',
        'scopeImage', 'https://images.unsplash.com/photo-1504384308090-c894fdcc538d?auto=format&fit=crop&w=1400&q=80',
        'scopeItems', JSON_ARRAY('机械配件', '建材辅料', '日化百货', '项目型补货'),
        'supportTitle', '建议哪些客户优先使用',
        'supportDescription', '适合需要阶段性稳定交付、又希望减少反复沟通成本的客户。',
        'supportItems', JSON_ARRAY('海外工程配套', '区域经销补货', '平台卖家备货', '贸易订单交付'),
        'ctaTitle', '准备评估你的非洲专线方案了吗？',
        'ctaSubtitle', '先把目的国、货型和预估货量发给我们，我们会先判断清关和交付风险。'
    ),
    '2026-04-12 10:20:00',
    '2026-04-12 10:20:00',
    '2026-04-12 10:20:00'
),
(
    4,
    'service-line:international',
    'draft',
    JSON_OBJECT(
        'key', 'international',
        'eyebrow', 'International Express',
        'title', '国际快递服务',
        'subtitle', '适合高时效样品件、紧急补货和高频小包发运。',
        'description', '整合主流渠道资源，围绕时效、价格和目的地要求，提供更清晰的国际快递发运建议。',
        'heroImage', 'https://images.unsplash.com/photo-1566576912321-d58ddd7a6088?auto=format&fit=crop&w=1600&q=80',
        'heroTags', JSON_ARRAY('高时效', '门到门', '样品件', '小包快递'),
        'metrics', JSON_ARRAY(
            JSON_OBJECT('label', '常见用途', 'value', '样品 / 紧急补货'),
            JSON_OBJECT('label', '交付方式', 'value', '门到门'),
            JSON_OBJECT('label', '节点反馈', 'value', '关键轨迹同步')
        ),
        'highlightsTitle', '国际快递更关注什么',
        'highlightsSubtitle', '核心是时效判断、渠道匹配和异常响应速度。',
        'highlights', JSON_ARRAY(
            JSON_OBJECT('title', '时效判断清晰', 'description', '先判断目的地和渠道差异，再给建议。', 'icon', 'mdi-timer-sand'),
            JSON_OBJECT('title', '渠道组合灵活', 'description', '按重量、时效和预算给出更合适的组合。', 'icon', 'mdi-source-branch'),
            JSON_OBJECT('title', '适合轻小件', 'description', '适合样品、紧急补货和文件件。', 'icon', 'mdi-package-variant-closed'),
            JSON_OBJECT('title', '异常响应快', 'description', '轨迹异常和偏远附加可及时沟通。', 'icon', 'mdi-lightning-bolt-outline')
        ),
        'processTitle', '标准交付流程',
        'processSubtitle', '缩短方案判断和实际发运的切换时间。',
        'processSteps', JSON_ARRAY('渠道确认', '报价下单', '收货打单', '国际转运', '末端派送'),
        'scopeTitle', '适配场景',
        'scopeSubtitle', '适合样品件、合同文件、轻小件补货和节奏快的小批量发运。',
        'scopeImage', 'https://images.unsplash.com/photo-1586528116493-6d3d71d0f6a3?auto=format&fit=crop&w=1400&q=80',
        'scopeItems', JSON_ARRAY('样品寄送', '合同文件', '紧急补货', '轻小件发运'),
        'supportTitle', '建议哪些客户优先使用',
        'supportDescription', '更适合对时效敏感、单票件量较轻的小批量发运。',
        'supportItems', JSON_ARRAY('样品开发团队', '跨境电商紧急补货', '海外售后寄件'),
        'ctaTitle', '需要更快判断国际快递方案？',
        'ctaSubtitle', '把货重、件数、目的地和目标时效发给我们，我们会给出更合适的渠道建议。'
    ),
    NULL,
    '2026-04-12 10:30:00',
    '2026-04-12 10:30:00'
);

INSERT INTO news_article (
    id,
    title,
    summary,
    cover_image_url,
    content,
    author,
    status,
    published_at,
    deleted,
    created_at,
    updated_at
) VALUES
(
    1,
    '公司上线新版线路模板管理，首页与线路页内容结构完成统一',
    '后台内容管理完成一次结构整理，首页、线路模板和联系我们字段与前台页面 schema 对齐。',
    'https://images.unsplash.com/photo-1552664730-d307ca884978?auto=format&fit=crop&w=1200&q=80',
    JSON_OBJECT(
        'blocks', JSON_ARRAY(
            JSON_OBJECT('id', 'block-1', 'type', 'paragraph', 'content', '本周完成了首页、线路模板和联系我们模块的数据结构整理，后台字段与前台页面展示结构保持一致。'),
            JSON_OBJECT('id', 'block-2', 'type', 'heading', 'content', '为什么要先做 schema 对齐'),
            JSON_OBJECT('id', 'block-3', 'type', 'paragraph', 'content', '这样可以减少后续前台取数和后台管理之间的映射成本，也更方便持续扩展公开页面接口。'),
            JSON_OBJECT('id', 'block-4', 'type', 'image', 'content', 'https://images.unsplash.com/photo-1516321497487-e288fb19713f?auto=format&fit=crop&w=1200&q=80'),
            JSON_OBJECT('id', 'block-5', 'type', 'image_caption', 'content', '图：后台内容结构调整后的示意图')
        )
    ),
    'QSD Admin',
    'published',
    '2026-04-12 11:00:00',
    0,
    '2026-04-12 11:00:00',
    '2026-04-12 11:00:00'
),
(
    2,
    '台湾专线新增周度履约复盘机制，异常处理时效进一步收紧',
    '围绕高频发运客户的异常反馈，台湾专线将周度复盘和节点追踪纳入标准交付流程。',
    'https://images.unsplash.com/photo-1520607162513-77705c0f0d4a?auto=format&fit=crop&w=1200&q=80',
    JSON_OBJECT(
        'blocks', JSON_ARRAY(
            JSON_OBJECT('id', 'block-1', 'type', 'paragraph', 'content', '为了提高高频发运客户的履约体验，我们将异常反馈、节点补录和案例复盘整合进了固定周度机制。'),
            JSON_OBJECT('id', 'block-2', 'type', 'heading', 'content', '这次调整影响哪些客户'),
            JSON_OBJECT('id', 'block-3', 'type', 'paragraph', 'content', '主要影响台湾专线周发货、月发货及对节点透明度要求较高的客户。'),
            JSON_OBJECT('id', 'block-4', 'type', 'paragraph', 'content', '后续会继续围绕清关资料准备、派送签收反馈和异常原因分类做细化。')
        )
    ),
    '运营中心',
    'published',
    '2026-04-08 16:30:00',
    0,
    '2026-04-08 16:30:00',
    '2026-04-08 16:30:00'
),
(
    3,
    '非洲专线资料预审清单进入试运行阶段',
    '针对目的国资料差异较大的情况，非洲专线开始试运行资料预审清单，降低清关阶段返工概率。',
    'https://images.unsplash.com/photo-1504384308090-c894fdcc538d?auto=format&fit=crop&w=1200&q=80',
    JSON_OBJECT(
        'blocks', JSON_ARRAY(
            JSON_OBJECT('id', 'block-1', 'type', 'paragraph', 'content', '我们正在试运行一版更细的资料预审清单，用来提升目的港清关前的准备充分度。'),
            JSON_OBJECT('id', 'block-2', 'type', 'heading', 'content', '试运行重点'),
            JSON_OBJECT('id', 'block-3', 'type', 'paragraph', 'content', '重点覆盖申报品名、发票信息、目的国限制要求和客户内部协同节点。')
        )
    ),
    '方案团队',
    'draft',
    NULL,
    0,
    '2026-04-10 14:20:00',
    '2026-04-10 14:20:00'
);

INSERT INTO operation_log (
    id,
    module_name,
    operation_type,
    operator,
    target_id,
    summary,
    created_at
) VALUES
    (1, '首页内容', 'PUBLISH', 'admin', 'home', '发布首页内容并更新 Banner、主营业务与新闻预览模块', '2026-04-12 11:10:00'),
    (2, '线路页面', 'SAVE_DRAFT', 'admin', 'service-line:taiwan', '保存台湾专线模板草稿并更新亮点模块', '2026-04-12 11:15:00'),
    (3, '新闻管理', 'CREATE', 'admin', 'news:1', '新增新闻：公司上线新版线路模板管理，首页与线路页内容结构完成统一', '2026-04-12 11:20:00'),
    (4, '全局设置', 'UPDATE', 'admin', 'settings:contact', '更新联系我们模块中的热线、微信和服务承诺配置', '2026-04-12 11:25:00');

INSERT INTO login_log (
    id,
    username,
    ip,
    user_agent,
    result,
    created_at
) VALUES
    (1, 'admin', '127.0.0.1', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) Chrome/135.0 AdminConsole', 'SUCCESS', '2026-04-12 09:05:00'),
    (2, 'admin', '127.0.0.1', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) Chrome/135.0 AdminConsole', 'SUCCESS', '2026-04-12 10:48:00'),
    (3, 'admin', '127.0.0.1', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) Chrome/135.0 AdminConsole', 'SUCCESS', '2026-04-12 11:12:00');

ALTER TABLE admin_user AUTO_INCREMENT = 2;
ALTER TABLE admin_role AUTO_INCREMENT = 2;
ALTER TABLE permission AUTO_INCREMENT = 9;
ALTER TABLE admin_menu AUTO_INCREMENT = 10;
ALTER TABLE admin_user_role AUTO_INCREMENT = 2;
ALTER TABLE admin_role_permission AUTO_INCREMENT = 9;
ALTER TABLE admin_role_menu AUTO_INCREMENT = 10;
ALTER TABLE site_content_page AUTO_INCREMENT = 5;
ALTER TABLE news_article AUTO_INCREMENT = 4;
ALTER TABLE operation_log AUTO_INCREMENT = 5;
ALTER TABLE login_log AUTO_INCREMENT = 4;

COMMIT;

SET FOREIGN_KEY_CHECKS = 1;
