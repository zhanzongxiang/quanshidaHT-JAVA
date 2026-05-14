import { normalizeMessage } from '@/utils/toast'

export async function getWechatLoginCode() {
  const result = await uni.login()
  if (!result.code) {
    throw new Error('获取微信登录凭证失败')
  }

  return result.code
}

export function normalizePaymentResultMessage(error: unknown) {
  const message = normalizeMessage(error instanceof Error ? error.message : error, '发起支付失败')
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
