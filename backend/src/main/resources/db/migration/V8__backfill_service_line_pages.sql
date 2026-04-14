insert into site_content_page (page_code, status, form_json, published_at, created_at, updated_at)
values (
    'service-line:taiwan',
    'published',
    json_object(
        'key', 'taiwan',
        'eyebrow', 'Taiwan Line',
        'title', '台湾专线服务',
        'subtitle', '适合跨境电商补货、样品寄送和常规贸易发运的稳定线路。',
        'description', '围绕时效、清关和末端派送做标准化协同，减少反复确认和节点不透明问题。',
        'heroImage', 'https://images.unsplash.com/photo-1570710891163-6d3b5c47248b?auto=format&fit=crop&w=1600&q=80',
        'heroTags', json_array('双清到门', '固定班次', '全程跟踪', '适配电商补货'),
        'metrics', json_array(
            json_object('label', '常用方案', 'value', '空运 / 海运'),
            json_object('label', '服务特征', 'value', '双清到门'),
            json_object('label', '末端范围', 'value', '台湾全境派送')
        ),
        'highlightsTitle', '为什么客户会优先选择台湾专线',
        'highlightsSubtitle', '把时效稳定、流程透明和异常协同放在同一套服务标准里。',
        'highlights', json_array(
            json_object('title', '时效稳定', 'description', '固定班次和成熟链路，适合周发货和月发货场景。', 'icon', 'mdi-clock-outline'),
            json_object('title', '流程清晰', 'description', '报价、收货、清关和派送节点统一同步。', 'icon', 'mdi-map-marker-path'),
            json_object('title', '适配电商', 'description', '支持样品件、补货件和常规贸易发运。', 'icon', 'mdi-storefront-outline'),
            json_object('title', '异常可协同', 'description', '出现偏差时可快速定位节点并推进处理。', 'icon', 'mdi-alert-circle-outline')
        ),
        'processTitle', '标准服务流程',
        'processSubtitle', '让高频线路形成可复用的固定执行节奏。',
        'processSteps', json_array('咨询报价', '确认下单', '安排提货', '出口报关', '台湾清关', '末端派送'),
        'scopeTitle', '适用货型与业务场景',
        'scopeSubtitle', '适合普货、合规带电、样品件和常规贸易补货场景。',
        'scopeImage', 'https://images.unsplash.com/photo-1586528116311-ad8dd3c8310d?auto=format&fit=crop&w=1400&q=80',
        'scopeItems', json_array('服装鞋帽', '日用百货', '合规带电产品', '商业样品'),
        'supportTitle', '更适合哪些客户',
        'supportDescription', '适合货量稳定、需要节点透明并希望降低沟通成本的长期客户。',
        'supportItems', json_array('跨境电商补货', '两岸贸易订单', '品牌样品寄送', '个人搬家运输'),
        'ctaTitle', '准备开始你的台湾专线发运了吗？',
        'ctaSubtitle', '把货型、目的地和时效要求发给我们，我们会先判断链路再给报价。'
    ),
    '2026-04-14 12:00:00',
    '2026-04-14 12:00:00',
    '2026-04-14 12:00:00'
)
on duplicate key update
    status = case
        when coalesce(json_unquote(json_extract(form_json, '$.title')), '') = ''
         and coalesce(json_unquote(json_extract(form_json, '$.description')), '') = ''
        then values(status) else status end,
    form_json = case
        when coalesce(json_unquote(json_extract(form_json, '$.title')), '') = ''
         and coalesce(json_unquote(json_extract(form_json, '$.description')), '') = ''
        then values(form_json) else form_json end,
    published_at = case
        when coalesce(json_unquote(json_extract(form_json, '$.title')), '') = ''
         and coalesce(json_unquote(json_extract(form_json, '$.description')), '') = ''
        then coalesce(published_at, values(published_at)) else published_at end,
    updated_at = case
        when coalesce(json_unquote(json_extract(form_json, '$.title')), '') = ''
         and coalesce(json_unquote(json_extract(form_json, '$.description')), '') = ''
        then values(updated_at) else updated_at end;

insert into site_content_page (page_code, status, form_json, published_at, created_at, updated_at)
values (
    'service-line:feizhou',
    'published',
    json_object(
        'key', 'feizhou',
        'eyebrow', 'Africa Line',
        'title', '非洲专线服务',
        'subtitle', '覆盖重点国家的门到门交付，适合重视清关协同和稳定履约的客户。',
        'description', '把方案判断、资料准备、目的港协同和末端签收纳入同一套执行流程，降低返工概率。',
        'heroImage', 'https://images.unsplash.com/photo-1519003722824-194d4455a60c?auto=format&fit=crop&w=1600&q=80',
        'heroTags', json_array('重点国家覆盖', '清关协同', '节点同步', '门到门交付'),
        'metrics', json_array(
            json_object('label', '重点方向', 'value', '东非 / 西非'),
            json_object('label', '服务方式', 'value', '专线门到门'),
            json_object('label', '适配货型', 'value', '普货 / 合规带电')
        ),
        'highlightsTitle', '非洲专线更关注什么',
        'highlightsSubtitle', '把资料准备、节点同步和目的港交付能力前置到方案阶段。',
        'highlights', json_array(
            json_object('title', '资料准备更前置', 'description', '根据目的国要求提前梳理资料，减少清关返工。', 'icon', 'mdi-file-check-outline'),
            json_object('title', '目的港协同', 'description', '加强清关和派送节点联动，异常处理更直接。', 'icon', 'mdi-handshake-outline'),
            json_object('title', '适合阶段放量', 'description', '适合从试发货逐步扩大到稳定月发运。', 'icon', 'mdi-chart-timeline-variant'),
            json_object('title', '多节点同步', 'description', '关键节点清晰，便于客户内部传递和追踪。', 'icon', 'mdi-message-badge-outline')
        ),
        'processTitle', '专线执行流程',
        'processSubtitle', '把前期资料确认和后段交付协同都做扎实。',
        'processSteps', json_array('需求评估', '资料确认', '收货入仓', '国际运输', '目的港清关', '末端签收'),
        'scopeTitle', '适配品类与发运场景',
        'scopeSubtitle', '适合机械配件、建材辅料、日化百货和阶段性补货业务。',
        'scopeImage', 'https://images.unsplash.com/photo-1504384308090-c894fdcc538d?auto=format&fit=crop&w=1400&q=80',
        'scopeItems', json_array('机械配件', '建材辅料', '日化百货', '项目型补货'),
        'supportTitle', '建议哪些客户优先使用',
        'supportDescription', '适合需要稳定交付、又希望减少重复沟通成本的客户。',
        'supportItems', json_array('海外工程配套', '区域经销补货', '平台卖家备货', '贸易订单履约'),
        'ctaTitle', '准备评估你的非洲专线方案了吗？',
        'ctaSubtitle', '先把目的国、货型和预估货量发给我们，我们会先判断清关和交付风险。'
    ),
    '2026-04-14 12:05:00',
    '2026-04-14 12:05:00',
    '2026-04-14 12:05:00'
)
on duplicate key update
    status = case
        when coalesce(json_unquote(json_extract(form_json, '$.title')), '') = ''
         and coalesce(json_unquote(json_extract(form_json, '$.description')), '') = ''
        then values(status) else status end,
    form_json = case
        when coalesce(json_unquote(json_extract(form_json, '$.title')), '') = ''
         and coalesce(json_unquote(json_extract(form_json, '$.description')), '') = ''
        then values(form_json) else form_json end,
    published_at = case
        when coalesce(json_unquote(json_extract(form_json, '$.title')), '') = ''
         and coalesce(json_unquote(json_extract(form_json, '$.description')), '') = ''
        then coalesce(published_at, values(published_at)) else published_at end,
    updated_at = case
        when coalesce(json_unquote(json_extract(form_json, '$.title')), '') = ''
         and coalesce(json_unquote(json_extract(form_json, '$.description')), '') = ''
        then values(updated_at) else updated_at end;

insert into site_content_page (page_code, status, form_json, published_at, created_at, updated_at)
values (
    'service-line:international',
    'published',
    json_object(
        'key', 'international',
        'eyebrow', 'International Express',
        'title', '国际快递服务',
        'subtitle', '适合样品件、紧急补货和轻小件高频发运场景。',
        'description', '围绕时效判断、渠道匹配和异常响应速度，提供更清晰的国际快递发运建议。',
        'heroImage', 'https://images.unsplash.com/photo-1566576912321-d58ddd7a6088?auto=format&fit=crop&w=1600&q=80',
        'heroTags', json_array('高时效', '门到门', '样品件', '轻小件发运'),
        'metrics', json_array(
            json_object('label', '常见用途', 'value', '样品 / 紧急补货'),
            json_object('label', '交付方式', 'value', '门到门'),
            json_object('label', '节点反馈', 'value', '关键轨迹同步')
        ),
        'highlightsTitle', '国际快递更关注什么',
        'highlightsSubtitle', '核心是时效判断、渠道匹配和异常响应速度。',
        'highlights', json_array(
            json_object('title', '时效判断清晰', 'description', '先判断目的地和渠道差异，再给出发运建议。', 'icon', 'mdi-timer-sand'),
            json_object('title', '渠道组合灵活', 'description', '按重量、预算和时效匹配更合适的渠道。', 'icon', 'mdi-source-branch'),
            json_object('title', '适合轻小件', 'description', '适合样品、文件和紧急补货件。', 'icon', 'mdi-package-variant-closed'),
            json_object('title', '异常响应快', 'description', '轨迹偏差和附加问题可以更快同步与处理。', 'icon', 'mdi-lightning-bolt-outline')
        ),
        'processTitle', '标准交付流程',
        'processSubtitle', '缩短方案判断和实际发运之间的切换时间。',
        'processSteps', json_array('渠道确认', '报价下单', '收货打单', '国际转运', '末端派送'),
        'scopeTitle', '适配场景',
        'scopeSubtitle', '适合样品件、合同文件、轻小件补货和快节奏小批量发运。',
        'scopeImage', 'https://images.unsplash.com/photo-1586528116493-6d3d71d0f6a3?auto=format&fit=crop&w=1400&q=80',
        'scopeItems', json_array('样品寄送', '合同文件', '紧急补货', '轻小件发运'),
        'supportTitle', '建议哪些客户优先使用',
        'supportDescription', '适合对时效更敏感、单票件量较轻的小批量发运。',
        'supportItems', json_array('样品开发团队', '跨境电商紧急补货', '海外售后寄件'),
        'ctaTitle', '需要更快判断国际快递方案吗？',
        'ctaSubtitle', '把货重、件数、目的地和目标时效发给我们，我们会给出更合适的渠道建议。'
    ),
    '2026-04-14 12:10:00',
    '2026-04-14 12:10:00',
    '2026-04-14 12:10:00'
)
on duplicate key update
    status = case
        when coalesce(json_unquote(json_extract(form_json, '$.title')), '') = ''
         and coalesce(json_unquote(json_extract(form_json, '$.description')), '') = ''
        then values(status) else status end,
    form_json = case
        when coalesce(json_unquote(json_extract(form_json, '$.title')), '') = ''
         and coalesce(json_unquote(json_extract(form_json, '$.description')), '') = ''
        then values(form_json) else form_json end,
    published_at = case
        when coalesce(json_unquote(json_extract(form_json, '$.title')), '') = ''
         and coalesce(json_unquote(json_extract(form_json, '$.description')), '') = ''
        then coalesce(published_at, values(published_at)) else published_at end,
    updated_at = case
        when coalesce(json_unquote(json_extract(form_json, '$.title')), '') = ''
         and coalesce(json_unquote(json_extract(form_json, '$.description')), '') = ''
        then values(updated_at) else updated_at end;
