// 轻量登录态管理（localStorage 持久化）
export function getAuth() {
  return {
    token: localStorage.getItem('token') || '',
    role: localStorage.getItem('role') || '',
    name: localStorage.getItem('name') || ''
  }
}

export function setAuth({ token, role, name }) {
  localStorage.setItem('token', token)
  localStorage.setItem('role', role)
  localStorage.setItem('name', name || '')
}

export function clearAuth() {
  localStorage.removeItem('token')
  localStorage.removeItem('role')
  localStorage.removeItem('name')
}

export const ROLE_TEXT = {
  SUPER_ADMIN: '超级管理员',
  ADMIN: '车场管理员'
}
