const DEFAULT_ERROR_MESSAGE = '操作失败，请稍后重试'

export function normalizeMessage(message: unknown, fallback = DEFAULT_ERROR_MESSAGE) {
  if (typeof message === 'string') {
    const normalized = message.replace(/\s+/g, ' ').trim()
    return normalized || fallback
  }

  if (message instanceof Error) {
    return normalizeMessage(message.message, fallback)
  }

  return fallback
}

export function showError(message: unknown, fallback?: string) {
  uni.showToast({
    title: normalizeMessage(message, fallback),
    icon: 'none',
    duration: 2200,
  })
}

export function showSuccess(title: string) {
  uni.showToast({
    title,
    icon: 'success',
  })
}
