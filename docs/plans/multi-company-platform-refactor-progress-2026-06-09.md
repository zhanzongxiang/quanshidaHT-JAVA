# Multi-Company Platform Refactor Progress

Date: 2026-06-09

## Completed In This Round

- Added member tenant migration: `V21__tenantize_member_module.sql`
- Added `tenant_id` to `member_user`
- Added `tenant_id` to `member_waybill_relation`
- Changed member uniqueness to tenant scope:
  - `member_user(tenant_id, phone)`
  - `member_user(tenant_id, wechat_openid)`
  - `member_waybill_relation(tenant_id, member_id, waybill_id)`
- Updated member entities to carry `tenantId`
- Updated member mappers to require `tenantId`
- Updated `MemberService` to read/write all member data in tenant context
- Updated member JWT creation to include `tenantId` and `tenantCode`
- Updated `PaymentService` member lookups to use tenant-scoped queries
- Updated tests for member and payment services
- Added payment tenant migration: `V22__tenantize_payment_module.sql`
- Added content/news tenant migration: `V23__tenantize_content_and_news_module.sql`
- Added tenant admin menu migration: `V24__add_tenant_admin_menu.sql`
- Added `tenant_id` to payment tables:
  - `pay_order`
  - `pay_transaction`
  - `refund_order`
  - `pay_notify_log`
  - `refund_notify_log`
  - `pay_reconcile_record`
  - `pay_merchant_config`
- Updated payment entities to carry `tenantId`
- Updated payment mappers to require `tenantId`
- Added global merchant lookup paths for callback parsing and scheduler-style flows
- Updated `PaymentService` to read and write payment data in tenant context
- Updated payment callback flow to derive tenant context from merchant config before handling notify payloads
- Updated payment tests for tenant-aware queries and callback context handling
- Added `tenant_id` to content/news tables:
  - `site_content_page`
  - `news_article`
- Updated content/news entities to carry `tenantId`
- Updated content/news mappers to require `tenantId`
- Updated content/news admin services to read and write in tenant context
- Updated public website page/news reads and cache keys to respect tenant context
- Updated dashboard content/news metrics to use tenant-scoped queries
- Added tenant admin page in frontend
- Added tenant frontend API/types and dynamic route mapping
- Added tenant menu entry for super admin
- Added tenant domain management in backend/frontend
- Added tenant disable safety rules:
  - default tenant cannot be disabled
  - tenant with enabled admin users cannot be disabled
  - tenant with enabled domains cannot be disabled
- Added tenant bootstrap/init flow on tenant creation:
  - bootstrap admin account
  - default home content
  - default service line pages
  - default payment merchant config

## Verification

- Backend build passed:
  - `mvn -DskipTests package`
- Targeted payment tests passed:
  - `mvn test -Dtest=PaymentServiceTest,PaymentCallbackControllerTest,PaymentNotifyReplayServiceTest`
- Backend build passed after content/news tenantization:
  - `mvn -DskipTests package`
- Backend build passed after tenant admin menu update:
  - `mvn -DskipTests package`
- Frontend build passed after tenant admin page update:
  - `npm run build`
- Backend build passed after tenant governance updates:
  - `mvn -DskipTests package`
- Frontend build passed after tenant domain management updates:
  - `npm run build`
- Backend build passed after tenant bootstrap updates:
  - `mvn -DskipTests package`
- Frontend build passed after bootstrap info display updates:
  - `npm run build`

## Current Status

- Tenant foundation: completed
- Waybill module tenantization: completed
- Member module tenantization: completed
- Payment module full tenantization: completed
- Content/news tenantization: completed
- Frontend tenant management UI: completed

## Next Recommended Step

- Review public-site seed/default content strategy for new tenants
- Add deeper tenant governance flows:
  - cross-tenant super-admin switching
  - tenant-level operational dashboards
- Expand payment test coverage beyond targeted service/controller paths
