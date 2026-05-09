import { defineStore } from 'pinia'
import {
  bindMemberWechat,
  fetchMemberProfile,
  loginMember,
  loginMemberWithWechat,
  registerMember,
  updateMemberProfile,
} from '@/api/member'
import type {
  MemberLoginPayload,
  MemberProfile,
  MemberProfileUpdatePayload,
  MemberRegisterPayload,
  MemberWechatBindPayload,
  MemberWechatLoginPayload,
} from '@/types/member'
import { clearStoredToken, getStoredToken, setStoredToken } from '@/utils/http'

export const useMemberStore = defineStore('member', {
  state: () => ({
    token: '',
    profile: null as MemberProfile | null,
  }),
  getters: {
    isAuthenticated: (state) => Boolean(state.token),
    isWechatBound: (state) => Boolean(state.profile?.wechatOpenid),
  },
  actions: {
    restore() {
      this.token = getStoredToken()
    },
    setToken(token: string) {
      this.token = token
      setStoredToken(token)
    },
    clearSession() {
      this.token = ''
      this.profile = null
      clearStoredToken()
    },
    async fetchProfile() {
      const profile = await fetchMemberProfile()
      this.profile = profile
      return profile
    },
    async register(payload: MemberRegisterPayload) {
      const result = await registerMember(payload)
      this.setToken(result.accessToken)
      await this.fetchProfile()
    },
    async login(payload: MemberLoginPayload) {
      const result = await loginMember(payload)
      this.setToken(result.accessToken)
      await this.fetchProfile()
    },
    async wechatLogin(payload: MemberWechatLoginPayload) {
      const result = await loginMemberWithWechat(payload)
      this.setToken(result.accessToken)
      await this.fetchProfile()
    },
    async saveProfile(payload: MemberProfileUpdatePayload) {
      const profile = await updateMemberProfile(payload)
      this.profile = profile
      return profile
    },
    async bindWechat(payload: MemberWechatBindPayload) {
      const profile = await bindMemberWechat(payload)
      this.profile = profile
      return profile
    },
    logout(redirect = true) {
      this.clearSession()
      if (redirect) {
        uni.switchTab({
          url: '/pages/index/index',
        })
      }
    },
  },
})
