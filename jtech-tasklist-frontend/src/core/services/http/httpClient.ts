import axios from 'axios'
import { useAuthStore } from '@/stores/useAuthStore'

const http = axios.create({
  baseURL: import.meta.env.VITE_API_BASE,
  headers: { 'Content-Type': 'application/json' },
})

http.interceptors.request.use(config => {
  if (!config.url?.includes('/users/login') && !(config.url === '/users' && config.method === 'post')) {
    let token: string | null = null
    try {
      const store = useAuthStore()
      token = store.token
    } catch {
      token = JSON.parse(localStorage.getItem('auth') || '{}').token || null
    }
    if (token) {
      config.headers = config.headers || {}
      config.headers['Authorization'] = `Bearer ${token}`
    }
  }
  return config
})

http.interceptors.response.use(
  response => response,
  error => {
    const message = error.response?.data?.message || 'Erro ao conectar com servidor'
    return Promise.reject({ message, status: error.response?.status })
  }
)

export default http
