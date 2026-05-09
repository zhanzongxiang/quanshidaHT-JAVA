# 真实微信支付配置准备说明

更新时间：2026-05-09

## 目标

在不改代码的前提下，通过环境变量切换项目到真实微信支付联调模式。

## 配置入口

项目当前通过 [application.yml](/E:/me/quanshidaHT-JAVA/backend/src/main/resources/application.yml:1) 读取以下环境变量：

- `WECHAT_PAY_MOCK_ENABLED`
- `WECHAT_PAY_AUTO_REFRESH_PLATFORM_CERTIFICATES`
- `WECHAT_PAY_CERTIFICATE_STORE_DIR`
- `WECHAT_PAY_CERTIFICATE_REFRESH_HOURS`
- `WECHAT_PAY_MERCHANT_NAME`
- `WECHAT_PAY_APP_ID`
- `WECHAT_PAY_APP_SECRET`
- `WECHAT_PAY_MCH_ID`
- `WECHAT_PAY_NOTIFY_URL`
- `WECHAT_PAY_API_V3_KEY`
- `WECHAT_PAY_PRIVATE_KEY_PATH`
- `WECHAT_PAY_MERCHANT_SERIAL_NO`
- `WECHAT_PAY_PLATFORM_CERTIFICATE_PATH`

对应模板文件：

- [backend/.env.wechat-pay.example](/E:/me/quanshidaHT-JAVA/backend/.env.wechat-pay.example:1)

## 最关键的开关

要启用真实微信支付，必须设置：

```env
WECHAT_PAY_MOCK_ENABLED=false
```

如果这个值仍然是 `true`，项目会继续走 `MockWechatPayGateway`，不会打到微信真实接口。

## 必填项

以下字段缺一不可，否则真实支付链路无法完整工作：

- `WECHAT_PAY_APP_ID`
- `WECHAT_PAY_APP_SECRET`
- `WECHAT_PAY_MCH_ID`
- `WECHAT_PAY_NOTIFY_URL`
- `WECHAT_PAY_API_V3_KEY`
- `WECHAT_PAY_PRIVATE_KEY_PATH`
- `WECHAT_PAY_MERCHANT_SERIAL_NO`
- `WECHAT_PAY_PLATFORM_CERTIFICATE_PATH`

## 字段说明

### `WECHAT_PAY_APP_ID`

- 小程序 `appId`
- 必须和发起支付的小程序一致

### `WECHAT_PAY_APP_SECRET`

- 小程序 `appSecret`
- 用于 `code2Session`

### `WECHAT_PAY_MCH_ID`

- 微信支付商户号
- 必须和商户证书、商户私钥对应

### `WECHAT_PAY_NOTIFY_URL`

- 支付回调地址
- 当前代码会基于这个地址推导退款回调地址
- 如果此值以 `/wechat` 结尾，退款回调会自动使用 `/wechat-refund`

### `WECHAT_PAY_API_V3_KEY`

- 微信支付 APIv3 Key
- 应为 32 位字符串

### `WECHAT_PAY_PRIVATE_KEY_PATH`

- 商户私钥文件路径
- 当前进程必须可读

### `WECHAT_PAY_MERCHANT_SERIAL_NO`

- 商户证书序列号

### `WECHAT_PAY_PLATFORM_CERTIFICATE_PATH`

- 微信平台证书文件路径
- 当前进程必须可读

## 建议目录

建议不要把真实证书和私钥直接放在仓库目录里。更稳妥的做法是：

- 私钥放在独立安全目录
- 平台证书放在独立安全目录
- 只把路径通过环境变量传给应用

示例：

```env
WECHAT_PAY_PRIVATE_KEY_PATH=E:/secure/wechatpay/apiclient_key.pem
WECHAT_PAY_PLATFORM_CERTIFICATE_PATH=E:/secure/wechatpay/wechatpay_platform.pem
```

## 回调地址要求

支付回调：

- `POST /api/payment/callback/wechat`

退款回调：

- `POST /api/payment/callback/wechat-refund`

要求：

- 必须能被微信公网访问
- 不能只写本地 `localhost`
- 反向代理需保留请求头

## PowerShell 会话示例

可以在 PowerShell 中手动设置后再启动后端：

```powershell
$env:WECHAT_PAY_MOCK_ENABLED="false"
$env:WECHAT_PAY_APP_ID="wx_your_real_appid"
$env:WECHAT_PAY_APP_SECRET="your_real_app_secret"
$env:WECHAT_PAY_MCH_ID="your_real_mch_id"
$env:WECHAT_PAY_NOTIFY_URL="https://your-domain.example.com/api/payment/callback/wechat"
$env:WECHAT_PAY_API_V3_KEY="your_32_char_api_v3_key"
$env:WECHAT_PAY_PRIVATE_KEY_PATH="E:\secure\wechatpay\apiclient_key.pem"
$env:WECHAT_PAY_MERCHANT_SERIAL_NO="your_merchant_serial_no"
$env:WECHAT_PAY_PLATFORM_CERTIFICATE_PATH="E:\secure\wechatpay\wechatpay_platform.pem"

cd E:\me\quanshidaHT-JAVA
.\backend\start-dev.ps1
```

## 联调前自检

启动前至少确认：

- `WECHAT_PAY_MOCK_ENABLED=false`
- `notifyUrl` 是公网地址
- 私钥路径真实存在
- 平台证书路径真实存在
- 商户号和证书序列号匹配
- 当前激活商户在后台页显示为 `Ready`

## 相关文档

- [真实微信支付上线前检查表](/E:/me/quanshidaHT-JAVA/docs/wechat-pay-production-readiness-checklist.md:1)
- [小程序与支付联调清单](/E:/me/quanshidaHT-JAVA/docs/mini-program-payment-integration-checklist.md:1)
