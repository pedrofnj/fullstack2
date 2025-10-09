import http from '@/core/services/http/httpClient'

export class HttpAuthService {
  async login({ email, password }: { email: string; password: string }) {
    try {
      const { data } = await http.post('/users/login', { email, password })
      return data
    } catch (err: any) {
      if (err.status === 404 || err.status === 400) {
        const { data } = await http.post('/users', {
          name: email.split('@')[0],
          email,
          password,
        })
        return data
      }
      throw err
    }
  }

  async getUserById(id: string) {
    const { data } = await http.get(`/users/${id}`)
    return data
  }

  async logout(refreshToken: string) {
    await http.post('/users/logout', { refreshToken })
  }

  async refreshToken(refreshToken: string) {
    const { data } = await http.post('/users/refresh-token', { refreshToken })
    return data
  }
}
