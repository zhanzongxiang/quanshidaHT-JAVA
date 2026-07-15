# Mini Program Payment Integration Checklist

Updated: 2026-06-11

## Goal

Verify that member sign-in, WeChat binding, miniapp payment, callback handling, refund, and reconcile can run end-to-end in a test or staging environment.

## 1. Environment Ready

- Backend service starts normally
- Admin console can open the payment management pages
- Current merchant config is complete and marked `ready`
- Callback URLs are reachable from WeChat
- Test member account is available
- Payable test waybill data is available
- Current run mode is known: `mock` or `real WeChat`

## 2. Member And WeChat Sign-In

Pre-check:

- `POST /api/member/auth/wechat-login` is available
- `POST /api/member/auth/wechat-complete` is available
- `PUT /api/member/profile/wechat` is available
- Member profile can store `openid`, `unionid`, and bind timestamps

Steps:

1. Get a WeChat login `code` from the miniapp.
2. Call `POST /api/member/auth/wechat-login`.
3. If phone completion is required, call `POST /api/member/auth/wechat-complete`.
4. Use the returned member token to call `GET /api/member/profile`.
5. Confirm the WeChat binding fields are returned correctly.
6. If testing manual bind or rebind, call `PUT /api/member/profile/wechat`.
7. Query the profile again and confirm the final state is persisted.

Checks:

- First WeChat login can create or link a member correctly
- Existing member can bind correctly
- Existing binding can log in again
- Disabled or pending member is rejected correctly
- Error responses follow the unified `ApiResponse` format

## 3. Miniapp Payment

Pre-check:

- Target member has a valid `openid`
- Target waybill is visible to that member
- Active merchant configuration is complete
- `PUT /api/member/payments/prepare` is available
- `GET /api/member/payments` is available

Steps:

1. Call `GET /api/member/waybills`.
2. Pick a payable waybill.
3. Call `PUT /api/member/payments/prepare`.
4. Record the returned values:
   - `payOrderId`
   - `orderNo`
   - `appId`
   - `timeStamp`
   - `nonceStr`
   - `packageValue`
   - `signType`
   - `paySign`
5. Call `wx.requestPayment` in the miniapp.
6. After payment, call `GET /api/member/payments`.
7. Confirm the admin payment page shows the expected order status.

Checks:

- Order is created with the correct tenant/member/merchant snapshot
- Payment parameters match the active merchant app
- Payment success can transition the order to `paid`
- Repeated callbacks remain idempotent

## 4. Refund

Steps:

1. Pick a paid order in admin.
2. Create a refund.
3. Wait for the refund callback or simulate it in mock mode.
4. Confirm refund order status and payment order status are updated.

Checks:

- Refund amount validation works
- Retry is only allowed for failed refunds
- Refund callback keeps the order state consistent

## 5. Reconcile

Steps:

1. Trigger reconcile generation for the current merchant/channel.
2. Open reconcile records in admin.
3. Inspect diff details.

Checks:

- Reconcile record can be generated
- Diff summary is readable
- No tenant crossover occurs

## 6. Failure Paths

- Invalid or expired session returns structured auth errors
- Merchant mismatch callback is rejected
- Missing WeChat binding blocks miniapp payment
- Rate-limited login returns a stable error code
- Invalid merchant config returns a readable validation message

## 7. Final Sign-Off

- Backend payment tests pass
- Backend package passes
- Admin frontend build passes
- Miniapp build passes
- At least one full payment and one refund are verified in the target environment
