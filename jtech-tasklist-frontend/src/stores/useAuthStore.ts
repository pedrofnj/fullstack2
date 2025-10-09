import { defineStore } from 'pinia'
import { Services } from '@/core/services'
import type { AuthUser } from '@/core/models/auth'

export const useAuthStore = defineStore('auth', {
  state: (): { user: AuthUser | null, token: string | null } => ({ user: null, token: null }),
  getters: {
    isAuthenticated: (s) => !!s.user && !!s.token,
  },
  actions: {
    async login(email: string, password: string) {
      const { user, token } = await Services.auth.login({ email, password })
      this.user = user
      this.token = token
    },
    async logout() {
      await Services.auth.logout()
      this.user = null
      this.token = null
    },
  },
  persist: true,
})
