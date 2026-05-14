const PHONE_PATTERN = /^1\d{10}$/
const DATE_TIME_PATTERN = /^\d{4}-\d{2}-\d{2} \d{2}:\d{2}:\d{2}$/

export function normalizePhone(value: string) {
  return value.replace(/\D/g, '').slice(0, 11)
}

export function isValidPhone(value: string) {
  return PHONE_PATTERN.test(normalizePhone(value))
}

export function isPositiveAmount(value: number | null | undefined) {
  return typeof value === 'number' && Number.isFinite(value) && value > 0
}

export function hasText(value: string | null | undefined) {
  return typeof value === 'string' && value.trim().length > 0
}

export function isDateTimeText(value: string | null | undefined) {
  return hasText(value) && DATE_TIME_PATTERN.test(value!.trim())
}
