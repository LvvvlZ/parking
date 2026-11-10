// API 基础配置：开发时后端跑在本机 8080，真机调试请改为局域网 IP
export const BASE_URL = 'http://localhost:8080'

// 请求超时时间(ms)
export const REQUEST_TIMEOUT = 15000

// Token Key
export const TOKEN_KEY = 'parking_token'

export function getToken(): string {
  try {
    const token = uni.getStorageSync(TOKEN_KEY)
    return token || ''
  } catch (e) {
    return ''
  }
}

export function setToken(token: string): void {
  try {
    uni.setStorageSync(TOKEN_KEY, token)
  } catch (e) {
    console.error('设置Token失败:', e)
  }
}

export function clearToken(): void {
  try {
    uni.removeStorageSync(TOKEN_KEY)
  } catch (e) {
    console.error('清除Token失败:', e)
  }
}
