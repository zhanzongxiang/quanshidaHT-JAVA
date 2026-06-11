import { defineStore } from 'pinia'
import { fetchMe, login, logout, switchTenant } from '../api/auth'
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
    clearSession() {
      this.me = null
      this.initialized = true
    },
    async loginByPassword(username: string, password: string) {
      this.loading = true
      try {
        await login({ username, password })
        this.me = await fetchMe()
        this.initialized = true
      } catch (error) {
        this.clearSession()
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
        this.clearSession()
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
      this.clearSession()
    },
    async switchTenant(tenantId: number) {
      this.loading = true
      try {
        await switchTenant({ tenantId })
        this.me = await fetchMe()
        this.initialized = true
      } catch (error) {
        throw error
      } finally {
        this.loading = false
      }
    },
    hasPermission(permission: string) {
      return this.me?.permissions.includes(permission) ?? false
    },
  },
})
