# API Error Code Spec

## Response Shape

All backend APIs return the same JSON envelope:

```json
{
  "code": 0,
  "message": "OK",
  "data": {}
}
```

- `code = 0`: success
- `code != 0`: business failure
- `message`: safe user-facing summary
- `data`: payload for success, `null` for failures unless a specific API defines otherwise

## Stable Error Codes

| Code | Name | Meaning | Client Handling |
| --- | --- | --- | --- |
| `40000` | `BUSINESS_RULE_FAILED` | Generic business rule failure | Show message |
| `40001` | `VALIDATION_FAILED` | Request field validation failed | Show message and keep form state |
| `40002` | `INVALID_ARGUMENT` | Illegal argument or malformed input | Show message |
| `40010` | `TENANT_CONTEXT_REQUIRED` | Tenant context could not be resolved | Show message and verify domain/tenant routing |
| `40100` | `AUTHENTICATION_REQUIRED` | Request needs a valid authenticated session | Clear session and redirect to sign-in |
| `40101` | `AUTHENTICATION_FAILED` | Login or identity verification failed | Show message |
| `40102` | `SESSION_INVALID` | Session/token is stale or user state no longer matches token | Clear session and redirect to sign-in |
| `40300` | `AUTHORIZATION_DENIED` | Authenticated user lacks required permission | Show message or redirect to forbidden page |
| `40400` | `RESOURCE_NOT_FOUND` | Target resource does not exist | Show message or show empty/not-found state |
| `40900` | `RESOURCE_CONFLICT` | Duplicate/occupied resource such as phone or binding | Show message and keep form state |
| `40901` | `STATE_INVALID` | Current entity state does not allow the operation | Show message and refresh current state if needed |
| `42900` | `RATE_LIMITED` | Too many attempts or requests | Show message and delay retry |
| `50000` | `INTERNAL_ERROR` | Unexpected server-side failure | Show fallback message and allow retry |

## Auth/Error Normalization

- Spring Security unauthenticated responses now return JSON with code `40100`.
- Spring Security access-denied responses now return JSON with code `40300`.
- Admin frontend listens for `401xx` responses, clears session state, and redirects to `/admin/login`.
- Miniapp request wrapper listens for `401xx` responses, clears the local token, and returns the user to `/pages/auth/login`.

## Backend Usage Rules

- Throw `BusinessException(code, message)` when the client should receive a stable business error.
- Use `NotFoundException` for missing resources that should map to `40400`.
- Let unexpected exceptions bubble to the global handler so they map to `50000`.

## Current High-Impact Mappings

- Admin login wrong password: `40101`
- Admin session no longer valid: `40102`
- Member login wrong password: `40101`
- Member session invalid: `40102`
- Permission denied for tenant switching: `40300`
- Duplicate phone / WeChat binding conflict: `40900`
- Login throttling: `42900`
