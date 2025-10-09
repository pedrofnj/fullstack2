import axios from 'axios'

const http = axios.create({
  baseURL: import.meta.env.VITE_API_BASE,
  headers: { 'Content-Type': 'application/json' },
})

http.interceptors.response.use(
  response => response,
  error => {
    const message = error.response?.data?.message || 'Erro ao conectar com servidor'
    return Promise.reject({ message, status: error.response?.status })
  }
)

export default http
