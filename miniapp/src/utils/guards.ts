import { useMemberStore } from '@/stores/member'
import { goToLogin, resolveRedirectUrl } from '@/utils/navigation'

export function buildLoginRedirect() {
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

  return resolveRedirectUrl(target)
}

export async function ensureMemberSession() {
  const memberStore = useMemberStore()
  memberStore.restore()

  if (!memberStore.isAuthenticated) {
    goToLogin(buildLoginRedirect())
    return false
  }

  if (!memberStore.profile) {
    try {
      await memberStore.fetchProfile()
    } catch {
      memberStore.logout(false)
      goToLogin(buildLoginRedirect())
      return false
    }
  }

  return true
}
