import request from './request'

export const login = data => request.post('/api/auth/login', data)

// 车场
export const myLots = () => request.get('/api/lot-admin/my')
export const lotDetail = id => request.get(`/api/lot-admin/${id}`)
export const lotQrcode = id => request.get(`/api/lot-admin/${id}/qrcode`)
export const createLot = data => request.post('/api/super/lot', data)
export const updateLot = (id, data) => request.put(`/api/super/lot/${id}`, data)
export const deleteLot = id => request.delete(`/api/super/lot/${id}`)

// 管理员
export const listAdmins = () => request.get('/api/super/admin/list')
export const createAdmin = data => request.post('/api/super/admin', data)
export const updateAdminStatus = (id, status) =>
  request.post('/api/super/admin/status', { id, status })
export const assignLots = data => request.post('/api/super/admin/assign', data)
export const adminLots = adminId => request.get(`/api/super/admin/${adminId}/lots`)

// 车辆（管理端）
export const lotVehicles = lotId => request.get(`/api/vehicle/lot/${lotId}/list`)
export const adminUnbind = (vehicleId, userId) =>
  request.post('/api/vehicle/lot/unbind', { vehicleId, userId })

// 临停（模拟道闸出场计费）
export const tempExit = data => request.post('/api/temp/exit', data)
