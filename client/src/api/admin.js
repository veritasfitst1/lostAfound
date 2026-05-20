import request from '../utils/request'
//各种后端接口  管理员
export const getStats = () => request.get('/api/admin/stats')
export const getUsers = (params) => request.get('/api/admin/users', { params })
export const toggleBan = (id) => request.put(`/api/admin/users/${id}/ban`)
export const getItems = (params) => request.get('/api/admin/items', { params })
export const updateItem = (id, data) => request.put(`/api/admin/items/${id}`, data)
export const updateItemStatus = (id, status) => request.post(`/api/admin/items/${id}/status?status=${status}`)
export const deleteItem = (id) => request.delete(`/api/admin/items/${id}`)
export const expireItems = (days = 30) => request.post(`/api/admin/items/expire?days=${days}`)
export const getReports = (params) => request.get('/api/admin/reports', { params })
export const approveReport = (id, note = '') => request.put(`/api/admin/reports/${id}/approve?note=${encodeURIComponent(note)}`)
export const rejectReport = (id, note = '') => request.put(`/api/admin/reports/${id}/reject?note=${encodeURIComponent(note)}`)
export const revokeReport = (id) => request.put(`/api/admin/reports/${id}/revoke`)

export const getAdminCategories = () => request.get('/api/admin/categories')
export const createCategory = (data) => request.post('/api/admin/categories', data)
export const updateCategory = (id, data) => request.put(`/api/admin/categories/${id}`, data)
export const deleteCategory = (id) => request.delete(`/api/admin/categories/${id}`)
