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
    // #region agent log
    fetch('http://127.0.0.1:7452/ingest/49c51c8e-b700-498f-abd5-e19a65322b20',{method:'POST',headers:{'Content-Type':'application/json','X-Debug-Session-Id':'5795f3'},body:JSON.stringify({sessionId:'5795f3',hypothesisId:'A,C',location:'request.js:errorInterceptor',message:'axios error',data:{status:err.response?.status,data:JSON.stringify(err.response?.data),url:err.config?.url},timestamp:Date.now()})}).catch(()=>{});
    // #endregion
    if (err.response?.status === 401 && !window.location.pathname.includes('/login')) {
      localStorage.removeItem('token')
      localStorage.removeItem('userInfo')
      window.location.href = '/login'
    }
    return Promise.reject(err.response?.data?.message || err.message || '请求失败')
  }
)

export default request
