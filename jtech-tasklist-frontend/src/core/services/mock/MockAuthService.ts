import type { IAuthService } from '../IAuthService'
import type { AuthUser, LoginPayload } from '@/core/models/auth'
import { sget, sset, suuid } from '@/core/utils/storage'
const KEY = 'mock_users'
export class MockAuthService implements IAuthService {
  async login({ email, password }: LoginPayload) {
    if (!email || !password) throw new Error('Credenciais inválidas')
    let users = sget<AuthUser[]>(KEY, [])
    let user = users.find((u) => u.email === email)
    if (!user) {
      user = { id: suuid(), name: email.split('@')[0], email }
      users = [...users, user]
      sset(KEY, users)
    }
    return { user }
  }
  async logout(refreshToken?: string) {
    return
  }
  async refreshToken(refreshToken: string) {
    return { token: 'mock-token-' + refreshToken }
  }
  async me() {
    return null
  }
}
