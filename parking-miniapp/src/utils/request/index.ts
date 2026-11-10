import { BASE_URL, REQUEST_TIMEOUT, getToken, clearToken } from './config'

let loadingCount = 0
function showLoadingTip() {
  loadingCount++
  if (loadingCount === 1) {
    uni.showLoading({ title: '加载中...', mask: true })
  }
}

function hideLoadingTip() {
  loadingCount--
  if (loadingCount <= 0) {
    loadingCount = 0
    uni.hideLoading()
  }
}

function showErrorTip(msg: string) {
  uni.showToast({ title: msg, icon: 'none', duration: 2000 })
}

export interface RequestOptions {
  url: string
  method?: 'GET' | 'POST' | 'PUT' | 'DELETE'
  data?: any
  header?: Record<string, string>
  showLoading?: boolean
  showError?: boolean
}

export interface RequestResult<T = any> {
  code: number
  data: T
  message: string
  success: boolean
}

export function request<T = any>(options: RequestOptions): Promise<RequestResult<T>> {
  const {
    url,
    method = 'GET',
    data = {},
    header = {},
    showLoading = true,
    showError = true
  } = options

  const token = getToken()
  if (token) {
    header['Authorization'] = `Bearer ${token}`
  }
  if (!header['Content-Type']) {
    header['Content-Type'] = 'application/json'
  }

  return new Promise((resolve, reject) => {
    if (showLoading) showLoadingTip()

    uni.request({
      url: BASE_URL + url,
      method,
      data,
      header,
      timeout: REQUEST_TIMEOUT,
      success: (res) => {
        if (showLoading) hideLoadingTip()

        const statusCode = res.statusCode
        const result = res.data as RequestResult<T>

        if (statusCode !== 200) {
          const msg = `请求失败(${statusCode})`
          if (showError) showErrorTip(msg)
          reject({ code: statusCode, data: null, message: msg, success: false })
          return
        }

        // 业务码非 0：未登录(401) 清 token 回登录页
        if (result.code !== 0) {
          if (result.code === 401) {
            clearToken()
            uni.reLaunch({ url: '/pages/login/login' })
            return
          }
          if (showError) showErrorTip(result.message || '请求失败')
          reject(result)
          return
        }

        resolve(result)
      },
      fail: (err) => {
        if (showLoading) hideLoadingTip()
        console.error('=== 请求失败 ===', err)
        const msg = '网络请求失败，请检查网络连接'
        if (showError) showErrorTip(msg)
        reject({ code: -1, data: null, message: msg, success: false })
      }
    })
  })
}

export const get = <T = any>(url: string, data?: any, options?: Partial<RequestOptions>) =>
  request<T>({ url, method: 'GET', data, ...options })

export const post = <T = any>(url: string, data?: any, options?: Partial<RequestOptions>) =>
  request<T>({ url, method: 'POST', data, ...options })

export const put = <T = any>(url: string, data?: any, options?: Partial<RequestOptions>) =>
  request<T>({ url, method: 'PUT', data, ...options })

export const del = <T = any>(url: string, data?: any, options?: Partial<RequestOptions>) =>
  request<T>({ url, method: 'DELETE', data, ...options })
