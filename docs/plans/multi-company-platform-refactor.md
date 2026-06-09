# 多公司通用化平台重构计划

更新时间：2026-06-09

## 0. 当前进展

已完成第一批基础设施落地：

- 新增 Flyway 迁移 `V19__create_tenant_platform_foundation.sql`
- 新增 `tenant`、`tenant_domain`、`tenant_app`、`tenant_setting` 表
- 为 `admin_user` 增加 `tenant_id`，并将存量后台账号回填到默认租户 `default`
- 新增后端 `tenant` 模块基础代码
- 新增请求级 `TenantContext` / `TenantContextFilter`
- 后台登录 JWT 已包含 `tenantId` / `tenantCode`
- 新增租户管理接口：
  - `GET /api/platform/tenants/current`
  - `GET /api/platform/tenants`
  - `POST /api/platform/tenants`
  - `PUT /api/platform/tenants/{id}`
- 运单模块已完成第一轮租户化：
  - 新增 `V20__tenantize_waybill_module.sql`
  - `waybill_order`、`waybill_leg`、`waybill_track_event` 已增加 `tenant_id`
  - 运单后台管理、公开查单、仪表盘运单统计已切到租户上下文
  - 依赖运单读取的会员/支付服务调用已完成方法签名对齐

当前仍未完成：

- 会员核心表 `tenant_id` 改造
- 支付核心表 `tenant_id` 改造
- 内容与新闻模块 `tenant_id` 改造
- 前端租户管理页面
- 站点内容、支付、会员、运单等模块的租户隔离改造

## 1. 目标

将当前以单一公司业务为默认前提的运单系统，重构为可服务多个不同公司的平台。

目标不是简单加一个“公司名称”字段，而是完成以下平台化能力：

- 同一套后端、管理端、小程序代码可服务多个公司
- 不同公司之间的数据、账号、支付配置、站点内容相互隔离
- 每个公司可独立维护自己的官网内容、服务线路、支付商户、会员、运单
- 平台方可以统一运维、审计、配置和扩展能力
- 后续新增公司时，以“开通租户 + 初始化配置”为主，而不是复制代码或复制一套库

## 2. 当前单公司耦合点

基于当前代码，主要存在以下单公司耦合：

### 2.1 站点内容写死为单公司

- 公开站点默认品牌、联系方式、导航等内容在 [backend/src/main/java/com/qsd/admin/website/service/PublicWebsiteService.java](/E:/me/quanshidaHT-JAVA/backend/src/main/java/com/qsd/admin/website/service/PublicWebsiteService.java) 中硬编码
- `TEX Express`、默认邮箱、固定导航、固定服务线路路径直接写在服务层
- `site_content_page.page_code` 目前是全局唯一，没有公司维度

### 2.2 服务线路定义写死

- 当前线路是固定的 `taiwan`、`feizhou`、`international`
- 线路定义写在 `ServiceLineContentService`
- 不同公司无法维护自己的线路集合、名称、排序、路径、状态

### 2.3 业务数据没有公司隔离维度

- `waybill_order`
- `member_user`
- `member_waybill_relation`
- `pay_order`
- `refund_order`
- `pay_notify_log`
- `refund_notify_log`
- `pay_reconcile_record`
- `news_article`
- `site_content_page`

以上核心表目前都没有 `tenant_id` / `company_id`

### 2.4 后台账号与权限没有租户作用域

- `admin_user`
- `admin_role`
- `admin_menu`
- `permission`

当前更接近“单后台管理单业务空间”，而不是“平台管理员 + 公司管理员”双层模型

### 2.5 支付配置作用域不清晰

- `pay_merchant_config` 当前是全局商户池
- “当前生效商户”逻辑默认作用于整套系统
- 如果多个公司同时使用平台，会发生支付配置串用风险

### 2.6 小程序和微信身份未平台化

- 小程序 `appId`
- 微信登录配置
- 支付商户 `appId`
- 域名与回调地址

这些配置目前默认对应单个业务主体，不适合多个公司并存

### 2.7 初始化数据与种子数据绑定单公司

- `full_business_seed.sql`
- `home_content_seed.sql`
- 部分默认文案和页面结构

当前初始化方式更像“生成一个具体公司的网站和后台”，不是“生成一个空白平台租户”

## 3. 平台化设计原则

### 3.1 以租户为一等公民

建议统一使用 `tenant` 概念，不要同时混用 `company`、`organization`、`merchant`。

平台建议最小核心模型：

- `platform`：整个平台部署实例
- `tenant`：某一个入驻公司
- `tenant_app`：租户下的小程序或站点应用
- `tenant_payment_profile`：租户支付配置

### 3.2 先做“共享库 + 行级隔离”，不要先做“一租户一库”

当前项目更适合第一阶段采用共享数据库、核心业务表加 `tenant_id` 的方案：

- 改造成本低
- 现有查询和后台逻辑可渐进式迁移
- 更适合当前项目规模

后续如果某些大客户有独立部署需求，再评估“独立库 / 独立实例”

### 3.3 平台管理与租户管理分层

至少需要两层后台角色：

- 平台管理员：管理租户开通、配额、全局监控、审计
- 租户管理员：只管理本公司数据

### 3.4 显式租户上下文，不依赖隐式猜测

所有后端请求都应有明确租户上下文来源，建议优先级：

1. 管理端 JWT 中的 `tenantId`
2. 公开站点域名映射到租户
3. 小程序 `appId` / 请求头 / 网关路由映射到租户
4. 内部任务显式带 `tenantId`

不要依赖“手机号能推断是哪家公司”这类隐式规则。

### 3.5 配置数据和业务数据分离

下列内容应从代码常量迁移到租户配置或租户内容模型：

- 品牌名称
- Logo
- 联系方式
- 服务线路
- 页面导航
- 微信公众号文案
- 小程序 `appId`
- 支付回调域名

## 4. 目标架构

## 4.1 新增核心租户模型

建议新增：

- `tenant`
- `tenant_domain`
- `tenant_app`
- `tenant_setting`

建议字段：

### `tenant`

- `id`
- `tenant_code`
- `tenant_name`
- `status`
- `timezone`
- `locale`
- `remark`
- `created_at`
- `updated_at`

### `tenant_domain`

- `id`
- `tenant_id`
- `domain`
- `domain_type`
- `enabled`
- `created_at`

### `tenant_app`

- `id`
- `tenant_id`
- `app_type`
- `app_code`
- `wechat_app_id`
- `wechat_app_secret`
- `status`
- `created_at`
- `updated_at`

### `tenant_setting`

- `id`
- `tenant_id`
- `setting_group`
- `setting_key`
- `setting_value`
- `created_at`
- `updated_at`

## 4.2 业务表统一加 `tenant_id`

第一批必须加：

- `waybill_order`
- `waybill_leg`
- `waybill_track_event`
- `member_user`
- `member_waybill_relation`
- `pay_order`
- `pay_transaction`
- `refund_order`
- `pay_notify_log`
- `refund_notify_log`
- `pay_reconcile_record`
- `pay_merchant_config`
- `news_article`
- `site_content_page`

第二批建议加：

- `admin_user`
- `admin_role`
- `admin_role_permission`
- `admin_role_menu`
- 审计日志类表

## 4.3 公开内容模型改造

现状问题：

- 页面内容部分在 DB
- 站点配置部分在代码
- 服务线路部分半固定半内容化

目标：

- 站点配置、导航、页脚、联系信息全部租户化
- 服务线路改成租户可配置的数据模型，而不是固定枚举

建议新增：

- `tenant_site_profile`
- `tenant_service_line`

`tenant_service_line` 建议字段：

- `id`
- `tenant_id`
- `line_code`
- `line_name`
- `line_slug`
- `line_category`
- `status`
- `sort_no`
- `page_code`
- `created_at`
- `updated_at`

## 5. 分阶段任务

## 阶段 A：平台基础层

### A1. 新增租户核心表

- 新增 `tenant`
- 新增 `tenant_domain`
- 新增 `tenant_app`
- 新增 `tenant_setting`

验收标准：

- 能创建至少 2 个租户
- 每个租户能保存独立名称、状态、应用配置

### A2. 建立租户上下文解析机制

后端增加：

- 管理端 JWT 增加 `tenantId`
- 请求级 `TenantContext`
- 公开接口支持按域名或请求头解析租户
- 小程序接口支持按 `appId` 或固定租户配置解析租户

验收标准：

- 同一个接口在不同租户上下文下返回不同数据
- 无租户上下文时拒绝访问或走明确默认租户策略

### A3. 平台管理员与租户管理员分层

- 新增平台级后台角色
- 管理员账号增加作用域
- 平台管理员可管理租户
- 租户管理员只能访问本租户数据

验收标准：

- 平台管理员可查看全部租户
- 租户管理员无法跨租户查询数据

## 阶段 B：业务数据租户化

### B1. 运单模块租户化

- `waybill_order` 加 `tenant_id`
- `waybill_leg`、`waybill_track_event` 跟随父单隔离
- 所有运单查询、保存、删除都带租户过滤
- 公开查单按租户范围查询

验收标准：

- 不同租户可使用相同运单号规则时，需要明确唯一性策略
- 同租户内查询正常，跨租户不可见

### B2. 会员模块租户化

- `member_user` 加 `tenant_id`
- 手机号唯一约束改为 `tenant_id + phone`
- 微信 `openid` 唯一约束改为按应用或按租户作用域设计
- `member_waybill_relation` 加 `tenant_id`

验收标准：

- 不同租户可以存在相同手机号会员
- 会员只能看到本租户运单

### B3. 支付模块租户化

- `pay_order`、`refund_order`、`pay_transaction` 等加 `tenant_id`
- `pay_merchant_config` 改为租户内生效
- “当前商户”从全局唯一改为“每租户一个当前商户”
- 回调处理必须校验租户与商户归属

验收标准：

- A 公司支付回调不能更新 B 公司订单
- 每个租户都可独立切换自己的当前商户

### B4. 内容与新闻模块租户化

- `site_content_page` 加 `tenant_id`
- `news_article` 加 `tenant_id`
- 页面缓存 key 增加 `tenant_id`
- 发布逻辑按租户隔离

验收标准：

- 不同租户可拥有各自首页、新闻和联系页

## 阶段 C：站点和线路平台化

### C1. 去除 `PublicWebsiteService` 中的硬编码品牌信息

将以下内容迁移到租户配置：

- 品牌名
- Logo
- 联系电话
- 邮箱
- 社媒文案
- Footer 文案
- 默认导航

验收标准：

- 不修改代码即可切换不同租户的站点品牌信息

### C2. 服务线路改成租户可配置

- 移除固定 `taiwan` / `feizhou` / `international` 线路枚举依赖
- 新增租户线路表
- 线路页面由租户线路数据驱动
- 管理端增加线路配置入口

验收标准：

- 新租户可创建自己的线路，如“欧美空派”“中东专线”
- 公开线路页不再依赖固定路径枚举

### C3. 种子数据改造

- 将当前业务 seed 分拆为“平台基础 seed”与“租户样例 seed”
- 不再默认灌入某个具体公司品牌数据
- 增加租户初始化脚本

验收标准：

- 新环境初始化后，可先创建空租户，再按模板填充

## 阶段 D：前端与小程序平台化

### D1. 管理端平台化

- 登录后根据租户作用域加载菜单和数据
- 平台管理员增加租户管理页面
- 租户管理员只能看到本租户模块数据
- 页面中的品牌、线路、支付配置都走接口返回

验收标准：

- 管理端不再写死公司品牌和线路

### D2. 小程序平台化

需要明确模式二选一：

模式 1：每个租户一个独立小程序

- 租户绑定自己的 `appId`
- 后端按 `appId` 识别租户

模式 2：平台统一小程序，多租户切换

- 用户先选择公司或通过邀请码进入
- 后端保存当前租户上下文

建议当前优先采用模式 1，更符合现有微信支付与微信登录结构。

验收标准：

- 不同租户小程序登录、运单、支付各自隔离

## 阶段 E：运维与交付平台化

### E1. 审计和监控增加租户维度

- 登录日志加 `tenant_id`
- 业务操作日志加 `tenant_id`
- 支付异常统计按租户聚合
- 回调失败按租户定位

### E2. 配置中心化

- 微信支付配置
- 小程序配置
- 站点配置
- 联系方式
- 文件路径与证书配置

逐步从代码和环境散落项，迁移到“租户配置 + 安全存储”模式。

### E3. 租户开通流程

形成标准化开通流程：

1. 创建租户
2. 创建租户管理员
3. 初始化站点模板
4. 初始化线路模板
5. 绑定小程序应用
6. 配置支付商户
7. 验证公开域名与回调地址

## 6. 核心重构任务清单

## P0：必须先做

- 新增租户模型与迁移脚本
- 所有核心业务表增加 `tenant_id`
- 建立 `TenantContext`
- 后端查询统一增加租户过滤
- 管理端 JWT 增加 `tenantId`
- 支付商户改为租户内生效
- 页面缓存 key 增加租户维度

## P1：紧接着做

- 平台管理员后台
- 租户管理界面
- 站点配置去硬编码
- 服务线路配置化
- 小程序按租户 `appId` 识别
- 种子数据和初始化逻辑改造

## P2：增强项

- 审计日志租户化
- 限流与监控租户化
- 文件存储按租户分桶或分路径
- 报表按租户聚合
- 可选独立域名、独立证书、独立消息模板

## 7. 数据迁移策略

### 7.1 第一阶段迁移原则

由于当前项目本质上只有一个存量公司，可采用：

- 新建默认租户 `default`
- 所有历史数据回填到 `default.tenant_id`
- 新代码强制所有新数据写入 `tenant_id`

### 7.2 约束改造建议

原有全局唯一约束需改造为租户内唯一：

- `member_user.phone` -> `uk_member_user_tenant_phone`
- `waybill_order.main_tracking_no` -> `uk_waybill_order_tenant_main_tracking_no`
- `site_content_page.page_code` -> `uk_site_content_page_tenant_page_code`
- `news_article.slug` 如果后续有 slug，也应改为租户内唯一
- 支付商户编码 -> `tenant_id + merchant_code`

### 7.3 接口兼容策略

迁移早期可保留旧接口路径，但服务层必须已租户化。

不建议为了兼容保留“无租户默认全局查询”超过一个过渡版本。

## 8. 代码层改造建议

### 后端

- 新增 `tenant` 包：entity、mapper、service、controller
- 新增 `TenantContextHolder`
- `JwtTokenService` 增加 `tenantId`
- `SecurityConfig` 区分平台管理员和租户管理员
- Mapper 查询统一补 `tenant_id`
- 公共缓存 key 统一增加租户前缀

### 前端

- 登录态增加 `tenantId`、`tenantName`
- 增加平台管理入口
- 所有页面文案和配置项优先取接口配置
- 将服务线路从前端固定选项改为接口动态加载

### 小程序

- 增加租户应用配置读取
- 支付前校验当前租户应用与后端商户配置匹配
- 调试页增加租户上下文检查

## 9. 非目标

本轮不建议同时做以下大改：

- 一租户一数据库
- 多语言国际化全量重构
- 全模块微服务拆分
- 多品牌主题系统深度可视化装修

这些会显著扩大范围，影响当前平台化主线落地。

## 10. 验收标准

完成本次平台化改造后，至少应能验证以下场景：

### 场景 1：双租户后台隔离

- 创建租户 A、租户 B
- A 管理员只能看到 A 的运单、会员、支付、内容
- B 管理员只能看到 B 的数据

### 场景 2：双租户公开站点隔离

- A 域名返回 A 的品牌与页面
- B 域名返回 B 的品牌与页面

### 场景 3：双租户支付隔离

- A 支付商户只能用于 A 的订单
- B 的支付回调不能更新 A 的订单

### 场景 4：双租户小程序隔离

- A 小程序登录进入 A 业务空间
- B 小程序登录进入 B 业务空间

### 场景 5：运单号与会员账号租户内唯一

- 不同租户允许存在相同手机号
- 不同租户允许存在相同运单号，前提是公开查单和后台查询都有租户上下文

## 11. 实施顺序建议

建议按以下顺序推进，而不是同时改所有模块：

1. 建租户模型与上下文
2. 改后台鉴权与管理员作用域
3. 改运单、会员、支付主业务表
4. 改公开站点与内容模型
5. 改小程序与微信/支付映射
6. 改种子数据、缓存、监控、审计

## 12. 风险与注意事项

- 最大风险不是代码量，而是“哪些数据应该按租户隔离，哪些应该平台共享”定义不清
- 微信 `openid` 的唯一性必须结合 `appId` 设计，不能简单全局唯一
- 支付回调的租户识别要足够严格，否则会有串单风险
- 缓存如果不带租户前缀，会出现最隐蔽的数据串读问题
- 历史 SQL seed 和默认页面是当前最明显的单租户污染源，需要尽早拆分

## 13. 建议追加的后续文档

本计划落地前，建议再补 3 份配套文档：

- 《租户数据模型详细设计》
- 《租户上下文与鉴权流程设计》
- 《平台化数据迁移与回填脚本方案》

---

一句话结论：

当前项目已经具备“单公司业务系统”的完整骨架，但若要变成“多公司共享的平台”，核心不是继续堆功能，而是先把“租户模型、数据隔离、配置作用域、支付作用域、站点内容作用域”五件事做对。
