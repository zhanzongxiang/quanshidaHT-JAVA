# Payment Ops Alert Runbook

Updated: 2026-06-11

## Goal

This runbook defines the default alert rules and response expectations for tenant-scoped payment operations.

## Alert Source

Primary source:

- `GET /api/admin/payments/ops/overview`

The API now returns structured `alerts` with:

- `severity`
- `rule`
- `title`
- `message`
- `suggestedAction`

## Current Rules

| Rule | Severity | Trigger | Expected Action |
| --- | --- | --- | --- |
| `merchant_config_incomplete` | `critical` | Active merchant config is incomplete or invalid | Fix merchant config before real-payment cutover |
| `certificate_auto_refresh_disabled` | `warning` | Real-payment mode is on but certificate auto-refresh is off | Enable auto-refresh or assign manual refresh owner |
| `payment_callback_failures_present` | `warning` / `critical` | Payment callback failures exist; `>=5` escalates to critical | Inspect callback logs and replay failed notifications |
| `refund_callback_failures_present` | `warning` / `critical` | Refund callback failures exist; `>=5` escalates to critical | Inspect refund callback logs and replay failed notifications |
| `reconcile_missing` | `warning` | No reconcile record exists yet | Run reconcile and confirm record generation |
| `reconcile_attention_required` | `warning` / `critical` | Latest reconcile has diffs or failed/error status | Resolve diff items before cutover |
| `ops_healthy` | `info` | No immediate issue detected | Continue routine monitoring |

## Suggested Review Cadence

- During payment cutover: every 15 minutes
- During normal operation: at least once per business day
- After merchant config changes: immediately
- After certificate refresh issues: immediately

## Escalation Guidance

- `critical`
  - Do not cut over or continue unattended production payment traffic until resolved
  - Assign an owner immediately
- `warning`
  - Investigate within the same business day
  - Confirm whether the issue is transient or persistent
- `info`
  - No immediate action required

## Manual Checks Still Required

- Real-device miniapp payment verification
- Real WeChat callback reachability from public network
- Real refund callback verification
- On-call and rollback communication before production cutover
