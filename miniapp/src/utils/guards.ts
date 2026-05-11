import { useMemberStore } from '@/stores/member'

function buildLoginRedirect() {
  const pages = getCurrentPages()
  const current = pages[pages.length - 1]
  const route = current?.route

  if (!route) {
    return '/pages/auth/login'
  }

  const options = ((current as { options?: Record<string, string | number | boolean | undefined> }).options || {})
  const queryString = Object.keys(options)
    .map((key) => `${encodeURIComponent(key)}=${encodeURIComponent(String(options[key] ?? ''))}`)
    .join('&')
  const target = queryString ? `/${route}?${queryString}` : `/${route}`

  return `/pages/auth/login?redirect=${encodeURIComponent(target)}`
}

export async function ensureMemberSession() {
  const memberStore = useMemberStore()
  memberStore.restore()

  if (!memberStore.isAuthenticated) {
    uni.navigateTo({
      url: buildLoginRedirect(),
    })
    return false
  }

  if (!memberStore.profile) {
    try {
      await memberStore.fetchProfile()
    } catch {
      memberStore.logout(false)
      uni.navigateTo({
        url: buildLoginRedirect(),
      })
      return false
    }
  }

  return true
}
