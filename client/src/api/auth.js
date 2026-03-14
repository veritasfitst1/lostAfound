import request from '../utils/request'

export const adminLogin = (data) => request.post('/api/auth/admin-login', data)
