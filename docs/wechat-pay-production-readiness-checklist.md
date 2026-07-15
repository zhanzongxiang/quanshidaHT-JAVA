# WeChat Pay Production Readiness Checklist

Updated: 2026-06-11

## Goal

Use this checklist before switching any tenant from mock payment to real WeChat Pay.

## 1. Configuration

- `WECHAT_PAY_MOCK_ENABLED=false`
- Real `appId` is configured
- Real `appSecret` is configured
- Real `mchId` is configured
- Real `notifyUrl` is configured
- Real `apiV3Key` is configured
- Real `privateKeyPath` is configured
- Real `merchantSerialNo` is configured
- Real `platformCertificatePath` is configured
- Secrets are injected through environment variables or secure config, not hard-coded in repo files

## 2. Certificates And Keys

- Merchant private key file exists and the backend process can read it
- Platform certificate file exists and the backend process can read it
- Platform certificate refresh can run successfully
- Certificate storage directory permissions are restricted
- Merchant serial number matches the active merchant certificate

## 3. Callback Reachability

- `notifyUrl` is publicly reachable by WeChat
- Payment callback endpoint is reachable
- Refund callback endpoint is reachable
- Reverse proxy / gateway keeps required headers
- Firewall and upstream rules do not block WeChat callback traffic

## 4. Business Flow

- Miniapp payment prepare succeeds
- Payment success callback can mark the order as paid
- Duplicate payment callbacks remain idempotent
- Refund request succeeds
- Refund callback can update refund state
- Reconcile download and diff inspection work
- Merchant switching does not break historical orders or refunds

## 5. Security

- Logs do not expose `appSecret`
- Logs do not expose `apiV3Key`
- Logs do not expose private-key content
- Merchant secrets are encrypted at rest
- Admin and member tokens remain isolated
- Error responses do not leak merchant secrets

## 6. Verification

- Backend build passes: `mvn -DskipTests package`
- Payment-related backend tests pass
- Frontend build passes: `npm run build`
- Miniapp build passes: `npm run build:mp-weixin`
- At least one real payment is verified end-to-end
- At least one real refund is verified end-to-end
- At least one real reconcile cycle is verified

## 7. Operations

- Callback failure alerting rule is defined
- Certificate refresh failure alerting rule is defined
- Reconcile diff handling owner is assigned
- Production rollback path is documented
- On-call contact knows the payment cutover window

## 8. Launch Day

- Active merchant is double-checked
- Production callback URL is double-checked
- Public certificate/domain state is healthy
- Test orders are cleaned up or clearly isolated
- Support/ops team is informed of the release window

## 9. Post-Launch Observation

- Watch the first successful payment
- Watch the first successful callback ingestion
- Watch the first refund
- Watch callback failure metrics
- Watch reconcile records for continued generation
