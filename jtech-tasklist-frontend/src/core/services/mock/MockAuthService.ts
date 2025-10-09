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
    return { user } // sem tokens no mock
  }
  async logout() {
    return
  }
  async me() {
    return null
  }
}
