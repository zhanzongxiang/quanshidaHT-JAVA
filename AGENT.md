# qsd Admin 项目规范

## 项目定位

这是企业官网配套的后台管理系统，当前重点是支持官网内容运营、新闻管理、线路模板管理、运单管理、基础站点设置和公开数据接口，不以完整 ERP 为目标。

## 技术约束

- 后台前端统一使用 `Vue 3 + TypeScript + Vite + Vue Router + Pinia`
- UI 方案固定为 `Element Plus + Tailwind CSS`
- 后端统一使用 `Java 21 + Spring Boot 3 + Spring Security + JWT + MyBatis-Plus + Flyway`
- 前端业务请求统一走 `/api`
- 开发环境通过 Vite 代理转发到 `VITE_API_PROXY_TARGET`
- 默认脚手架残留组件、资源和未使用代码不保留，确认无引用后应及时删除

## 后台前端规范

- 根路由 `/` 默认跳转到 `/dashboard`
- 未登录访问后台路由时，统一跳转到 `/login?redirect=当前地址`
- 已登录访问 `/login` 时，直接回到 `/dashboard`
- 登录成功后必须先完成 `/api/auth/me` 和动态菜单注入，再进入目标页
- 后台布局保持左侧菜单与右侧内容区等高
- 只允许右侧内容区内部滚动，不使用全局滚动条
- 前端开发端口默认使用 `5174`

## 首页内容管理规范

- 首页内容管理页面位于 `frontend/src/views/ContentView.vue`
- 首页内容持久化使用后端接口：
  - `GET /api/content/home`
  - `PUT /api/content/home/draft`
  - `PUT /api/content/home/publish`
- 首页数据存储在 `site_content_page` 表中
- 首页表单当前按以下结构维护：
  - `hero`
  - `trackingSection`
  - `businessSection`
  - `processSection`
  - `promiseSection`
  - `newsPreviewSection`
  - `seo`
- 历史草稿升级时必须做字段兼容，不能因旧数据缺字段导致页面渲染失败

## 新闻管理规范

- 新闻管理使用区块化表单，不再使用单一大文本正文
- 当前支持的正文区块类型：
  - `paragraph`
  - `heading`
  - `image`
  - `image_caption`
- 后端新闻管理接口保持在 `/api/news/*`
- 前端区块数据允许序列化到后端 `content` 字段中保存

## 运单与字典规范

- 运单管理页面位于 `frontend/src/views/WaybillView.vue`
- 运单页面、按钮、表格列名、表单项、提示消息统一使用中文
- 运单状态、线路类型、分段状态等基础枚举统一收敛到字典模块，不再在页面或服务中长期硬编码
- 数据库存储继续保留稳定英文编码值，例如 `created`、`direct`、`pending`
- 前后端显示层统一通过字典标签映射成中文
- 公开运单查询接口返回的状态文案也必须走字典映射

## 字典管理规范

- 后台必须提供字典管理页面，支持查看、新增、编辑、删除非内置字典项
- 字典管理接口位于：
  - `GET /api/dictionaries`
  - `GET /api/dictionaries/options`
  - `POST /api/dictionaries`
  - `PUT /api/dictionaries/{id}`
  - `DELETE /api/dictionaries/{id}`
- 字典表使用 `sys_dict_item`
- 内置字典项允许编辑标签、排序、启用状态和备注，不允许修改编码值或删除
- 业务页面优先通过字典接口读取选项

## 线路模板管理规范

- 线路页面使用固定模板表单，不允许自由拼装布局
- 当前线路模板编辑接口位于 `/api/content/service-lines/*`
- 当前线路模板结构按以下字段维护：
  - `key`
  - `eyebrow`
  - `title`
  - `subtitle`
  - `description`
  - `heroImage`
  - `heroTags`
  - `metrics`
  - `highlights`
  - `processSteps`
  - `scope`
  - `support`
  - `cta`
- 保存草稿允许内容不完整
- 发布时必须通过必填项和模块完整性校验

## 站点设置规范

- 导航、页脚、联系方式属于全局配置，不挂在单页内容表单中
- 联系方式页面为结构化编辑模块，不是简单键值表单
- 联系方式模块至少包含：
  - Hero 文案
  - 联系卡片
  - 办公时间
  - 服务承诺
  - CTA 按钮

## 公开官网接口规范

- 官网公开接口与后台 JWT 管理接口必须分离
- 公开只读接口统一放在：
  - `/api/site`
  - `/api/pages/**`
  - `/api/tracking/**`
- 公开接口只允许返回已发布数据，不允许暴露草稿
- 后台管理接口继续要求登录认证

## 安全与迁移规范

- Spring Security 仅放行明确声明的公开接口
- Flyway 已执行迁移必须保持不可变，不能直接修改历史迁移文件
- 菜单、权限、文案调整如果影响已执行迁移，必须新增迁移文件
- 本地 Windows 启动后端优先使用 `backend/start-dev.ps1` 或 `backend/start-dev.cmd`
- 本地开发使用 `Java 21`

## 验证要求

- 前端改动后至少保证 `npm run build` 通过
- 后端改动后至少保证 `mvn -DskipTests package` 通过
- 涉及登录、权限、路由、内容发布、公开接口时，需要做对应功能回归
