# 会员端 API 文档

更新时间：2026-05-09

## 基本说明

- 会员端接口前缀：`/api/member/**`
- 认证方式：`Authorization: Bearer <member-token>`
- 会员 token 与后台管理员 token 不可混用
- 所有接口统一响应结构：

```json
{
  "code": 0,
  "message": "OK",
  "data": {}
}
```

## 错误码

- `0`：成功
- `4001`：业务参数错误或业务状态不允许
- `4002`：请求参数校验失败
- `4040`：资源不存在
- `5000`：服务端异常

常见错误示例：

- `member is disabled`
- `member is pending approval`
- `member wechat identity is not bound`
- `member waybill not found`
- `wechat openid is already bound to another member`

## 1. 会员注册

- 方法：`POST`
- 路径：`/api/member/auth/register`
- 是否鉴权：否

请求体：

```json
{
  "phone": "13800138000",
  "password": "123456",
  "nickname": "张三",
  "fullName": "张三"
}
```

字段说明：

- `phone`：手机号，必须为 11 位大陆手机号
- `password`：密码，必填，最长 64
- `nickname`：昵称，可选，最长 64
- `fullName`：姓名，可选，最长 64

成功响应 `data`：

```json
{
  "accessToken": "jwt-token",
  "tokenType": "Bearer"
}
```

## 2. 会员登录

- 方法：`POST`
- 路径：`/api/member/auth/login`
- 是否鉴权：否

请求体：

```json
{
  "phone": "13800138000",
  "password": "123456"
}
```

成功响应 `data`：

```json
{
  "accessToken": "jwt-token",
  "tokenType": "Bearer"
}
```

## 3. 微信登录

- 方法：`POST`
- 路径：`/api/member/auth/wechat-login`
- 是否鉴权：否

说明：

- 后端会用 `code` 调微信 `code2Session`
- 若 `openid` 已绑定会员，则直接登录
- 若未绑定：
  - 传了 `phone` 且手机号会员存在，则绑定该会员
  - 否则自动创建新会员

请求体：

```json
{
  "code": "wx-login-code",
  "phone": "13800138000",
  "nickname": "wx-user",
  "fullName": "Wechat User"
}
```

字段说明：

- `code`：微信登录 code，必填
- `phone`：手机号，可选
- `nickname`：昵称，可选
- `fullName`：姓名，可选

成功响应 `data`：

```json
{
  "accessToken": "jwt-token",
  "tokenType": "Bearer"
}
```

## 4. 查询会员资料

- 方法：`GET`
- 路径：`/api/member/profile`
- 是否鉴权：是

成功响应 `data`：

```json
{
  "id": 1,
  "phone": "13800138000",
  "wechatOpenid": "openid-001",
  "wechatUnionid": "unionid-001",
  "wechatBindTime": "2026-05-09 10:00:00",
  "nickname": "张三",
  "fullName": "张三",
  "avatarUrl": "https://example.com/avatar.png",
  "status": "active",
  "createdAt": "2026-05-09 09:00:00"
}
```

## 5. 更新会员资料

- 方法：`PUT`
- 路径：`/api/member/profile`
- 是否鉴权：是

请求体：

```json
{
  "nickname": "新昵称",
  "fullName": "新姓名",
  "avatarUrl": "https://example.com/avatar-new.png"
}
```

字段说明：

- `nickname`：最长 64
- `fullName`：最长 64
- `avatarUrl`：最长 500

成功响应 `data` 结构同“查询会员资料”。

## 6. 绑定微信身份

- 方法：`PUT`
- 路径：`/api/member/profile/wechat`
- 是否鉴权：是

请求体：

```json
{
  "openid": "openid-001",
  "unionid": "unionid-001"
}
```

字段说明：

- `openid`：必填，最长 64
- `unionid`：可选，最长 64

成功响应 `data` 结构同“查询会员资料”。

## 7. 查询会员可见运单列表

- 方法：`GET`
- 路径：`/api/member/waybills`
- 是否鉴权：是

成功响应 `data`：

```json
[
  {
    "id": 10,
    "mainTrackingNo": "WB202605090001",
    "referenceNo": "REF-001",
    "customerName": "张三",
    "destinationCountry": "日本",
    "destinationCity": "东京",
    "currentStatus": "in_transit",
    "currentNode": "大阪清关中",
    "updatedAt": "2026-05-09 10:30:00"
  }
]
```

## 8. 查询会员可见运单详情

- 方法：`GET`
- 路径：`/api/member/waybills/{id}`
- 是否鉴权：是

成功响应 `data`：

```json
{
  "id": 10,
  "mainTrackingNo": "WB202605090001",
  "referenceNo": "REF-001",
  "customerName": "张三",
  "destinationCountry": "日本",
  "destinationCity": "东京",
  "currentStatus": "in_transit",
  "currentNode": "大阪清关中",
  "originWarehouse": "深圳一仓",
  "cargoDescription": "服装",
  "packageCount": 2,
  "weightKg": 3.5,
  "updatedAt": "2026-05-09 10:30:00",
  "legs": [
    {
      "legNo": 1,
      "legType": "air",
      "carrierName": "XX 航空",
      "trackingNo": "LEG001",
      "fromNode": "深圳",
      "toNode": "大阪",
      "legStatus": "in_transit",
      "transferFlag": false,
      "departureTime": "2026-05-08 12:00:00",
      "arrivalTime": null,
      "remark": ""
    }
  ],
  "events": [
    {
      "legId": 1,
      "sortNo": 1,
      "eventTime": "2026-05-08 15:00:00",
      "eventStatus": "in_transit",
      "eventDescription": "航班已起飞",
      "eventLocation": "深圳",
      "visibleToCustomer": true
    }
  ]
}
```

`legs` 字段说明：

- `legNo`：分段序号
- `legType`：分段类型
- `carrierName`：承运商
- `trackingNo`：分段运单号
- `fromNode`：起点
- `toNode`：终点
- `legStatus`：分段状态
- `transferFlag`：是否中转
- `departureTime`：出发时间，格式 `yyyy-MM-dd HH:mm:ss`
- `arrivalTime`：到达时间，格式 `yyyy-MM-dd HH:mm:ss`
- `remark`：备注

`events` 字段说明：

- `legId`：所属分段号，可能为空
- `sortNo`：排序号
- `eventTime`：事件时间，格式 `yyyy-MM-dd HH:mm:ss`
- `eventStatus`：事件状态
- `eventDescription`：事件描述
- `eventLocation`：事件地点
- `visibleToCustomer`：是否前台可见

## 9. 查询会员支付记录

- 方法：`GET`
- 路径：`/api/member/payments`
- 是否鉴权：是

成功响应 `data`：

```json
[
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
```

## 10. 准备小程序支付

- 方法：`PUT`
- 路径：`/api/member/payments/prepare`
- 是否鉴权：是

说明：

- 当前会员必须已绑定微信身份
- 目标运单必须属于当前会员可见范围

请求体：

```json
{
  "waybillId": 15,
  "amountTotal": 88.5,
  "description": "Waybill payment",
  "channel": "wechat_pay"
}
```

字段说明：

- `waybillId`：运单 ID，必填
- `amountTotal`：支付金额，必填
- `description`：支付描述，可选，最长 255
- `channel`：支付渠道，必填，当前建议传 `wechat_pay`

成功响应 `data`：

```json
{
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
```

小程序可直接使用这些字段调用 `wx.requestPayment`。

## 联调建议

- 先走 `wechat-login` 获取会员 token
- 再调用 `GET /api/member/profile` 检查微信绑定状态
- 用 `GET /api/member/waybills` 选择可支付运单
- 调用 `PUT /api/member/payments/prepare`
- 再调用 `wx.requestPayment`
- 支付后用 `GET /api/member/payments` 轮询支付结果
