import { useMemberStore } from '@/stores/member'

export async function ensureMemberSession() {
  const memberStore = useMemberStore()
  memberStore.restore()

  if (!memberStore.isAuthenticated) {
    uni.navigateTo({
      url: '/pages/auth/login',
    })
    return false
  }

  if (!memberStore.profile) {
    try {
      await memberStore.fetchProfile()
    } catch {
      memberStore.logout(false)
      uni.navigateTo({
        url: '/pages/auth/login',
      })
      return false
    }
  }

  return true
}
