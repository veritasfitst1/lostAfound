import request from '../utils/request'

export const getStats = () => request.get('/api/admin/stats')
export const getUsers = (params) => request.get('/api/admin/users', { params })
export const toggleBan = (id) => request.put(`/api/admin/users/${id}/ban`)
export const getItems = (params) => request.get('/api/admin/items', { params })
export const deleteItem = (id) => request.delete(`/api/admin/items/${id}`)
export const expireItems = (days = 30) => request.post(`/api/admin/items/expire?days=${days}`)
export const getReports = () => request.get('/api/admin/reports')
export const approveReport = (id, note = '') => request.put(`/api/admin/reports/${id}/approve?note=${encodeURIComponent(note)}`)
export const rejectReport = (id, note = '') => request.put(`/api/admin/reports/${id}/reject?note=${encodeURIComponent(note)}`)
