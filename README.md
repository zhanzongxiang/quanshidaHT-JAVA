# qsd Admin

## 项目简介

这是企业官网配套的后台管理系统，当前包含后台登录、菜单权限、首页内容管理、线路模板管理、新闻管理、全局站点设置，以及一组面向官网前台的公开只读接口。

## 技术栈

### 前端

- Vue 3
- TypeScript
- Vite
- Vue Router
- Pinia
- Element Plus
- Tailwind CSS

### 后端

- Java 21
- Spring Boot 3
- Spring Security
- JWT
- MyBatis-Plus
- Flyway

## 当前主要模块

- 工作台
- 页面管理
  - 首页配置
  - 线路页面
  - 新闻资讯
- 全局配置
  - 导航设置
  - 页脚设置
  - 联系方式

## 管理端接口

### 认证接口

- `POST /api/auth/login`
- `GET /api/auth/me`
- `GET /api/health`

### 首页内容管理

- `GET /api/content/home`
- `PUT /api/content/home/draft`
- `PUT /api/content/home/publish`

### 线路模板管理

- `GET /api/content/service-lines`
- `GET /api/content/service-lines/{code}`
- `PUT /api/content/service-lines/{code}/draft`
- `PUT /api/content/service-lines/{code}/publish`

### 新闻管理

- `GET /api/news`
- `GET /api/news/{id}`
- `POST /api/news`
- `PUT /api/news/{id}`
- `DELETE /api/news/{id}`

## 官网公开接口

以下接口供企业官网前台直接读取，不需要 JWT：

- `GET /api/site`
- `GET /api/pages/home`
- `GET /api/pages/about`
- `GET /api/pages/contact`
- `GET /api/pages/service-line/{key}`
- `GET /api/pages/news`
- `GET /api/pages/news/{id}`
- `GET /api/tracking/{trackingNo}`

说明：

- 公开接口只返回已发布内容
- 后台管理接口仍然要求登录
- 当前 `site`、`about`、`contact`、`tracking` 仍可能由后端默认数据提供

## 本地启动

### 前端

```bash
cd frontend
npm install
npm run dev
```

默认开发地址：

- `http://localhost:5174`

### 后端

Windows 环境建议使用项目脚本启动：

```powershell
cd F:\work\quanshidaHT-JAVA
.\backend\start-dev.ps1
```

如果 `8080` 已占用：

```powershell
.\backend\start-dev.ps1 -Port 8081
```

也可以使用 Maven 打包验证：

```powershell
cd backend
mvn -DskipTests package
```

## 数据与迁移

- 首页和线路模板内容存储在 `site_content_page`
- 新闻存储在 `news_article`
- 历史迁移文件必须保持不可变
- 示例数据文件位于：
  - `backend/sql/home_content_seed.sql`
  - `backend/sql/full_business_seed.sql`

## 开发约束

- 前端统一走 `/api`，不要在业务代码中写死后端地址
- 后台 UI 统一使用 `Element Plus + Tailwind CSS`
- 固定模板页面不要做成自由拼版
- 新闻正文统一用区块化表单维护
- 官网不要直接复用后台 JWT 接口
