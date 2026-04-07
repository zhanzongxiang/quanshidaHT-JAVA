# qsd Admin Backend

## 项目概况

这是 `qsd-gw` 官网配套的后台管理系统。一期优先支持官网运营、客户线索和运单轨迹维护，不以完整 ERP 为目标。

项目边界和实现约束以 `AGENT.md` 为准，本文档用于帮助开发者快速理解当前结构、启动方式和关键约定。

## 一期范围

- 官网内容管理
- 新闻管理
- 素材库管理
- 客户线索管理
- 运单与轨迹管理
- 客户档案管理
- 账号权限管理
- 操作日志
- 系统设置

## 当前代码结构

- `frontend`
  - Vue 3 + TypeScript + Vite + Vue Router + Pinia + Element Plus
  - 已实现登录页、基础布局、路由守卫、动态菜单渲染
- `backend`
  - Spring Boot 3 + JWT + MyBatis-Plus + Flyway
  - 已实现 `/api/health`、`/api/auth/login`、`/api/auth/me`
- `infra/docker-compose.yml`
  - 本地依赖：MySQL 8 + Redis 7.2

## 前端路由约定

- `/` 默认重定向到 `/dashboard`
- 未登录访问 `/dashboard` 或其他后台路由时，统一跳转到 `/login?redirect=当前地址`
- 已登录访问 `/login` 时，自动跳回 `/dashboard`
- `404` 只用于动态菜单路由注入后仍然无法匹配的地址，不能作为未登录访问后台地址时的默认落点

## 本地启动

1. 启动依赖服务：
   - `docker compose -f infra/docker-compose.yml up -d`
2. 启动后端：
   - `mvn -f backend/pom.xml spring-boot:run`
3. 启动前端：
   - `npm --prefix frontend run dev`

## 默认测试账号

- 用户名：`admin`
- 密码：`admin123`

## 相关文档

- `AGENT.md`：最高优先级实现约束
- `PLAN.md`：阶段实施计划
- `TODO.md`：任务池与待办项
