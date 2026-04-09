DELETE FROM site_content_page WHERE page_code = 'home';

INSERT INTO site_content_page (
    page_code,
    status,
    form_json,
    published_at,
    created_at,
    updated_at
) VALUES (
    'home',
    'published',
    '{
      "hero": {
        "enabled": true,
        "images": [
          {
            "id": "banner-1",
            "name": "banner-logistics-1.jpg",
            "url": "https://images.unsplash.com/photo-1494412651409-8963ce7935a7?auto=format&fit=crop&w=1400&q=80"
          },
          {
            "id": "banner-2",
            "name": "banner-logistics-2.jpg",
            "url": "https://images.unsplash.com/photo-1570710891163-6d3b5c47248b?auto=format&fit=crop&w=1400&q=80"
          }
        ],
        "title": "全球物流解决方案",
        "subtitle": "为企业提供稳定、透明、可追踪的国际物流服务，覆盖海运、空运、清关与末端交付全链路。",
        "primaryButtonText": "立即咨询",
        "primaryButtonLink": "/contact",
        "secondaryButtonText": "查看服务",
        "secondaryButtonLink": "/services"
      },
      "tracking": {
        "enabled": true,
        "title": "运单查询",
        "placeholder": "请输入运单号",
        "buttonText": "查询"
      },
      "servicesSection": {
        "enabled": true,
        "title": "主营业务",
        "description": "聚焦核心跨境物流场景，为客户提供高时效、高稳定性和高性价比的物流方案。",
        "items": [
          {
            "id": "service-1",
            "iconUrl": "https://img.icons8.com/fluency-systems-regular/96/f97316/shipping-container.png",
            "name": "台湾专线",
            "description": "提供台湾海运、空运双清派送到门服务，适合电商和贸易类客户持续发货。",
            "link": "/services/taiwan"
          },
          {
            "id": "service-2",
            "iconUrl": "https://img.icons8.com/fluency-systems-regular/96/f97316/warehouse.png",
            "name": "非洲专线",
            "description": "深耕非洲市场，多国门到门物流可选，支持普货、带电和定制清关服务。",
            "link": "/services/africa"
          },
          {
            "id": "service-3",
            "iconUrl": "https://img.icons8.com/fluency-systems-regular/96/f97316/paper-plane.png",
            "name": "国际空运",
            "description": "整合主流航空资源，提供高时效国际空运和目的港清关配送能力。",
            "link": "/services/air"
          }
        ]
      },
      "processSection": {
        "enabled": true,
        "title": "一站式服务",
        "subtitle": "从需求沟通到签收交付，全流程由专业团队跟进，降低跨境物流沟通与履约成本。",
        "steps": [
          {
            "id": "process-1",
            "title": "需求沟通",
            "description": "由客户经理了解产品属性、时效要求、目的国与履约方式。"
          },
          {
            "id": "process-2",
            "title": "方案报价",
            "description": "结合货量与运输方式提供物流方案、报价和风险提示。"
          },
          {
            "id": "process-3",
            "title": "揽收发运",
            "description": "确认合作后安排提货、入仓、打板或订舱并进入正式运输。"
          },
          {
            "id": "process-4",
            "title": "清关配送",
            "description": "完成出口与目的国清关后，安排末端派送或客户自提。"
          }
        ]
      },
      "promiseSection": {
        "title": "我们承诺",
        "subtitle": "您的每一次托付，我们都全力以赴",
        "items": [
          {
            "id": "promise-1",
            "iconUrl": "https://img.icons8.com/ios/100/ffffff/shield--v1.png",
            "title": "100%安全",
            "subtitle": "全链路风控管理，运输过程透明可追踪。"
          },
          {
            "id": "promise-2",
            "iconUrl": "https://img.icons8.com/ios/100/ffffff/battery-level.png",
            "title": "一条龙服务",
            "subtitle": "从揽收、报关到派送，全程专人跟进。"
          },
          {
            "id": "promise-3",
            "iconUrl": "https://img.icons8.com/ios/100/ffffff/export.png",
            "title": "快捷清关",
            "subtitle": "熟悉多国口岸政策，提升整体通关效率。"
          },
          {
            "id": "promise-4",
            "iconUrl": "https://img.icons8.com/ios/100/ffffff/administrator-male--v1.png",
            "title": "一级代理",
            "subtitle": "整合头程与尾程资源，方案更稳更具性价比。"
          },
          {
            "id": "promise-5",
            "iconUrl": "https://img.icons8.com/ios/100/ffffff/clock--v1.png",
            "title": "多年专业经验",
            "subtitle": "成熟团队处理复杂货型和多场景履约需求。"
          },
          {
            "id": "promise-6",
            "iconUrl": "https://img.icons8.com/ios/100/ffffff/bank-card-back-side.png",
            "title": "付款便捷",
            "subtitle": "支持多种支付方式，流程简单透明。"
          }
        ]
      },
      "seo": {
        "title": "QSD Logistics | 国际物流与供应链解决方案",
        "description": "QSD 提供国际海运、空运、清关、仓配及门到门物流服务，帮助企业提升跨境交付效率。",
        "keywords": "国际物流,海运,空运,清关,跨境运输,供应链"
      }
    }',
    NOW(),
    NOW(),
    NOW()
)
ON DUPLICATE KEY UPDATE
    status = VALUES(status),
    form_json = VALUES(form_json),
    published_at = VALUES(published_at),
    updated_at = VALUES(updated_at);
