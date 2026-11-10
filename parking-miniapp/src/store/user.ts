import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { setToken as saveToken, clearToken, getToken } from '@/utils/request/config'
import { authApi } from '@/api'

export const useUserStore = defineStore('user', () => {
  const token = ref<string>(getToken())
  const nickname = ref<string>('')
  const userId = ref<number>(0)

  const isLoggedIn = computed(() => !!token.value)

  function setToken(t: string) {
    token.value = t
    saveToken(t)
  }

  function checkLogin() {
    if (!token.value) {
      uni.reLaunch({ url: '/pages/login/login' })
    }
  }

  // 微信登录：uni.login 拿 code -> 后端 code2session
  async function login(code: string): Promise<boolean> {
    try {
      const res = await authApi.wxLogin(code)
      if (res && res.code === 0) {
        setToken(res.data.token)
        userId.value = res.data.userId
        nickname.value = res.data.name || ''
        return true
      }
      return false
    } catch (e) {
      console.error('登录异常:', e)
      return false
    }
  }

  async function fetchProfile() {
    try {
      const res = await authApi.profile()
      if (res && res.code === 0 && res.data) {
        nickname.value = res.data.nickname || ''
        userId.value = res.data.id
      }
    } catch (e) {
      console.error('获取资料失败:', e)
    }
  }

  function logout() {
    token.value = ''
    nickname.value = ''
    userId.value = 0
    clearToken()
    uni.reLaunch({ url: '/pages/login/login' })
  }

  return {
    token,
    nickname,
    userId,
    isLoggedIn,
    setToken,
    checkLogin,
    login,
    fetchProfile,
    logout
  }
})
