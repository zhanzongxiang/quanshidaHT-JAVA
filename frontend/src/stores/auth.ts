import { defineStore } from 'pinia'
import { fetchMe, login } from '../api/auth'
import type { MeInfo } from '../types/auth'

interface AuthState {
  initialized: boolean
  loading: boolean
  me: MeInfo | null
}

export const useAuthStore = defineStore('auth', {
  state: (): AuthState => ({
    initialized: false,
    loading: false,
    me: null,
  }),
  getters: {
    token: () => localStorage.getItem('access_token'),
    isAuthenticated: (state) => Boolean(localStorage.getItem('access_token') && state.me),
  },
  actions: {
    async loginByPassword(username: string, password: string) {
      const result = await login({ username, password })
      localStorage.setItem('access_token', result.accessToken)
      await this.initialize()
    },
    async initialize() {
      if (!localStorage.getItem('access_token')) {
        this.me = null
        this.initialized = true
        return
      }
      this.loading = true
      try {
        this.me = await fetchMe()
      } catch (error) {
        localStorage.removeItem('access_token')
        this.me = null
      } finally {
        this.initialized = true
        this.loading = false
      }
    },
    logout() {
      localStorage.removeItem('access_token')
      this.me = null
      this.initialized = true
    },
    hasPermission(permission: string) {
      return this.me?.permissions.includes(permission) ?? false
    },
  },
})
