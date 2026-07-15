# Real WeChat Pay Configuration Preparation

Updated: 2026-06-11

## Goal

Prepare real WeChat Pay configuration by environment variables, without changing application code.

## Configuration Inputs

The backend reads these values from [application.yml](/E:/me/quanshidaHT-JAVA/backend/src/main/resources/application.yml:1):

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

Reference template:

- [backend/.env.wechat-pay.example](/E:/me/quanshidaHT-JAVA/backend/.env.wechat-pay.example:1)

## Required Switch

Real payment is only enabled when:

```env
WECHAT_PAY_MOCK_ENABLED=false
```

If this value stays `true`, the system continues to use `MockWechatPayGateway`.

## Required Fields

The following values must be prepared for a real payment flow:

- `WECHAT_PAY_APP_ID`
- `WECHAT_PAY_APP_SECRET`
- `WECHAT_PAY_MCH_ID`
- `WECHAT_PAY_NOTIFY_URL`
- `WECHAT_PAY_API_V3_KEY`
- `WECHAT_PAY_PRIVATE_KEY_PATH`
- `WECHAT_PAY_MERCHANT_SERIAL_NO`
- `WECHAT_PAY_PLATFORM_CERTIFICATE_PATH`

## Field Notes

### `WECHAT_PAY_APP_ID`

- The miniapp `appId`
- Must match the app that initiates the payment

### `WECHAT_PAY_APP_SECRET`

- The miniapp `appSecret`
- Used for `code2Session`

### `WECHAT_PAY_MCH_ID`

- WeChat Pay merchant ID
- Must match the merchant certificate and private key

### `WECHAT_PAY_NOTIFY_URL`

- Payment callback URL
- Refund callback is derived from this base URL by the backend
- Use a public HTTPS endpoint in production

### `WECHAT_PAY_API_V3_KEY`

- WeChat Pay API v3 key
- Must be exactly 32 characters

### `WECHAT_PAY_PRIVATE_KEY_PATH`

- Merchant private key file path
- Must be readable by the backend process

### `WECHAT_PAY_MERCHANT_SERIAL_NO`

- Merchant certificate serial number

### `WECHAT_PAY_PLATFORM_CERTIFICATE_PATH`

- WeChat platform certificate file path
- Must be readable by the backend process

## Recommended Storage Layout

Do not store real certificates or private keys inside the repo.

Recommended approach:

- Keep private keys in a separate secure directory
- Keep platform certificates in a separate secure directory
- Pass only file paths through environment variables

Example:

```env
WECHAT_PAY_PRIVATE_KEY_PATH=E:/secure/wechatpay/apiclient_key.pem
WECHAT_PAY_PLATFORM_CERTIFICATE_PATH=E:/secure/wechatpay/wechatpay_platform.pem
```

## Callback Endpoints

Payment callback:

- `POST /api/payment/callback/wechat`

Refund callback:

- `POST /api/payment/callback/wechat-refund`

Requirements:

- Reachable from the public internet
- Not `localhost` in production
- Reverse proxy must preserve request headers

## PowerShell Example

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

## Pre-Flight Check

Before startup, confirm:

- `WECHAT_PAY_MOCK_ENABLED=false`
- `notifyUrl` is public and valid
- Private key path exists
- Platform certificate path exists
- Merchant ID and serial number match the certificate set
- The active merchant is shown as `ready` in the admin console

## Related Docs

- [WeChat Pay Production Readiness Checklist](/E:/me/quanshidaHT-JAVA/docs/wechat-pay-production-readiness-checklist.md:1)
- [Mini Program Payment Integration Checklist](/E:/me/quanshidaHT-JAVA/docs/mini-program-payment-integration-checklist.md:1)
