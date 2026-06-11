import { defineStore } from 'pinia'
import {
  bindMemberWechat,
  changeMemberPassword,
  completeMemberWechatLogin,
  fetchMemberProfile,
  loginMember,
  loginMemberWithWechat,
  registerMember,
  unbindMemberWechat,
  updateMemberProfile,
} from '@/api/member'
import type {
  MemberLoginPayload,
  MemberPasswordChangePayload,
  MemberProfile,
  MemberProfileUpdatePayload,
  MemberRegisterPayload,
  MemberWechatBindPayload,
  MemberWechatCompletePayload,
  MemberWechatLoginPayload,
} from '@/types/member'
import { clearSessionStorage, getStoredToken, setStoredToken } from '@/utils/http'

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
      clearSessionStorage()
    },
    async fetchProfile() {
      const profile = await fetchMemberProfile()
      this.profile = profile
      return profile
    },
    async applyAuthenticatedSession(accessToken: string) {
      this.setToken(accessToken)
      await this.fetchProfile()
    },
    async register(payload: MemberRegisterPayload) {
      const result = await registerMember(payload)
      await this.applyAuthenticatedSession(result.accessToken)
    },
    async login(payload: MemberLoginPayload) {
      const result = await loginMember(payload)
      await this.applyAuthenticatedSession(result.accessToken)
    },
    async wechatLogin(payload: MemberWechatLoginPayload) {
      const result = await loginMemberWithWechat(payload)
      if (result.accessToken) {
        await this.applyAuthenticatedSession(result.accessToken)
      }
      return result
    },
    async completeWechatLogin(payload: MemberWechatCompletePayload) {
      const result = await completeMemberWechatLogin(payload)
      if (result.accessToken) {
        await this.applyAuthenticatedSession(result.accessToken)
      }
      return result
    },
    async saveProfile(payload: MemberProfileUpdatePayload) {
      const profile = await updateMemberProfile(payload)
      this.profile = profile
      return profile
    },
    async updatePassword(payload: MemberPasswordChangePayload) {
      await changeMemberPassword(payload)
    },
    async bindWechat(payload: MemberWechatBindPayload) {
      const profile = await bindMemberWechat(payload)
      this.profile = profile
      return profile
    },
    async unbindWechat() {
      const profile = await unbindMemberWechat()
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
