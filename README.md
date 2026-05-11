# qsd Admin

## 项目结构

- `backend`：Spring Boot 后端，包含管理端、会员端、支付与微信支付集成能力
- `frontend`：Vue 3 管理后台
- `miniapp`：新增的 `uni-app` 微信小程序前端骨架，用于会员端与支付端联调

当前仓库已经包含一个可构建的 `uni-app` 小程序骨架，但它的定位是联调版，不是最终上线版小程序产品。

## 当前已落地能力

- 管理员登录、权限、菜单、内容管理、新闻管理、运单管理、字典管理
- 会员系统：会员注册、登录、资料维护、会员与运单绑定、会员查看本人运单
- 支付系统 v1：支付单、退款单、回调日志、对账记录、后台支付管理页
- 微信支付一期：小程序支付准备、支付回调、退款、对账下载、平台证书刷新
- 小程序骨架：登录、注册、资料、运单、支付记录、支付准备入口

## 小程序相关接口

- `POST /api/member/auth/register`
- `POST /api/member/auth/login`
- `POST /api/member/auth/wechat-login`
- `GET /api/member/profile`
- `PUT /api/member/profile`
- `PUT /api/member/profile/wechat`
- `GET /api/member/waybills`
- `GET /api/member/waybills/{id}`
- `PUT /api/member/payments/prepare`
- `GET /api/member/payments`

## 小程序当前状态

- 小程序目录：`miniapp/`
- `npm run type-check` 已通过
- `npm run build:mp-weixin` 已通过
- 微信小程序构建输出目录：`miniapp/dist/build/mp-weixin`

## 本地启动

### 后端

```powershell
cd E:\me\quanshidaHT-JAVA
.\backend\start-dev.ps1
```

后端构建或测试：

```powershell
cd backend
mvn -DskipTests package
mvn test
```

### 管理端

```powershell
cd frontend
npm install
npm run dev
```

管理端构建：

```powershell
cd frontend
npm run build
```

### 小程序

```powershell
cd miniapp
npm install
npm run dev:mp-weixin
```

生成微信小程序构建产物：

```powershell
cd miniapp
npm run build:mp-weixin
```

## 约束

- 管理端接口使用 `/api/**`
- 会员端接口使用 `/api/member/**`
- 会员 JWT 与管理员 JWT 必须隔离
- Flyway 历史迁移只允许追加，不允许改写已执行版本
- 支付敏感配置不得硬编码入仓库
