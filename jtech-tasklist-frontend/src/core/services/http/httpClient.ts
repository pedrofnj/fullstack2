import axios from 'axios'
export const http = axios.create({
  baseURL: import.meta.env.VITE_API_BASE || 'http://localhost:8080',
})
// depois do JWT: http.interceptors.request.use(cfg => { cfg.headers.Authorization = `Bearer ${token}`; return cfg })
