import { defineStore } from 'pinia'
import { fetchMe, login, logout } from '../api/auth'
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
    isAuthenticated: (state) => state.me !== null,
  },
  actions: {
    async loginByPassword(username: string, password: string) {
      this.loading = true
      try {
        await login({ username, password })
        this.me = await fetchMe()
        this.initialized = true
      } catch (error) {
        this.me = null
        this.initialized = true
        throw error
      } finally {
        this.loading = false
      }
    },
    async initialize() {
      this.loading = true
      try {
        this.me = await fetchMe()
      } catch (error) {
        this.me = null
      } finally {
        this.initialized = true
        this.loading = false
      }
    },
    async logout() {
      try {
        await logout()
      } catch {
        // ignore logout API errors
      }
      this.me = null
      this.initialized = true
    },
    hasPermission(permission: string) {
      return this.me?.permissions.includes(permission) ?? false
    },
  },
})
