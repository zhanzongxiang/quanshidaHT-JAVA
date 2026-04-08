# qsd Admin Backend

## 项目概况

这是 `qsd-gw` 官网配套的后台管理系统。一期优先支持官网内容运营、客户线索和运单轨迹维护，不以完整 ERP 为目标。
项目边界和实现约束以 [AGENT.md](/F:/work/quanshidaHT-JAVA/AGENT.md) 为准。

## 当前结构

- `frontend`
  - Vue 3 + TypeScript + Vite + Vue Router + Pinia
  - UI 方案：Element Plus + Tailwind CSS
  - 已实现登录页、后台布局、路由守卫、动态菜单注入
  - 已实现首页内容管理表单，当前保存/发布使用本地存储 mock
- `backend`
  - Spring Boot 3 + JWT + MyBatis-Plus + Flyway
  - 已实现 `/api/health`、`/api/auth/login`、`/api/auth/me`
- `infra/docker-compose.yml`
  - 本地依赖：MySQL 8 + Redis 7.2

## 前端关键约定

- `/` 默认重定向到 `/dashboard`
- 未登录访问后台路由时，统一跳转到 `/login?redirect=当前地址`
- 已登录访问 `/login` 时，自动回到 `/dashboard`
- 登录成功后先完成 `/api/auth/me` 和动态路由注入，再进入目标页
- 开发环境接口统一走 `/api`，由 Vite 代理到 `VITE_API_PROXY_TARGET`

## 首页内容管理

当前首页内容管理位于 `frontend/src/views/ContentView.vue`，数据结构定义在 `frontend/src/types/content.ts`，本地 mock 读写在 `frontend/src/api/content.ts`。

已实现内容：

- Banner 图片组上传、预览、删除、设为首图
- 首图主标题、次要内容
- 两个 CTA 按钮名称与跳转链接
- 运单追踪模块开关与标题
- 主营业务模块开关、标题、描述和多个业务小模块
- 联系转化区
- SEO 设置
- 草稿保存、发布、重置
- 右侧 Banner 预览摘要

约束：

- 内容管理页必须完整显示顶部摘要、左侧表单区和右侧预览区
- 内容结构升级时必须兼容浏览器中已有的历史草稿数据
- 后台布局使用右侧内容区内部滚动，避免整页全局滚动
- 后续接入后端时，优先替换 `frontend/src/api/content.ts`

## 本地启动

1. 启动依赖服务
   - `docker compose -f infra/docker-compose.yml up -d`
2. 启动后端
   - `mvn -f backend/pom.xml spring-boot:run`
3. 启动前端
   - `npm --prefix frontend run dev`

如果修改了 `frontend/vite.config.ts`、`frontend/postcss.config.js`、`frontend/tailwind.config.js` 或 `.env` 文件，需要重启前端开发服务。

## 默认测试账号

- 用户名：`admin`
- 密码：`admin123`
