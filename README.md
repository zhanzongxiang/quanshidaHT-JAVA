# qsd Admin Backend

## 项目概况

这是为 `qsd-gw` 官网配套建设的后台管理系统，一期目标不是完整物流 ERP，而是先支撑官网运营、客户线索和运单轨迹维护。

后台建设和执行边界以 `AGENT.md` 为最高优先级约束；本文件用于帮助后续 agent 或开发者快速理解后台项目的范围、模块和数据方向。

## 一期范围

一期只覆盖以下能力：

- 官网内容管理
- 新闻管理
- 素材库管理
- 客户线索管理
- 运单与轨迹管理
- 客户档案管理
- 账号权限管理
- 操作日志
- 系统设置

## 一期不做

除非后续明确追加需求，否则不纳入一期：

- 订单管理
- 报价管理
- 财务结算
- 发票管理
- 审批流
- 多组织/多租户
- 多仓管理
- 复杂报表中心
- 风控系统
- 自动化工作流

## 后台角色

一期默认使用三类角色：

- `超级管理员`
- `运营`
- `客服`

权限优先覆盖：

- 菜单可见性
- 页面访问权限
- 关键写操作权限

## 后台菜单

建议一级菜单如下：

- `工作台`
- `内容管理`
- `新闻管理`
- `素材库`
- `线索管理`
- `运单管理`
- `客户管理`
- `账号权限`
- `操作日志`
- `系统设置`

## 核心数据域

后台数据按以下域拆分：

- `内容域`
  - 官网页面内容、Banner、导航、页脚、联系信息。
- `新闻域`
  - 新闻文章、分类、发布状态、排序。
- `素材域`
  - 图片与文件资源。
- `线索域`
  - 官网咨询线索、跟进记录、负责人。
- `客户域`
  - 客户资料、联系方式、关联线索、关联运单。
- `运单域`
  - 运单基础信息、状态、轨迹节点、异常件。
- `权限域`
  - 用户、角色、菜单权限、操作权限。
- `审计域`
  - 登录日志、操作日志。

## 推荐核心对象

一期建议至少围绕这些对象建模：

- `admin_user`
- `admin_role`
- `permission`
- `content_page`
- `content_block`
- `news_article`
- `media_asset`
- `lead`
- `lead_follow_record`
- `customer`
- `shipment`
- `shipment_tracking_event`
- `operation_log`
- `login_log`
- `system_setting`

## 技术与语言约束

后台项目默认使用以下固定技术栈：

- 前端：`Vue 3 + TypeScript + Vite + Vue Router + Pinia + Element Plus`
- 后端：`Java 21 + Spring Boot 3`
- 数据库：`MySQL 8`
- 缓存：`Redis`
- 接口风格：`REST API`

语言约束：

- 文档说明使用 `中文`
- 代码注释使用 `中文`
- 提交说明使用 `中文`
- 表名、字段名、接口路径、代码标识符使用 `英文`

实现原则：

- 不要混用多套后台 UI 框架。
- 不要把官网里的静态模拟数据方案直接搬进后台。
- 所有业务页面都要围绕真实数据模型和接口设计。
- 后端保持单体应用优先，一期不主动拆微服务。

## 建设顺序

后台建议按以下顺序建设：

1. 登录、布局、路由、权限壳子
2. 账号权限
3. 工作台
4. 内容管理
5. 新闻管理
6. 素材库
7. 线索管理
8. 运单管理
9. 客户管理
10. 操作日志
11. 系统设置

## 验证要求

每次后台改动后，至少验证：

- 项目可构建
- 登录可用
- 权限拦截有效
- 菜单访问正确
- 列表页可加载
- 表单校验有效
- 关键写操作有反馈
- 关键写操作有日志

## 相关文档

- `AGENT.md`
  - 后台项目最高优先级执行约束。
- `PLAN.md`
  - 后台实施路径和阶段计划。
- `TODO.md`
  - 后台任务池与待办清单。

## 当前代码结构

- `frontend`
  - Vue 3 + TypeScript + Vite + Vue Router + Pinia + Element Plus
  - 已实现登录页、主布局、路由守卫、动态菜单渲染（依赖 `/api/auth/me`）
- `backend`
  - Spring Boot 3 + JWT + MyBatis-Plus + Flyway
  - 已实现 `/api/health`、`/api/auth/login`、`/api/auth/me`
- `infra/docker-compose.yml`
  - MySQL 8 + Redis 7.2 本地依赖

## 本地启动

1. 启动依赖（需已安装 Docker）
   - `docker compose -f infra/docker-compose.yml up -d`
2. 启动后端（需 Java 21）
   - `mvn -f backend/pom.xml spring-boot:run`
3. 启动前端
   - `npm --prefix frontend run dev`

默认测试账号：

- 用户名：`admin`
- 密码：`admin123`
