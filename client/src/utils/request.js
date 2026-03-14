import axios from 'axios'

const request = axios.create({
  baseURL: import.meta.env.VITE_API_BASE || (import.meta.env.DEV ? '' : 'http://localhost:8080'),
  timeout: 10000,
  headers: { 'Content-Type': 'application/json' }
})

request.interceptors.request.use(config => {
  const token = localStorage.getItem('token')
  if (token) config.headers.Authorization = 'Bearer ' + token
  return config
})

request.interceptors.response.use(
  res => res.data,
  err => {
    if (err.response?.status === 401) {
      localStorage.removeItem('token')
      localStorage.removeItem('userInfo')
      window.location.href = '/login'
    }
    return Promise.reject(err.response?.data?.message || err.message || '请求失败')
  }
)

export default request
