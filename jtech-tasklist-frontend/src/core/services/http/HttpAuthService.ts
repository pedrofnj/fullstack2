import { http } from './httpClient'
import type { IAuthService } from '../IAuthService'
import type { LoginPayload, AuthUser } from '@/core/models/auth'

export class HttpAuthService implements IAuthService {
  async login(payload: LoginPayload) {
    const { data } = await http.post('/auth/login', payload)
    return { user: data.user, tokens: data.tokens }
  }
  async logout() {
    await http.post('/auth/logout')
  }
  async me() {
    const { data } = await http.get<AuthUser>('/auth/me')
    return data
  }
}
