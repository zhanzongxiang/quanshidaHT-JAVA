# qsd Miniapp

`miniapp/` 是为当前后端会员端与支付端 API 配套搭建的 `uni-app` 微信小程序前端骨架。

## 当前覆盖范围

- 会员手机号注册与登录
- 微信登录入口
- 会员资料查看与编辑
- 会员可见运单列表与详情
- 会员支付记录列表
- 从运单详情发起支付准备并调用 `uni.requestPayment`

## 当前定位

- 这是联调骨架，不是最终上线版小程序
- 重点是先把接口对齐、页面骨架和支付链路打通
- 视觉、交互和正式运营配置仍需后续完善

## 构建状态

- `npm run type-check` 已通过
- `npm run build:mp-weixin` 已通过
- 微信小程序构建输出目录：
  - `dist/build/mp-weixin`

## 目录结构

- `src/pages/index`：小程序首页
- `src/pages/auth`：登录、注册
- `src/pages/waybill`：运单列表、运单详情
- `src/pages/payment`：支付记录、支付结果
- `src/pages/profile`：会员资料
- `src/api`：会员端与支付端 API 封装
- `src/stores`：会员会话与资料状态
- `src/utils`：HTTP、鉴权守卫

## 启动方式

```powershell
cd miniapp
npm install
npm run dev:mp-weixin
```

如果需要生成可直接导入微信开发者工具的产物：

```powershell
npm run build:mp-weixin
```

## 环境变量

可通过 `VITE_API_BASE_URL` 指定后端地址，例如：

```powershell
$env:VITE_API_BASE_URL="http://127.0.0.1:8080"
```

## 对接文档

- [微信开发者工具导入说明](../docs/miniapp-wechat-devtools.md)
- [会员端 API 文档](../docs/member-api.md)
- [会员支付 API 文档](../docs/member-payment-api.md)
- [小程序与支付联调清单](../docs/mini-program-payment-integration-checklist.md)
