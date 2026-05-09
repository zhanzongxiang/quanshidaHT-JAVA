# qsd Admin

## 项目结构
- `backend`：Spring Boot 后端，包含管理端、会员端、支付与微信支付集成能力。
- `frontend`：Vue 3 管理后台，当前不是小程序前端。
- `miniapp`：新增的 `uni-app` 小程序前端骨架，用于会员端与支付端联调。

当前仓库现在已包含一个 `uni-app` 小程序骨架，但它的定位是联调骨架，不是最终上线版小程序产品。

## 当前已落地能力
- 管理员登录、权限、菜单、内容管理、新闻管理、运单管理、字典管理。
- 会员系统：会员注册、登录、资料维护、会员与运单绑定、会员查看自己的运单。
- 支付系统 v1：支付单、退款单、支付/退款回调日志、对账记录、后台支付管理页。
- 微信支付一期：小程序支付下单、支付回调、退款、对账下载、平台证书刷新。
- 商户模式：单商户运行，但支持后台维护多个商户配置，并切换当前生效商户。

## 小程序相关接口
- `POST /api/member/auth/register`
- `POST /api/member/auth/login`
- `POST /api/member/auth/wechat-login`
- `GET /api/member/profile`
- `PUT /api/member/profile`
- `PUT /api/member/profile/wechat`
- `GET /api/member/waybills`
- `GET /api/member/waybills/{id}`
- `POST /api/member/payments/prepare`
- `GET /api/member/payments`

## 微信支付一期运维说明

### 商户模式
- 系统允许维护多个商户配置，但同一时刻只有一个商户为 `active`。
- 新支付单会绑定创建时的商户快照，后续切换商户不会影响历史订单和退款链路。

### 真实微信支付必填配置
要进入真实微信支付链路，当前生效商户至少应完整配置：
- `appId`
- `appSecret`
- `mchId`
- `notifyUrl`
- `apiV3Key`
- `privateKeyPath`
- `merchantSerialNo`
- `platformCertificatePath`

后台支付页已经提供商户配置健康状态展示：
- `Ready`：商户配置完整，可用于真实微信支付。
- `Incomplete`：缺少关键字段，只适合 mock 或待补齐后再启用。

### 证书与回调
- 后端支持手动刷新当前商户的平台证书。
- 后端支持定时刷新平台证书，可通过 `app.wechat-pay.auto-refresh-platform-certificates` 控制。
- 支付和退款回调均支持日志记录、失败分类和后台统计查看。

### 对账
- 后端支持下载微信交易账单并生成本地对账记录。
- 后台支付页支持查看对账差异详情。
- 当真实商户配置不完整时，系统会保留手工对账兜底流程。

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

### 前端
```powershell
cd frontend
npm install
npm run dev
```

前端校验：
```powershell
cd frontend
npm run build
```

### 小程序骨架
```powershell
cd miniapp
npm install
npm run dev:mp-weixin
```

## 约束
- 管理端接口使用 `/api/**`。
- 会员端接口使用 `/api/member/**`。
- 会员 JWT 与管理员 JWT 必须隔离。
- Flyway 历史迁移只允许追加，不允许改写已执行版本。
- 支付敏感配置不得硬编码入仓库。
