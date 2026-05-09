# uni-app 小程序开发计划

更新时间：2026-05-09

## 目标

基于当前已完成的会员端和支付端后端接口，新增一个 `uni-app` 小程序前端骨架，用于：

- 提前验证会员 API 可用性
- 提前验证小程序登录与支付接入形态
- 为后续正式小程序产品开发提供可运行结构

## 当前已落地范围

小程序目录：

- `miniapp/`

当前骨架已包含：

- 首页
- 手机号注册
- 手机号登录
- 微信登录入口
- 会员资料查看与编辑
- 运单列表
- 运单详情
- 支付记录列表
- 从运单详情发起支付准备

## 已适配的后端功能点

- `POST /api/member/auth/register`
- `POST /api/member/auth/login`
- `POST /api/member/auth/wechat-login`
- `GET /api/member/profile`
- `PUT /api/member/profile`
- `PUT /api/member/profile/wechat`
- `GET /api/member/waybills`
- `GET /api/member/waybills/{id}`
- `GET /api/member/payments`
- `PUT /api/member/payments/prepare`

## 当前定位

- 是联调骨架，不是最终上线版
- 重点是接口对齐和流程闭环
- 不追求现在就把视觉和产品细节做满

## 下一阶段计划

### 第一阶段

- 补齐 `uni-app` 基础目录与页面骨架
- 封装会员与支付 API
- 接入会员 token 存储与资料拉取
- 打通运单详情发起支付准备

### 第二阶段

- 细化微信登录与首次绑定手机号流程
- 增加支付成功、失败、取消、处理中反馈页
- 增加支付结果主动轮询
- 提升错误提示与空状态

### 第三阶段

- 补品牌化 UI
- 补表单校验细节
- 补提交审核前的微信小程序配置
- 联动真实微信支付环境做完整回归

## 当前明确未完成

- 没有做正式视觉稿还原
- 没有做完整手机号授权产品流程
- 没有做退款前台入口
- 没有做消息通知、订单中心等扩展能力
- 尚未验证微信开发者工具编译与真机预览

## 当前已验证

- `miniapp/` 依赖已可安装
- `vue-tsc --noEmit` 已通过

## 相关文档

- [会员端 API 文档](./member-api.md)
- [会员支付 API 文档](./member-payment-api.md)
- [小程序与支付联调清单](./mini-program-payment-integration-checklist.md)
