import { defineStore } from 'pinia'
import { Services } from '@/core/services'
import type { AuthUser } from '@/core/models/auth'

export const useAuthStore = defineStore('auth', {
  state: (): { user: AuthUser | null } => ({ user: null }),
  getters: { isAuthenticated: (s) => !!s.user },
  actions: {
    async login(email: string, password: string) {
      const { user } = await Services.auth.login({ email, password })
      this.user = user
    },
    async logout() {
      await Services.auth.logout()
      this.user = null
    },
  },
  persist: true,
})
