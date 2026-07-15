const TAB_BAR_PATHS = [
  '/pages/index/index',
  '/pages/waybill/list',
  '/pages/payment/list',
  '/pages/profile/index',
]

export function resolveRedirectUrl(value?: string) {
  if (!value || typeof value !== 'string') {
    return '/pages/index/index'
  }

  const trimmed = decodeURIComponent(value).trim()
  if (!trimmed.startsWith('/pages/')) {
    return '/pages/index/index'
  }

  return trimmed
}

export function goToLogin(redirect?: string) {
  const target = redirect ? `?redirect=${encodeURIComponent(resolveRedirectUrl(redirect))}` : ''
  uni.navigateTo({
    url: `/pages/auth/login${target}`,
  })
}

export function navigateAfterAuth(target?: string) {
  const redirectUrl = resolveRedirectUrl(target)

  if (TAB_BAR_PATHS.includes(redirectUrl)) {
    uni.switchTab({
      url: redirectUrl,
    })
    return
  }

  uni.redirectTo({
    url: redirectUrl,
  })
}

export function openAppPage(url: string) {
  const resolved = resolveRedirectUrl(url)
  if (TAB_BAR_PATHS.includes(resolved)) {
    uni.switchTab({
      url: resolved,
    })
    return
  }

  uni.navigateTo({
    url: resolved,
  })
}
