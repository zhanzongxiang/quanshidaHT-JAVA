const PHONE_PATTERN = /^1\d{10}$/
const MONEY_PATTERN = /^(0|[1-9]\d*)(\.\d{1,2})?$/

export function normalizePhone(value: string) {
  return value.replace(/\D/g, '').slice(0, 11)
}

export function isValidPhone(value: string) {
  return PHONE_PATTERN.test(normalizePhone(value))
}

export function isValidPassword(value: string) {
  return value.trim().length >= 6
}

export function parsePaymentAmount(value: string) {
  const normalized = value.trim()

  if (!normalized || !MONEY_PATTERN.test(normalized)) {
    return null
  }

  const amount = Number(normalized)
  if (!Number.isFinite(amount) || amount <= 0) {
    return null
  }

  return Math.round(amount * 100) / 100
}
