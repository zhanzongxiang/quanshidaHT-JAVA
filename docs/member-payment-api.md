# 会员支付 API 文档

更新时间：2026-05-09

## 基本说明

- 本文档只描述会员端支付相关接口
- 接口前缀仍为：`/api/member/**`
- 使用会员 token 鉴权：`Authorization: Bearer <member-token>`
- 当前支付主通道为：`wechat_pay`

## 统一响应结构

```json
{
  "code": 0,
  "message": "OK",
  "data": {}
}
```

## 支付状态说明

- `pending`：待支付
- `paying`：支付中
- `paid`：已支付
- `refunding`：退款中
- `refunded`：已退款
- `closed`：已关闭
- `exception`：异常

## 1. 查询会员支付记录

- 方法：`GET`
- 路径：`/api/member/payments`

成功响应：

```json
{
  "code": 0,
  "message": "OK",
  "data": [
    {
      "id": 101,
      "orderNo": "PO202605090001",
      "merchantConfigId": 9,
      "merchantName": "Acme Merchant",
      "waybillId": 15,
      "waybillTrackingNo": "WB202605090001",
      "channel": "wechat_pay",
      "amountTotal": 88.5,
      "amountPaid": 88.5,
      "status": "paid",
      "description": "Waybill payment",
      "paidAt": "2026-05-09 11:00:00",
      "createdAt": "2026-05-09 10:55:00"
    }
  ]
}
```

字段说明：

- `id`：支付单 ID
- `orderNo`：支付单号
- `merchantConfigId`：商户配置 ID
- `merchantName`：商户名称
- `waybillId`：关联运单 ID
- `waybillTrackingNo`：关联运单号
- `channel`：支付渠道
- `amountTotal`：应付金额
- `amountPaid`：实付金额
- `status`：支付状态
- `description`：支付描述
- `paidAt`：支付完成时间
- `createdAt`：创建时间

## 2. 准备小程序支付

- 方法：`PUT`
- 路径：`/api/member/payments/prepare`

### 请求体

```json
{
  "waybillId": 15,
  "amountTotal": 88.5,
  "description": "Waybill payment",
  "channel": "wechat_pay"
}
```

字段说明：

- `waybillId`：必填，运单 ID
- `amountTotal`：必填，支付金额
- `description`：可选，最长 255
- `channel`：必填，建议固定传 `wechat_pay`

### 成功响应

```json
{
  "code": 0,
  "message": "OK",
  "data": {
    "payOrderId": 101,
    "orderNo": "PO202605090001",
    "status": "paying",
    "merchantConfigId": 9,
    "merchantName": "Acme Merchant",
    "appId": "wx-app-001",
    "timeStamp": "1710000000",
    "nonceStr": "nonce-123",
    "packageValue": "prepay_id=wx-prepay-001",
    "signType": "RSA",
    "paySign": "sign-123"
  }
}
```

字段说明：

- `payOrderId`：支付单 ID
- `orderNo`：支付单号
- `status`：当前状态，正常为 `paying`
- `merchantConfigId`：当前支付所使用商户 ID
- `merchantName`：当前支付所使用商户名称
- `appId`：小程序 `appId`
- `timeStamp`：微信支付参数
- `nonceStr`：微信支付参数
- `packageValue`：微信支付参数，对应 `package`
- `signType`：微信支付参数
- `paySign`：微信支付参数

## 小程序调用方式

拿到 `prepare` 响应后，小程序可按下面方式调用支付：

```js
wx.requestPayment({
  timeStamp: data.timeStamp,
  nonceStr: data.nonceStr,
  package: data.packageValue,
  signType: data.signType,
  paySign: data.paySign
})
```

## 常见失败场景

### 未绑定微信身份

HTTP 状态：`400`

```json
{
  "code": 4001,
  "message": "member wechat identity is not bound",
  "data": null
}
```

### 运单不属于当前会员

HTTP 状态：`404`

```json
{
  "code": 4040,
  "message": "member waybill not found",
  "data": null
}
```

### 参数校验失败

HTTP 状态：`400`

```json
{
  "code": 4002,
  "message": "waybillId: waybillId must not be null",
  "data": null
}
```

## 联调建议

1. 先确保会员已完成微信登录或微信绑定
2. 再查询可见运单列表
3. 针对运单发起 `prepare`
4. 前端调用 `wx.requestPayment`
5. 支付完成后调用 `/api/member/payments` 查询最新状态

## 说明

- 当前仓库只提供会员端支付 API，不包含小程序 UI 工程
- 支付成功与否最终以后端支付单状态为准
- 商户切换不会影响历史支付单，支付单会保留商户快照
