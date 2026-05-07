# qsd Admin

## 项目简介

这是一个面向运单管理与站点运营的后台项目，当前包含后台登录、菜单权限、首页内容管理、线路模板管理、新闻管理、运单管理、字典管理、全局站点设置，以及一组面向官网前台的公开只读接口。

当前版本规划新增“会员系统”，目标是让后台管理端可以管理会员，并让小程序侧通过独立会员接口完成注册、登录、资料维护和会员运单查询。

注意：

- 当前仓库不直接包含小程序页面代码
- 本次改造会优先交付“小程序可直接接入的后端接口 + 后台会员管理能力”

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
- 运单管理
- 会员管理
  - 会员列表
  - 会员状态维护
  - 会员绑定运单查看
- 全局配置
  - 导航设置
  - 页脚设置
  - 联系方式
  - 字典管理

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

### 运单管理

- `GET /api/waybills`
- `GET /api/waybills/{id}`
- `POST /api/waybills`
- `PUT /api/waybills/{id}`
- `DELETE /api/waybills/{id}`

### 字典管理

- `GET /api/dictionaries`
- `GET /api/dictionaries/options`
- `POST /api/dictionaries`
- `PUT /api/dictionaries/{id}`
- `DELETE /api/dictionaries/{id}`

### 会员管理

- `GET /api/admin/members`
- `GET /api/admin/members/{id}`
- `POST /api/admin/members`
- `PUT /api/admin/members/{id}`
- `PUT /api/admin/members/{id}/status`

## 小程序会员接口

以下接口为小程序或其他会员端使用，不能与后台管理员 JWT 混用：

- `POST /api/member/auth/register`
- `POST /api/member/auth/login`
- `GET /api/member/profile`
- `PUT /api/member/profile`
- `GET /api/member/waybills`
- `GET /api/member/waybills/{id}`

说明：

- 会员接口只返回当前会员可见数据
- 管理端接口仍然要求后台登录
- 后续如切换微信登录，可在现有会员接口层上扩展，不影响后台

## 官网公开接口

以下接口供官网前台直接读取，不需要管理员 JWT：

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
- 运单公开查询的状态文案由字典模块统一映射

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
cd E:\me\quanshidaHT-JAVA
.\backend\start-dev.ps1
```

如果 `8080` 已被占用：

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
- 运单存储在 `waybill_order`、`waybill_leg`、`waybill_track_event`
- 字典存储在 `sys_dict_item`
- 会员系统规划存储在 `member_user`、`member_waybill_relation`
- 历史迁移文件必须保持不可变

## 开发约束

- 前端统一走 `/api`，不要在业务代码中写死后端地址
- 后台 UI 统一使用 `Element Plus + Tailwind CSS`
- 固定模板页面不要做成自由拼版
- 新闻正文统一用区块化表单维护
- 官网不要直接复用后台 JWT 接口
- 小程序不要直接复用后台管理员 JWT 接口
- 运单状态、线路类型、会员状态等基础枚举统一由字典模块管理
