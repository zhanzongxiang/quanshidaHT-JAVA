import { normalizeMessage } from '@/utils/toast'

export async function getWechatLoginCode() {
  const result = await uni.login()
  if (!result.code) {
    throw new Error('Failed to get WeChat login code')
  }

  return result.code
}

export function normalizePaymentResultMessage(error: unknown) {
  const message = normalizeMessage(
    error instanceof Error ? error.message : error,
    'Failed to start payment',
  )
  const normalized = message.toLowerCase()

  if (normalized.includes('cancel')) {
    return {
      status: 'closed',
      message,
    }
  }

  return {
    status: 'exception',
    message,
  }
}
